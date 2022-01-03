package visualization;


import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import main.Peer;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;

public class MyGraphFrame extends JApplet {
    private static final Dimension DEFAULT_SIZE = new Dimension(1000,1000);
    private JGraphXAdapter<Peer, DefaultEdge> graphModelAdapter;
    SimpleGraph<Peer,DefaultEdge> myGraph;

    public MyGraphFrame(){
        myGraph = new SimpleGraph<>(DefaultEdge.class);
        Peer peer1 = new Peer("Carlos");
        Peer peer2 = new Peer("Lucas Dumb");
        Peer peer3 = new Peer("CUCX ?");
        Peer peer4 = new Peer("Crocodilo");

        myGraph.addVertex(peer1);
        myGraph.addVertex(peer2);
        myGraph.addVertex(peer3);
        myGraph.addVertex(peer4);

        myGraph.addEdge(peer1,peer4);
    }

    @Override
    public void init() {
        graphModelAdapter = new JGraphXAdapter<Peer, DefaultEdge>(myGraph);
        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(graphModelAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);

        mxCircleLayout layout = new mxCircleLayout(graphModelAdapter);

        int radius = 100;
        layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
        layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
        layout.setRadius(radius);
        layout.setMoveCircle(true);

        layout.execute(graphModelAdapter.getDefaultParent());

    }
}
