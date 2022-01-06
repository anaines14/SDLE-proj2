package main.controller.network;

import main.model.PeerInfo;
import main.model.message.Message;
import main.controller.message.MessageBuilder;
import main.controller.message.MessageSender;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


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
    private List<Worker> workers;

    private Thread thread;
    private String frontendPort; // For testing

    public Broker(ZContext context, MessageSender sender, PeerInfo peerInfo){
        this.context = context;
        frontend = context.createSocket(SocketType.ROUTER);
        backend = context.createSocket(SocketType.ROUTER);
        control = context.createSocket(SocketType.PULL);

        // Bind each socket, bind frontend to random port
        String hostName = peerInfo.getAddress().getHostName();
        int intP = frontend.bindToRandomPort("tcp://" + hostName);
        String port = Integer.toString(intP);
        peerInfo.setPort(port); // Set port to one that was bound to
        this.frontendPort = port;
        backend.bind("inproc://workers");
        control.bind("inproc://control");

        workers = new ArrayList<>();
        this.thread = new Thread(this::run);
        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(peerInfo, sender, context, id);
            workers.add(worker);
        }
    }

    public void execute() {
        this.thread.start();
    }

    public void close() {
        frontend.close();
        backend.close();
        control.close();
    }

    public void stop() {
        if (this.thread.isAlive()) {
            ZMQ.Socket controlSend = context.createSocket(SocketType.PUSH);
            controlSend.connect("inproc://control");
            controlSend.send("STOP");

            try {
                this.thread.join();
                this.close();
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
            ZMQ.Poller items = context.createPoller(3);
            items.register(backend, ZMQ.Poller.POLLIN);
            items.register(control, ZMQ.Poller.POLLIN);

            if(worker_queues.size() > 0)
                items.register(frontend, ZMQ.Poller.POLLIN);

            if (items.poll() < 0)
                return;

            if (items.pollin(0)) { // Backend, Worker pinged
                try {
                    worker_queues.add(backend.recvStr());

                    //Remove empty msg between messages
                    String empty = backend.recvStr();
                    assert(empty.length() == 0);

                    String clientAddr = backend.recvStr();
                    if (!clientAddr.equals("READY")){
                        //Remove empty msg between messages
                        empty = backend.recvStr();
                        assert(empty.length() == 0);

                        Message reply = null;
                        try {
                            reply = MessageBuilder.messageFromSocket(backend);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        frontend.sendMore(clientAddr);
                        frontend.sendMore("");
                        try {
                            frontend.send(MessageBuilder.messageToByteArray(reply));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ZMQException e) {
                    e.printStackTrace();
                }
            }


            if (items.pollin(1)) { // Control, shutdown now
                return;
            }

            if (items.pollin(2)) { // Frontend, client request
                try {
                    String clientAddr = frontend.recvStr();

                    //Remove empty msg between messages
                    String empty = frontend.recvStr();
                    assert(empty.length() == 0);

                    Message request = null;
                    try {
                        request = MessageBuilder.messageFromSocket(frontend);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    String workerAddr = worker_queues.poll();

                    backend.sendMore(workerAddr);
                    backend.sendMore("");
                    backend.sendMore(clientAddr);
                    backend.sendMore("");
                    try {
                        backend.send(MessageBuilder.messageToByteArray(request));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (ZMQException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getFrontendPort() {
        return frontendPort;
    }
}
