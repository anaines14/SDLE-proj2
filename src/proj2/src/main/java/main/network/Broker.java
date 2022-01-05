package main.network;

import org.zeromq.*;

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

    Broker(ZContext context, PeerInfo peerInfo){
        this.context = context;
        frontend = context.createSocket(SocketType.ROUTER);
        backend = context.createSocket(SocketType.ROUTER);
        control = context.createSocket(SocketType.PULL);
        frontend.bind("tcp://*:" + peerInfo.port); // TODO Put port
        backend.bind("inproc://workers");
        control.bind("inproc://control");
        workers = new ArrayList<>();
        this.thread = new Thread(this::run);

        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(peerInfo, context, id);
            workers.add(worker);
            worker.execute();
        }
        System.out.println("RUNNING BROKER: " + peerInfo.port);
    }

    public void execute() {
        this.thread.start();
    }

    public void stop() {
        frontend.close();
        backend.close();
        control.close();

        if (this.thread.isAlive()) {
            this.thread.interrupt();
            ZMQ.Socket controlSend = context.createSocket(SocketType.PUSH);
            controlSend.connect("inproc://control");
            controlSend.send("STOP");
            System.out.println("SENT");

            try {
                this.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Worker w: workers)
            w.stop();
    }

    public void run(){
        Queue<String> worker_queues = new LinkedList<>();

        while (!Thread.currentThread().isInterrupted()) {
            ZMQ.Poller items = context.createPoller(3);
            items.register(backend, ZMQ.Poller.POLLIN);
            items.register(frontend, ZMQ.Poller.POLLIN);
            items.register(control, ZMQ.Poller.POLLIN);

//            if(worker_queues.size() > 0)
//                items.register(frontend, ZMQ.Poller.POLLIN);

            int errno = items.poll();

            if (items.pollin(0)) {
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

                        String reply = backend.recvStr();
                        frontend.sendMore(clientAddr);
                        frontend.sendMore("");
                        frontend.sendMore(reply);
                    }
                } catch (ZMQException e) {
                    e.printStackTrace();
                }
            }

            if (items.pollin(1)) {
                try {
                    String clientAddr = frontend.recvStr();

                    //Remove empty msg between messages
                    String empty = backend.recvStr();
                    assert(empty.length() == 0);

                    String request = frontend.recvStr();

                    String workerAddr = worker_queues.poll();

                    backend.sendMore(workerAddr);
                    backend.sendMore("");
                    backend.sendMore(clientAddr);
                    backend.sendMore("");
                    backend.sendMore(request);
                } catch (ZMQException e) {
                    e.printStackTrace();
                }
            }

            if (items.pollin(2)) {
                System.out.println("HERE");
                break;
            }
        }
    }
}
