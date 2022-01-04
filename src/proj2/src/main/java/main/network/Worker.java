package main.network;

import main.network.message.Message;
import main.network.message.MessageBuilder;
import main.network.message.MessageHandler;
import main.network.message.MessageSender;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZSocket;

import java.io.IOException;
import java.math.BigInteger;

public class Worker implements Runnable{
    private MessageHandler handler;
    private ZMQ.Socket worker;


    Worker(PeerInfo peerInfo, ZContext context, int id){
        MessageSender sender = new MessageSender(peerInfo, context);
        handler = new MessageHandler(peerInfo, context, sender);
        worker = context.createSocket(SocketType.REQ);

        BigInteger bigint = BigInteger.valueOf(id);
        worker.setIdentity(bigint.toByteArray());
        worker.connect("inproc://workers");
        worker.send("READY");
    }

    @Override
    public void run() {
        while(true){
            try {
                Message message = MessageBuilder.messageFromSocket(worker);
                handle(message);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void handle(Message message) {
        switch(message.getType()){

        }
    }
}
