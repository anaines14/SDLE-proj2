package main.network;

import main.network.message.Message;
import main.network.message.MessageBuilder;
import main.network.message.MessageSender;
import org.zeromq.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class Broker {
    private static final int N_WORKERS = 3;

    private ZContext context;
    private ZMQ.Socket frontend;
    private ZMQ.Socket backend;
    private ZMQ.Socket control;
    private List<Worker> workers;

    private Thread thread;

    Broker(ZContext context, MessageSender sender, PeerInfo peerInfo){
        this.context = context;
        frontend = context.createSocket(SocketType.ROUTER);
        backend = context.createSocket(SocketType.ROUTER);
        control = context.createSocket(SocketType.PULL);
        frontend.bind("tcp://*:" + peerInfo.port); // TODO Put port
        backend.bind("inproc://workers");
        System.out.println("BINDING " + peerInfo.port);
        // To signal the broker thread to shutdown, we use a control socket
        // This is better than an interrupt because this thread has a poller, making the process of exiting more safe
        // We could use interrupts here, but it would cause too many try catches (smelly code) and JeroMQ only started
        // supporting socket interruption on receive calls recently
        control.bind("inproc://control");
        workers = new ArrayList<>();
        this.thread = new Thread(this::run);

        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(peerInfo, sender, context, id);
            workers.add(worker);
        }
        System.out.println("RUNNING BROKER: " + peerInfo.port);
    }

    public void execute() {
        this.thread.start();
    }

    public void close() {
        System.out.println("CLOSING");
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

            items.poll();
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

                    System.out.println("GOT REQUEST");
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
                    System.out.println("Got request from " + clientAddr + ": " + request);
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
}
