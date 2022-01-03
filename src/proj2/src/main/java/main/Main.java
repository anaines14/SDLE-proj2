package main;

import main.network.GnuNode;
import main.network.message.Message;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            GnuNode node1 = new GnuNode("localhost", "8100");
            GnuNode node2 = new GnuNode("localhost", "8101");

            node1.send(new Message("username1", "content1"), "8101");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
