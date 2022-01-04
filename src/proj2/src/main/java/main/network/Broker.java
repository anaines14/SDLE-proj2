package main.network;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZSocket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class Broker implements Runnable{
    private static final int N_WORKERS = 3;

    ZMQ.Socket frontend;
    ZMQ.Socket backend;
    List<Worker> workers;
    ZContext zContext;

    Broker(ZContext zContext, PeerInfo peerInfo){
        this.zContext = zContext;
        frontend = zContext.createSocket(SocketType.ROUTER);
        backend = zContext.createSocket(SocketType.ROUTER);
        frontend.bind("tcp://" + peerInfo.address + ":" + peerInfo.port);
        backend.bind("inproc://workers");
        workers = new ArrayList<>();

        for(int id = 0; id < N_WORKERS; id++){
            Worker worker = new Worker(peerInfo, zContext, id); 
            workers.add(worker);
            worker.run();
        }
    }

    @Override
    public void run(){
        Queue<String> worker_queues = new LinkedList<>();

        while(!Thread.currentThread().isInterrupted()){
            ZMQ.Poller items = zContext.createPoller(2);
            items.register(backend, ZMQ.Poller.POLLIN);

            if(worker_queues.size() > 0)
                items.register(frontend, ZMQ.Poller.POLLIN);

            if(items.poll() < 0 )
                break;

            if(items.pollin(0)){
                worker_queues.add(backend.recvStr());

                //Remove empty msg between messages
                String empty = backend.recvStr();
                assert (empty.length() == 0);

                String clientAddr = backend.recvStr();

                if(!clientAddr.equals("READY")){
                    //Remove empty msg between messages
                    empty = backend.recvStr();
                    assert (empty.length() == 0);

                    String reply = backend.recvStr();
                    frontend.sendMore(clientAddr);
                    frontend.sendMore("");
                    frontend.sendMore(reply);
                }
            }

            if(items.pollin(1)){
                String clientAddr = frontend.recvStr();

                //Remove empty msg between messages
                String empty = backend.recvStr();
                assert (empty.length() == 0);

                String request = frontend.recvStr();

                String workerAddr = worker_queues.poll();

                backend.sendMore(workerAddr);
                backend.sendMore("");
                backend.sendMore(clientAddr);
                backend.sendMore("");
                backend.sendMore(request);

            }
        }
    }

}
