package main.network.message;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.*;

public class MessageBuilder {
    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array

    public static byte[] messageToByteArray(Message msg) throws IOException {
        byte[] bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(msg);
            out.flush();
            bytes = bos.toByteArray();
        } finally {
            bos.close();
        }

        return bytes;
    }

    public static Message messageFromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        Message message;
        try {
          in = new ObjectInputStream(bis);
          message = (Message) in.readObject();
        } finally {
          try {
            if (in != null) {
              in.close();
            }
          } catch (IOException ex) {
            // ignore close exception
          }
        }

        return message;
    }

    public static Message messageFromSocket(ZMQ.Socket socket) throws ZMQException, IOException, ClassNotFoundException {
        byte[] bytes = socket.recv();
        Message message = messageFromByteArray(bytes);
        return message;
    }
}
