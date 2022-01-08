package main.controller.network;

import main.controller.message.MessageSender;
import main.model.PeerInfo;
import main.model.message.Message;
import main.controller.message.MessageBuilder;
import main.model.message.response.MessageResponse;
import main.model.timelines.Post;
import org.zeromq.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;


public class Broker {
    private static final int N_WORKERS = 3;

    private ZContext context;
    private ZMQ.Socket frontend;
    private ZMQ.Socket backend;
    // To signal the broker thread to shutdown, we use a control socket
    // This is better than an interrupt because this thread has a poller, making the process of exiting more safe
    // We could use interrupts here, but it would cause too many try catches (smelly code) and JeroMQ only started
    // supporting socket interruption on receive calls recently
    private ZMQ.Socket control;
    private ZMQ.Socket publisher;
    private ConcurrentMap<String, ZMQ.Socket> subscriptions; // Connects to all nodes that we have subscribed to
    private ConcurrentLinkedQueue<Post> subMessages; // New posts that are posted by our subs
    private List<Worker> workers;

    private Thread thread;
    private String frontendPort; // For testing
    private String publisherPort; // For testing
    // Messages that we are expecting to receive, workers fill these when they receive the request
    private final ConcurrentMap<UUID, CompletableFuture<MessageResponse>> promises;

    public Broker(ZContext context, InetAddress address){
        this.context = context;
        this.backend = context.createSocket(SocketType.ROUTER);
        this.control = context.createSocket(SocketType.PULL);
        this.frontend = context.createSocket(SocketType.REP);
        this.publisher = context.createSocket(SocketType.PUB);

        String hostName = address.getHostName();
        this.frontendPort = String.valueOf(frontend.bindToRandomPort("tcp://" + hostName));
        this.publisherPort = String.valueOf(publisher.bindToRandomPort("tcp://" + hostName));

        System.out.println("BOUND TO " + "tcp://" + hostName + ":" + publisherPort);
        // Bind each socket, bind frontend and publisher to random port

        this.backend.bind("inproc://workers");
        this.control.bind("inproc://control");

        this.promises = new ConcurrentHashMap<>();
        this.workers = new ArrayList<>();
        this.thread = new Thread(this::run);
        this.subscriptions = new ConcurrentHashMap<>();
        this.subMessages = new ConcurrentLinkedQueue<>();
        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(id, promises, context);
            workers.add(worker);
        }
    }

    public String getFrontendPort() {
        return frontendPort;
    }

    public String getPublisherPort() {
        return publisherPort;
    }

    public List<Post> popSubMessages() {
        List<Post> res = null;
        synchronized (subMessages) {
            res = subMessages.stream().toList();
            subMessages.clear();
        }
        return res;
    }

    public void setSender(MessageSender sender) {
        for (Worker w: workers)
            w.setSender(sender);
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        for (Worker w: workers)
            w.setPeerInfo(peerInfo);
    }

    public Future<MessageResponse> addPromise(UUID id) {
        if (promises.containsKey(id))
            return promises.get(id);

        CompletableFuture<MessageResponse> promise = new CompletableFuture<>();
        promises.put(id, promise);
        return promise;
    }

    public void removePromise(UUID id) {
        if (!promises.containsKey(id))
            return;
        promises.remove(id);
    }

    public void subscribe(String username, InetAddress address, String port) {
        ZMQ.Socket subscription = context.createSocket(SocketType.SUB);
        String hostName = address.getHostName();
        subscription.connect("tcp://" + hostName + ":" + port);
        System.out.println("SUBBED TO " + "tcp://" + hostName + ":" + port);
        subscription.subscribe("".getBytes());
        subscriptions.put(username, subscription);

        this.sendToControl("NEW_SUB");
    }

    private void sendToControl(String new_sub) {
        ZMQ.Socket controlSend = context.createSocket(SocketType.PUSH);
        controlSend.connect("inproc://control");
        controlSend.send(new_sub);
        controlSend.close();
    }

    public void unsubscribe(String username) {
        ZMQ.Socket subscription = subscriptions.get(username);
        subscription.setLinger(0);
        subscription.close();
        subscriptions.remove(username);
        this.sendToControl("NEW_UNSUB");
    }

    public void publishPost(Post post) {
        try {
            this.publisher.send(MessageBuilder.objectToByteArray(post));
        } catch (IOException e) { // Thrown when we don't receive a post
            e.printStackTrace();
        }
    }

    public void execute() {
        this.thread.start();
    }

    public void stop() {
        if (this.thread.isAlive()) {
            // CHECK controlSend.close() after try
            this.sendToControl("STOP");

            try {
                this.thread.join();
                frontend.close();
                backend.close();
                control.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Worker worker: workers)
            worker.stop();
    }

    public void run(){
        for (Worker worker: workers)
            worker.execute();
        Queue<String> worker_queues = new LinkedList<>();

        while (!Thread.currentThread().isInterrupted()) {
            ZMQ.Poller items = context.createPoller(4);
            items.register(backend, ZMQ.Poller.POLLIN);
            items.register(control, ZMQ.Poller.POLLIN);

            for (ZMQ.Socket socket: subscriptions.values())
                items.register(socket, ZMQ.Poller.POLLIN);

            if(worker_queues.size() > 0) {
                items.register(frontend, ZMQ.Poller.POLLIN);
            }

            if (items.poll() < 0)
                return;

            if (items.pollin(0)) { // Backend, Worker pinged
                try {
                    worker_queues.add(backend.recvStr());

                    //Remove empty msg between messages
                    String empty = backend.recvStr();
                    assert(empty.length() == 0);

                    String workerResponse = backend.recvStr();
                    assert(workerResponse.equals("READY"));
                } catch (ZMQException e) {
                    e.printStackTrace();
                }
            }

            if (items.pollin(1)) { // Control, shutdown now or add new sub
                String cmd = control.recvStr();
                if (cmd.equals("STOP"))
                    return;
                else if (cmd.equals("NEW_SUB") || cmd.equals("NEW_UNSUB")) {} // Do nothing
            }

            Set<String> subscribedUsers = this.subscriptions.keySet();
            int i=0;
            for (String ignored : subscribedUsers) {
                if (items.pollin(2 + i)) { // Received post from subscription
                    ZMQ.Socket subscription = items.getSocket(2 + i);
                    try {
                        this.subMessages.add(MessageBuilder.postFromSocket(subscription));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                ++i;
            }

            if (items.pollin(2 + subscriptions.size())) { // Frontend, client request
                try {
                    //Remove empty msg between messages
                    Message request = null;
                    try {
                        request = MessageBuilder.messageFromSocket(frontend);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    String workerAddr = worker_queues.poll();

                    backend.sendMore(workerAddr);
                    backend.sendMore("");
                    try {
                        backend.send(MessageBuilder.objectToByteArray(request));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    frontend.send("OK");
                } catch (ZMQException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
