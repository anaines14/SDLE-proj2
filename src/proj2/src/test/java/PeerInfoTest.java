import main.model.PeerInfo;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeerInfoTest {
    public void compareNeighbour(Neighbour neighbour, String username, Integer capacity, Integer degree, Integer timelineSize) {
        assertEquals(username, neighbour.getUsername());
        assertEquals(capacity, neighbour.getCapacity());
        assertEquals(degree, neighbour.getDegree());
        assertEquals(timelineSize, neighbour.getTimelines().size());
    }

    @Test
    public void testNeighbours() {
        PeerInfo peer1 = null;
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        peer1 = new PeerInfo(localhost, "user1", 30);
        List<String> timelines1 = new ArrayList<>(Arrays.asList("u1", "u2", "u3"));
        List<String> timelines2 = new ArrayList<>(Arrays.asList("u1", "u2", "u3", "u4"));
        List<String> timelines3 = new ArrayList<>(Arrays.asList("u1", "u2", "u3", "u4", "u5"));
        Neighbour n1 = new Neighbour("u1", localhost, "8000", 50, 1, timelines1);
        Neighbour n2 = new Neighbour("u2", localhost, "8001", 50, 3, timelines2);
        Neighbour n3 = new Neighbour("u1", localhost, "8000", 60, 4, timelines3);

        List<Neighbour> neighbours = new ArrayList<>(peer1.getNeighbours());
        assertEquals(0, neighbours.size());

        peer1.addNeighbour(n1);
        neighbours = new ArrayList<>(peer1.getNeighbours());
        assertEquals(1, neighbours.size());
        compareNeighbour(neighbours.get(0), "u1", 50, 1, 3);

        peer1.addNeighbour(n2);
        neighbours = new ArrayList<>(peer1.getNeighbours());
        assertEquals(2, neighbours.size());
        compareNeighbour(neighbours.get(1), "u2", 50, 3, 4);

        peer1.updateNeighbour(n3);
        neighbours = new ArrayList<>(peer1.getNeighbours());
        assertEquals(2, neighbours.size());
        compareNeighbour(neighbours.get(0), "u1", 60, 4, 5);
    }
}
