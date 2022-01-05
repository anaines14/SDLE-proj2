package main.network;

import main.network.message.Message;
import main.network.message.MessageBuilder;
import main.network.message.MessageHandler;
import main.network.message.MessageSender;
import org.zeromq.*;

import java.io.IOException;
import java.math.BigInteger;

public class Worker {
    private MessageHandler handler;
    private ZMQ.Socket worker;
    private Thread thread;


    Worker(PeerInfo peerInfo, ZContext context, int id){
        MessageSender sender = new MessageSender(peerInfo, context);
        handler = new MessageHandler(peerInfo, context, sender);
        worker = context.createSocket(SocketType.REQ);

        BigInteger bigint = BigInteger.valueOf(id);
        worker.setIdentity(bigint.toByteArray());
        worker.connect("inproc://workers");
        worker.send("READY");
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
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = MessageBuilder.messageFromSocket(worker);
                this.handler.handle(message);
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
