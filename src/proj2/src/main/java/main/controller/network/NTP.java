package main.controller.network;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;

// Adapted from: https://commons.apache.org/proper/commons-net/examples/ntp/NTPClient.java
public class NTP {
    private Long offsetValue;
    private Long delayValue;

    public NTP() {
        this.delayValue = Long.valueOf(0);
        this.offsetValue = Long.valueOf(0);
//        NTPUDPClient client = new NTPUDPClient();
//
//        InetAddress hostAddr;
//        TimeInfo info = null;
//        try {
//            client.open();
//            hostAddr = InetAddress.getByName("pt.pool.ntp.org");
//            info = client.getTime(hostAddr); // TODO Ter timeout e de X em X tempo tentar again
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        assert info != null;
//        info.computeDetails(); // compute offset/delay if not already done
//        offsetValue = info.getOffset() * 1000000L;
//        delayValue = info.getDelay() * 1000000L;
//        client.close();
    }

    public Long getDelayValue() {
        return delayValue;
    }

    public void setDelayValue(Long delayValue) {
        this.delayValue = delayValue;
    }

    public Long getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(Long offsetValue) {
        this.offsetValue = offsetValue;
    }
}
