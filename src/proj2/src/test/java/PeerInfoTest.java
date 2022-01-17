import main.Peer;
import main.model.PeerInfo;
import main.model.SocketInfo;
import main.model.neighbour.Neighbour;
import main.model.timelines.Post;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeerInfoTest {
    public void compareNeighbour(Neighbour neighbour, String username, Integer capacity, Integer degree, Integer timelineSize) {
        assertEquals(username, neighbour.getUsername());
        assertEquals(capacity, neighbour.getCapacity());
        assertEquals(degree, neighbour.getDegree());
        //assertEquals(timelineSize, neighbour.getTimelines().size()); TODO fix
    }

    @Test
    public void testNeighbours() {
        PeerInfo peer1 = null;
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        SocketInfo socketInfo = new SocketInfo("8000", "8101");
        peer1 = new PeerInfo("user1", localhost,30, socketInfo);
        Neighbour n1 = new Neighbour("u1", localhost, 50, 1, 3, "8000", "8100");
        Neighbour n2 = new Neighbour("u2", localhost, 50, 3, 3, "8001", "8101");
        Neighbour n3 = new Neighbour("u1", localhost, 60, 4, 3, "8000", "8100");

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

    @Test
    public void testContentPost() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        Peer peer = new Peer("u1", localhost,  5);

        peer.addPost("uwu");
        peer.addPost("uwu ");
        peer.addPost("ola");
        List<Post> posts = peer.getPeerInfo().getTimelineInfo().getRelatedPosts("uwu");
        assertEquals(2, posts.size());
    }
}
