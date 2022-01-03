package main;

public class Main {
    public static void main(String[] args) {
        Peer peer = new Peer("username");
        peer.addPost("Hello World!");
        peer.save();
    }
}
