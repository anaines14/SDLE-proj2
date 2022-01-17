package main;

import main.controller.network.Authenticator;
import main.gui.Observer;
import main.model.Pair;
import main.model.PeerInfo;
import main.controller.network.Broker;
import main.controller.message.MessageSender;
import main.model.message.request.*;
import main.model.message.request.query.QueryMessage;
import main.model.message.request.query.SubMessage;
import main.model.message.response.*;
import main.model.message.response.query.QueryHitMessage;
import main.model.message.response.query.SubHitMessage;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.Post;
import main.model.timelines.Timeline;
import main.model.timelines.TimelineInfo;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.Serializable;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Peer implements Serializable {
    public static final int PINGNEIGH_DELAY = 1000;
    public static final int ADDNEIGH_DELAY = 1000;
    private static final long PINGSUBS_DELAY = 1000;
    public static final int MIN_NGBRS = 1;
    public static final int MAX_RETRY = 3;
    public static final int RCV_TIMEOUT = 500;
    public static final int MAX_RANDOM_NEIGH = 2;
    // Minimun number of neighbours necessary to be considered a super peers
    public static final int SP_MIN = 5;
    public static final int MAX_SUBS = 3;

    // Model/Data members
    private final PeerInfo peerInfo;
    private final ZContext context;

    // Network members
    private final Broker broker;
    private final Authenticator authenticator;
    private final MessageSender sender;

    // Hooks
    private ScheduledFuture<?> pingNeighFuture;
    private ScheduledFuture<?> addNeighFuture;
    private ScheduledFuture<?> pingSubsFuture;


    // CALL PEER WITH PASSWORD != "" TO REGISTER
    public Peer(String username, InetAddress address, int capacity) {
        this.context = new ZContext();
        this.authenticator = new Authenticator(context);
        this.broker = new Broker(context, address, authenticator);
        this.peerInfo = new PeerInfo(username, address, capacity, broker.getSocketInfo());
        this.sender = new MessageSender(peerInfo, MAX_RETRY, RCV_TIMEOUT, context);
        this.broker.setSender(sender);
        this.broker.setPeerInfo(peerInfo);
    }


    public void join(Neighbour neighbour) {
        peerInfo.addNeighbour(neighbour);
    }

    public void join(Peer peer) {
        peerInfo.addHost(peer.getPeerInfo().getHost());
    }

    public void printTimelines() {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.printTimelines();
    }

    public void addTimeline(Timeline timeline) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.addTimeline(timeline);
    }

    public void updatePost(int postId, String newContent) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.updatePost(peerInfo.getUsername(), postId, newContent);
    }

    public void addPost(String newContent) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        Post addedPost = timelineInfo.addPost(peerInfo.getUsername(), newContent);
        if (peerInfo.isAuth())
            addedPost.addSignature(peerInfo.getPrivateKey());
        this.broker.publishPost(addedPost);
        this.peerInfo.notifyNewPost();
    }

    public void deletePost(int postId) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.deletePost(peerInfo.getUsername(), postId);
    }

    public boolean register(String password, InetAddress authAddress, String authPort) {
        String username = this.peerInfo.getUsername();
        authenticator.connectToAuth(authAddress, authPort);
        PrivateKey privateKey = authenticator.requestRegister(username, password);

        if (privateKey != null) { // We got registered
            this.peerInfo.setPrivateKey(privateKey);
            return true;
        }
        else // Already registered
            return this.login(password, authAddress, authPort);
    }

    public boolean login(String password, InetAddress authAddress, String authPort) {
        String username = this.peerInfo.getUsername();
        authenticator.connectToAuth(authAddress, authPort);
        PrivateKey privateKey = authenticator.requestLogin(username, password);

        if(privateKey != null) { // Success
            this.peerInfo.setPrivateKey(privateKey);
            return true;
        }
        else { // Fail
            this.logout();
            return false;
        }
    }

    public void logout(){
        this.peerInfo.logout();
    }

    public void execute(ScheduledThreadPoolExecutor scheduler) {
        this.broker.execute();
        pingNeighFuture = scheduler.scheduleWithFixedDelay(this::pingNeighbours,
                0, PINGNEIGH_DELAY, TimeUnit.MILLISECONDS);
        addNeighFuture = scheduler.scheduleWithFixedDelay(this::addNeighbour,
                0, ADDNEIGH_DELAY, TimeUnit.MILLISECONDS);
        pingSubsFuture = scheduler.scheduleWithFixedDelay(this::pingSubs,
                0, PINGSUBS_DELAY, TimeUnit.MILLISECONDS);
    }

    public void cancelHooks() {
        if (pingNeighFuture != null) pingNeighFuture.cancel(false);
        if (addNeighFuture != null) addNeighFuture.cancel(false);
        if (pingSubsFuture != null) pingSubsFuture.cancel(false);
    }

    public void stop() {
        this.broker.stop();
        this.authenticator.close();
        this.cancelHooks();
        this.context.close();
        this.peerInfo.notifyStop();
    }

    public Timeline requestTimeline(String username) {
        // check if neighbours have the username's timeline
        // TODO Tamos a dar flooding atm, dps temos que usar searches
        List<Neighbour> neighbours = peerInfo.getNeighbours().stream().toList();
        if (neighbours.size() == 0)
            return null;

        MessageRequest request = new QueryMessage(username, this.peerInfo);


        Future<List<MessageResponse>> responseFuture = this.sendQueryNeighbours(request, neighbours);

        boolean timed_out = false;
        List<MessageResponse> responses = null;
        try {
            Thread.sleep(RCV_TIMEOUT);
            responses = responseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        broker.removePromise(request.getId());

        if (responses != null && !responses.isEmpty()) {
            // select the most recent timeline
            List<Timeline> timelines = responses.stream().map(m -> ((QueryHitMessage) m).getTimeline()).toList();
            Timeline most_recent = timelines.stream().max(Timeline::compareTo).get();
            // save requested timeline
            this.addTimeline(most_recent);

            return most_recent;
        }
        return null;
    }

    public boolean requestSub(String username) {
        List<Neighbour> neighbours = peerInfo.getNeighbours().stream().toList();
        if (neighbours.size() == 0)
            return false;

        MessageRequest request = new SubMessage(username, this.peerInfo);
        Future<List<MessageResponse>> responseFuture = this.sendQueryNeighbours(request, neighbours);
        SubHitMessage response = null;
        try {
            response = (SubHitMessage) responseFuture.get(RCV_TIMEOUT, TimeUnit.MILLISECONDS).get(0);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            response = null;
        }

        // add subscription
        if (response != null) {
            this.broker.subscribe(username, response.getAddress(), response.getPublishPort(), response.getPort());
            this.peerInfo.addSubscription(username); // register subscription in peerInfo
            this.peerInfo.notifyNewSub(response.getPort());

            // create timeline for subscription if not exists
            TimelineInfo myTimelineInfos = this.peerInfo.getTimelineInfo();
            if (!myTimelineInfos.hasTimeline(username)){
                Timeline timeline = new Timeline(username, myTimelineInfos.getClockOffset());
                this.addTimeline(timeline);
            }
            System.out.println(this.peerInfo.getUsername() + " SUBBED TO " + username + " ON " + response.getPort());
            return true;
        } else {
            System.out.println(this.peerInfo.getUsername() + " COULDN'T SUB TO " + username);
            return false;
        }
    }

    private void pingSubs() {
        // See if any of our subscriptions has died
        Set<String> subsToRetry = new HashSet<>();
        Map<String, Pair<ZMQ.Socket, String>> subscriptions = this.broker.getSocketInfo().getSubscriptions();

        for (String username: subscriptions.keySet()) {
            String port = subscriptions.get(username).p2;
            SubPing request = new SubPing(this.getPeerInfo().getHost());
            Future<List<MessageResponse>> promise = broker.addPromise(request.getId());

            if (!this.sender.sendMessageNTimes(request, port)) {
                subsToRetry.add(username);
                broker.removePromise(request.getId());
                continue;
            }

            try {
                SubPong response = (SubPong) promise.get(RCV_TIMEOUT, TimeUnit.MILLISECONDS).get(0);
                // If a peer can at any time stop providing us with a subscription, we need to check here
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                broker.removePromise(request.getId());
                continue;
            } catch (TimeoutException e) { // Peer timed out
                subsToRetry.add(username);
                broker.removePromise(request.getId());
                continue;
            }
            broker.removePromise(request.getId());
        }

        // Start by removing the dead subscriptions, then try to reconnect them
        this.broker.unsubscribe(subsToRetry);
        // Done because it is faster for broker poller, he will break the poll cycle just once this way

        for (String username: subsToRetry) {
            // TODO Maybe do something with success var, prob schedule another sub request
            boolean success = this.requestSub(username); // Request it again
        }
    }

    public void requestUnsub(String username) {
        broker.unsubscribe(username);
    }

    public Map<String, List<Post>> getPostOfSubscriptions() {
        return broker.popSubMessages();
    }

    private Future<List<MessageResponse>> sendQueryNeighbours(MessageRequest request, List<Neighbour> neighbours) {
        // Get random N neighbours to send
        int[] randomNeighbours = IntStream.range(0, neighbours.size()).toArray();
        int i=0;
        Future<List<MessageResponse>> responseFuture = broker.addPromise(request.getId());
        while (i < randomNeighbours.length && i < MAX_RANDOM_NEIGH) {
            Neighbour n = neighbours.get(i);
            this.sender.sendMessageNTimes(request, n.getPort());
            ++i;
        }
        return responseFuture;
    }

    public void pingNeighbours() {
        // reset bloom filter
        this.peerInfo.resetFilter();

        List<Neighbour> neighbours = this.getPeerInfo().getNeighbours().stream().toList();
        for (Neighbour neighbour: neighbours) { // TODO multithread this, probably with scheduler
            PingMessage pingMessage = new PingMessage(peerInfo.getHost());
            Future<List<MessageResponse>> responseFuture = broker.addPromise(pingMessage.getId());

            this.sender.sendMessageNTimes(pingMessage, neighbour.getPort());
            PongMessage response = null;
            try {
                response = (PongMessage) responseFuture.get(RCV_TIMEOUT, TimeUnit.MILLISECONDS).get(0);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException ignored) {}
            broker.removePromise(pingMessage.getId());

            if (response == null) { // Went offline after n tries
                // System.out.println(peerInfo.getUsername() + " REMOVED " + neighbour.getUsername());
                peerInfo.removeNeighbour(neighbour);
                peerInfo.removeHost(neighbour);
                continue;
            }

            if (!response.isNeighbour) {
                peerInfo.removeNeighbour(neighbour);
                continue;
            }
            if (peerInfo.hasNeighbour(response.sender)) {
                Set<Host> hostCache = response.hostCache;
                // System.out.println(peerInfo.getUsername() + " UPDATED " + neighbour.getUsername());
                peerInfo.updateNeighbour(response.sender);
                peerInfo.updateHostCache(hostCache);
                this.peerInfo.mergeFilter(response.sender);
            }
        }
    }

    public void addNeighbour()  {
        // get higher capacity host not neighbour
        Host candidate = peerInfo.getBestHostNotNeighbour();
        if (candidate == null) return;

        boolean neighboursFull = this.peerInfo.areNeighboursFull();
        Neighbour worstNgbr = null;
        boolean canReplace = false;
        if (neighboursFull) {
            worstNgbr = peerInfo.acceptNeighbour(candidate);
            canReplace = worstNgbr == null;
        }

        if (neighboursFull && !canReplace)
            return; // We can't any neighbour

        PassouBem passouBem = new PassouBem(peerInfo.getHost());
        this.sender.sendMessageNTimes(passouBem, candidate.getPort());
        Future<List<MessageResponse>> promise = broker.addPromise(passouBem.getId());
        PassouBemResponse response;
        try {
            response = (PassouBemResponse) promise.get(RCV_TIMEOUT, TimeUnit.MILLISECONDS).get(0);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        } catch (TimeoutException e) {
            return; // We didn't get response, exit
        }
        broker.removePromise(passouBem.getId());

        if (response.isAccepted()) {
            this.peerInfo.notifyNewNeighbour(candidate);
            if (!neighboursFull)
                peerInfo.addNeighbour(new Neighbour(candidate));
            else  // Can replace
                peerInfo.replaceNeighbour(worstNgbr, new Neighbour(candidate));
        }
    }

    public double calculateSatisfaction() {
        Set<Neighbour> neighbours = this.peerInfo.getNeighbours();
        int num_neighbours = neighbours.size();

        // limits
        if (num_neighbours < MIN_NGBRS )
            return 0;
        if (this.peerInfo.areNeighboursFull())
            return 1;
        int total = 0;
        for (Neighbour n: neighbours) {
            total += n.getCapacity()/num_neighbours;
        }
        double satisfaction = ((double) total) / this.peerInfo.getCapacity();
        return satisfaction % 1;
    }

    // prints all timelines stored in order
    public void showFeed() {
        this.peerInfo.showFeed();
    }

    public void subscribe(Observer o) {
        this.getPeerInfo().subscribe(o);
        this.sender.subscribe(o);
    }

    public PeerInfo getPeerInfo() {
        return this.peerInfo;
    }

    public boolean isSubscribed(String username) {
        return this.broker.getSocketInfo().isSubscribed(username);
    }

    @Override
    public String toString() {
        return peerInfo.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(peerInfo, peer.peerInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerInfo);
    }

    public void addContentListening() {
        this.peerInfo.addContentListening();
    }

    public void stopContentListening() {
        this.peerInfo.stopContentListening();
    }
}