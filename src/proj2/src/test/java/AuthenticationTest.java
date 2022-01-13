import main.Peer;
import main.controller.message.MessageSender;
import main.controller.network.AuthenticationServer;
import main.model.timelines.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationTest {
    private Peer peer1;
    private Peer peer2;
    private AuthenticationServer authenticationServer;
    private ScheduledThreadPoolExecutor scheduler;

    @BeforeEach
    public void setUp() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        peer1 = new Peer("u1", localhost,  5);
        peer2 = new Peer("u2", localhost, 1);
        authenticationServer = new AuthenticationServer(localhost);
        scheduler = new ScheduledThreadPoolExecutor(2);

        List<Peer> peers = Arrays.asList(peer1, peer2);

        for (Peer p: peers) {
            p.execute(scheduler);
            System.out.println(p.getPeerInfo().getUsername() + ": " + p.getPeerInfo().getPort());
        }
        authenticationServer.execute();

        peer1.join(peer2);
    }


    @Test
    public void loginAndOut(){
        peer1.register("Carlos", authenticationServer.getAddress(), authenticationServer.getSocketPort());
        assertEquals(true, peer1.getPeerInfo().isAuth());

        peer1.logout();
        assertEquals(false, peer1.getPeerInfo().isAuth());

        peer1.login("Carlos", authenticationServer.getAddress(), authenticationServer.getSocketPort());
        assertEquals(true, peer1.getPeerInfo().isAuth());
    }

    @Test
    public void signTimeline(){
        peer1.register("Carlos", authenticationServer.getAddress(), authenticationServer.getSocketPort());
        peer2.register("Outra password mega fixe", authenticationServer.getAddress(), authenticationServer.getSocketPort());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        peer1.addPost("O que e que e a tua publicaçao");

        Timeline t = peer2.requestTimeline("u1");
        assertEquals("O que e que e a tua publicaçao", t.getPosts().get(0).getContent());

        //replace private key so it doesnt match
        peer1.getPeerInfo().setPrivateKey(peer2.getPeerInfo().getPrivateKey());
        peer1.addPost("QUE BURRO MAL ASSINADO");

        t = peer2.requestTimeline("u1");
        assertNull(t);
    }

    @Test
    public void signPost(){
        peer1.register("Carlos", authenticationServer.getAddress(), authenticationServer.getSocketPort());
        peer2.register("Outra password mega fixe", authenticationServer.getAddress(), authenticationServer.getSocketPort());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer1.requestSub("u2");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer2.addPost("Uma posta");
        peer2.addPost("Duas postas");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(2, peer1.getPostOfSubscriptions().get("u2").size());

        peer2.addPost("Terceira Posta");
        // replace private key so it doesnt match
        peer2.getPeerInfo().setPrivateKey(peer1.getPeerInfo().getPrivateKey());
        peer2.addPost("Posta mal assinada");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertNotEquals(2, peer1.getPostOfSubscriptions().get("u2").size());
    }
}
