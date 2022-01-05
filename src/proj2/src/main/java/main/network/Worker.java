package main.network;

import main.network.message.Message;
import main.network.message.MessageBuilder;
import main.network.message.MessageHandler;
import main.network.message.MessageSender;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Worker {
    private MessageHandler handler;
    private MessageSender sender;
    private ZMQ.Socket worker;
    private Thread thread;

    Worker(PeerInfo peerInfo, MessageSender sender, ZContext context, int id){
        handler = new MessageHandler(peerInfo, sender);
        this.sender = sender;
        worker = context.createSocket(SocketType.REQ);

        worker.setIdentity(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
        this.thread = new Thread(this::run);
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
                String clientAddress = worker.recvStr();
                String empty = worker.recvStr();
                assert(empty.length() == 0);

                Message message =  MessageBuilder.messageFromSocket(worker);
                worker.sendMore(clientAddress);
                worker.sendMore("");

                Message replyMsg = this.handler.handle(message);
                this.sender.send(replyMsg, worker);
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
