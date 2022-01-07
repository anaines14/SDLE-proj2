package main.model.neighbour;


import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class Neighbour extends Host implements Serializable{
    private final BloomFilter<String> timelines;

    public Neighbour(String username, InetAddress address, String port, int capacity, int degree, List<String> timelines) {
        super(username, address, port, capacity, degree);
        this.timelines = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);

        for(String str: timelines) {
            this.timelines.put(str);
        }
    }

    public Neighbour(Host host) {
        super(host);
        this.timelines = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);
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