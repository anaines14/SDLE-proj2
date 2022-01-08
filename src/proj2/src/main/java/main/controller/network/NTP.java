package main.controller.network;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NTP {
    private Long offsetValue;
    private Long delayValue;

    public NTP() {
        NTPUDPClient client = new NTPUDPClient();

        InetAddress hostAddr;
        TimeInfo info = null;
        try {
            client.open();
            hostAddr = InetAddress.getByName("pt.pool.ntp.org");
            info = client.getTime(hostAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        info.computeDetails(); // compute offset/delay if not already done
        offsetValue = info.getOffset() * 1000000L;
        delayValue = info.getDelay() * 1000000L;

        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();

        System.out.println(" Roundtrip delay(ns)=" + delay
                + ", clock offset(ns)=" + offset); // offset in ms

        client.close();
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
