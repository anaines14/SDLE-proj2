package main.controller.network;

import main.gui.Observer;
import main.model.PeerInfo;
import main.model.message.Message;
import main.controller.message.MessageBuilder;
import main.controller.message.MessageHandler;
import main.controller.message.MessageSender;
import main.model.message.response.MessageResponse;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import static main.Peer.MAX_RETRY;
import static main.Peer.RCV_TIMEOUT;

public class Worker {
    private MessageHandler handler;
    private MessageSender sender;
    private ZMQ.Socket worker;
    private Thread thread;

    public Worker(PeerInfo peerInfo, int id,
                  ConcurrentMap<UUID, CompletableFuture<MessageResponse>> promises, ZContext context){
        this.sender = new MessageSender(peerInfo, MAX_RETRY, RCV_TIMEOUT, context);
        this.handler = new MessageHandler(peerInfo, sender, promises);
        this.worker = context.createSocket(SocketType.REQ);
        this.worker.setIdentity(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
        this.thread = new Thread(this::run);
    }

    public void subscribe(Observer o) {
        this.sender.subscribe(o);
    }

    public void execute() {
        this.thread.start();
    }

    public void stop() {
        if (this.thread.isAlive()) {
            this.thread.interrupt();
            try {
                this.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.worker.setLinger(0);
        this.worker.close();
    }

    private void run() {
        worker.connect("inproc://workers");
        worker.send("READY");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message =  MessageBuilder.messageFromSocket(worker);
                this.handler.handle(message);
                worker.send("READY");
            } catch (ZMQException e) {
                if (e.getErrorCode() == ZMQ.Error.ETERM.getCode() || // Context terminated
                        e.getErrorCode() == ZMQ.Error.EINTR.getCode()) // Interrupted
                    break;
                e.printStackTrace();
                this.stop();
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                this.stop();
                return;
            }
        }
    }
}
