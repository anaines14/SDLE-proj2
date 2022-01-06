package main.gui;

import main.model.neighbour.Neighbour;

import java.util.Set;

public interface Observer {
    void newNodeUpdate(String username);
    void newEdgeUpdate(String username, String neighbour);
    void removeEdgeUpdate(String username, String neighbour);
}
