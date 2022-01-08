package main.controller.network;

import main.controller.message.MessageSender;
import main.model.PeerInfo;
import main.model.message.Message;
import main.controller.message.MessageBuilder;
import main.model.neighbour.Host;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;


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
    private List<Worker> workers;

    private Thread thread;
    private String frontendPort; // For testing
    private String publisherPort; // For testing
    // Messages that we are expecting to receive, workers fill these when they receive the request
    private final ConcurrentMap<UUID, CompletableFuture<Message>> promises;


    public Broker(ZContext context, MessageSender sender, PeerInfo peerInfo){
        this.context = context;
        this.backend = context.createSocket(SocketType.ROUTER);
        this.control = context.createSocket(SocketType.PULL);
        this.frontend = context.createSocket(SocketType.REP);
        this.publisher = context.createSocket(SocketType.PUB);
        String.valueOf(frontend.bindToRandomPort("tcp://" + peerInfo.getAddress()));
        String.valueOf(frontend.bindToRandomPort("tcp://" + peerInfo.getAddress()));
        this.backend.bind("inproc://workers");
        this.control.bind("inproc://control");
        this.promises = new ConcurrentHashMap<>();
        this.workers = new ArrayList<>();
        this.thread = new Thread(this::run);
        this.subscriptions = new ConcurrentHashMap<>();
        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(peerInfo, id, sender, promises, context);
            workers.add(worker);
        }
    }

    public Broker(ZContext context, ZMQ.Socket frontend, ZMQ.Socket publisher, MessageSender sender, PeerInfo peerInfo){
        this.context = context;
        this.backend = context.createSocket(SocketType.ROUTER);
        this.frontend = frontend;
        this.control = context.createSocket(SocketType.PULL);
        this.publisher = publisher;

        String hostName = peerInfo.getAddress().getHostName();
        // Bind each socket, bind frontend and publisher to random port

        this.backend.bind("inproc://workers");
        this.control.bind("inproc://control");

        this.promises = new ConcurrentHashMap<>();
        this.workers = new ArrayList<>();
        this.thread = new Thread(this::run);
        this.subscriptions = new ConcurrentHashMap<>();
        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(peerInfo, id, sender, promises, context);
            workers.add(worker);
        }
    }

    public Future<Message> addPromise(UUID id) {
        if (promises.containsKey(id))
            return promises.get(id);

        CompletableFuture<Message> promise = new CompletableFuture<>();
        promises.put(id, promise);
        return promise;
    }

    public void removePromise(UUID id) {
        if (!promises.containsKey(id))
            return;
        promises.remove(id);
    }

    public void subscribe(Host publisher) {
        ZMQ.Socket subscription = context.createSocket(SocketType.SUB);
        String hostName = publisher.getAddress().getHostName();
        subscription.connect("tcp://" + hostName + ":" + publisher.getPort());
        subscriptions.put(publisher.getUsername(), subscription);
    }

    public void unsubscribe(String username) {
        ZMQ.Socket subscription = subscriptions.get(username);
        subscription.setLinger(0);
        subscription.close();
    }

    public void execute() {
        this.thread.start();
    }

    public void stop() {
        if (this.thread.isAlive()) {
            ZMQ.Socket controlSend = context.createSocket(SocketType.PUSH);
            controlSend.connect("inproc://control");
            controlSend.send("STOP");

            try {
                this.thread.join();
                frontend.close();
                backend.close();
                control.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controlSend.close();
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

            if (items.pollin(1)) { // Control, shutdown now
                return;
            }

            if (items.pollin(2)) { // Frontend, client request
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
                        backend.send(MessageBuilder.messageToByteArray(request));
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
