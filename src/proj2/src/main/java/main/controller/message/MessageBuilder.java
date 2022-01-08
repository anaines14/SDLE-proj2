package main.controller.message;

import main.model.message.Message;
import main.model.timelines.Post;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.*;

public class MessageBuilder {
    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array

    public static byte[] objectToByteArray(Object msg) throws IOException {
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

    public static Object objectFromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        Object obj;
        try {
          in = new ObjectInputStream(bis);
          obj = in.readObject();
        } finally {
          try {
            if (in != null) {
              in.close();
            }
          } catch (IOException ex) {
            // ignore close exception
          }
        }

        return obj;
    }

    public static Message messageFromSocket(ZMQ.Socket socket) throws ZMQException, IOException, ClassNotFoundException {
        byte[] bytes = socket.recv();
        return (Message) objectFromByteArray(bytes);
    }

    public static Post postFromSocket(ZMQ.Socket socket) throws ZMQException, IOException, ClassNotFoundException {
        byte[] bytes = socket.recv();
        return (Post) objectFromByteArray(bytes);
    }
}
