package main.gui;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;

import java.net.URL;


public class GraphWrapper implements Observer{
    private final Graph graph;

    public GraphWrapper(String graphName) {
        System.setProperty("org.graphstream.ui","swing");
        this.graph = new MultiGraph(graphName);

        // fetch file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("stylesheet.css");

        // set style
        graph.setAttribute("ui.stylesheet", "url('" + resource + "')");
    }

    public void display() {
        graph.display();
        System.out.println("Displayed GRAPH in external window.");
    }

    public void addNode(String name, int capacity) throws IdAlreadyInUseException {
        if (graph.getNode(name) == null) {
            Node node = graph.addNode(name);
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", "size: " + capacity*5 + ";");
        }
    }

    public void addEdge(String peer1, String peer2) throws IdAlreadyInUseException,
            ElementNotFoundException, EdgeRejectedException {
        String edgeId = peer1 + peer2;

        // if edge not in graph => add it
        if (graph.getEdge(edgeId) == null) {
            this.graph.addEdge(edgeId, peer1, peer2, true);
        }
    }

    public void removeEdge(String peer1, String peer2) throws ElementNotFoundException {
        String edgeId = peer1 + peer2;
        this.graph.removeEdge(edgeId);
    }

    // updates

    // remove edge
    public void removeEdgeUpdate(String username, String neighbour) {
        try{
            this.removeEdge(username, neighbour);
        } catch(ElementNotFoundException e){
            System.err.println("ERROR: Failed to remove edge from graph");
        }
    }

    // new edge
    public void newEdgeUpdate(String username, String neighbour) {
        try{
            this.addEdge(username, neighbour);
        } catch(IdAlreadyInUseException | ElementNotFoundException | EdgeRejectedException e){
            e.printStackTrace();
            System.err.println("ERROR: Failed to add edge on graph");
        }
    }

    // new node
    public void newNodeUpdate(String username, int capacity) {
        // if node not in graph => add it
        try {
            this.addNode(username, capacity);
        } catch(IdAlreadyInUseException e) {
            System.err.println("ERROR: Failed to add node on graph");
        }
    }
}
