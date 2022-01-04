package main;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class Main{
    public static void main(String[] args) {
        //Graph thingys
        System.setProperty("org.graphstream.ui","swing");
        Graph graph = new SingleGraph("Example");

        graph.addNode("Lucas1");
        graph.addNode("2");
        graph.addNode("3");
        graph.addNode("Crocodilo");

        graph.addEdge("Lucas liga com crocodilo get it? AHAHAHAHAH comedia", "Lucas1","Crocodilo");

        graph.display();
    }

}
