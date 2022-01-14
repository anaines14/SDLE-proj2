package main.gui;

import java.util.Set;

public interface Observer {
    void newNodeUpdate(String username, String port, int capacity);
    void newEdgeUpdate(String username, String neighbour);
    void removeEdgeUpdate(String username, String neighbour);
    void newQueryUpdate(String source, String destination);
    void newQueryHitUpdate(String source, String destination);
    void newSubUpdate(String source, String destination);
    void newPostUpdate(String source, Set<String> destinations);
    void stopUpdate(String port);
}
