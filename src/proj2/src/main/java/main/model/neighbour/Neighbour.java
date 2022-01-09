package main.model.neighbour;


import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;


public class Neighbour extends Host implements Serializable{
    private BloomFilter<String> timelines;

    public Neighbour(String username, InetAddress address, String port, String frontendPort, int capacity, int degree, int maxNbrs) {
        super(username, address, port, frontendPort, capacity, degree, maxNbrs);
        this.timelines = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);
    }

    public Neighbour(Host host) {
        super(host);
        this.timelines = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);
    }

    public Neighbour(Host host, BloomFilter<String> timelines) {
        super(host);
        this.timelines = timelines;
    }

    public boolean hasTimeline(String username) {
        return timelines.mightContain(username);
    }

    public BloomFilter<String> getTimelines() {
        return timelines;
    }

    public String toString() {
        return super.toString() + " Degree: " + getDegree();
    }
}