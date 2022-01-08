package main.gui;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.net.URL;


public class GraphWrapper implements Observer{
    private final Graph graph;
    private final SpriteManager sprites;

    public GraphWrapper(String graphName) {
        System.setProperty("org.graphstream.ui","swing");
        this.graph = new MultiGraph(graphName);

        // fetch file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("stylesheet.css");

        // set style
        graph.setAttribute("ui.stylesheet", "url('" + resource + "')");

        this.sprites = new SpriteManager(this.graph);
    }

    public void display() {
        graph.display();
        System.out.println("Displayed GRAPH in external window.");
    }

    public void addNode(String name, String port, int capacity) throws IdAlreadyInUseException {
        if (graph.getNode(port) == null) {
            Node node = graph.addNode(port);
            node.setAttribute("ui.label", name);
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
    public void newNodeUpdate(String username, String port, int capacity) {
        // if node not in graph => add it
        try {
            this.addNode(username, port, capacity);
        } catch(IdAlreadyInUseException e) {
            System.err.println("ERROR: Failed to add node on graph");
        }
    }

    // new query message
    public void newQueryUpdate(String source, String destination) {
        String id = source + destination + "Msg";

        try {
            Edge e = this.graph.addEdge(id, source, destination);
            e.setAttribute("ui.class", "message");

            Sprite sprite = sprites.addSprite(id);
            sprite.attachToEdge(id);
            sprite.setAttribute("ui.class", "query");

            this.sendMsgView(sprite, id);
            this.graph.removeEdge(id);
        } catch(IdAlreadyInUseException | EdgeRejectedException | ElementNotFoundException e ) {
            e.printStackTrace();
        }
    }

    // new hit message
    public void newQueryHitUpdate(String source, String destination) {
        String id = source + destination + "Msg";

        try {
            Edge e = this.graph.addEdge(id, source, destination);
            e.setAttribute("ui.class", "message");

            Sprite sprite = sprites.addSprite(id);
            sprite.attachToEdge(id);
            sprite.setAttribute("ui.class", "hit");

            this.sendMsgView(sprite, id);
            this.graph.removeEdge(id);
        } catch(IdAlreadyInUseException | EdgeRejectedException | ElementNotFoundException e ) {
            e.printStackTrace();
        }
    }

    public void sendMsgView(Sprite sprite, String id) {
        try {
            for (double x = 0.0; x < 1; x += 0.1){
                sprite.setPosition(x, 0, 0);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sprites.removeSprite(id);
        sprite.detach();
    }
}
