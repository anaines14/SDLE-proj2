package main.controller.network;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

// Adapted from: https://commons.apache.org/proper/commons-net/examples/ntp/NTPClient.java
public class NTP {
    private static final int nTries=3;
    private Long offsetValue;
    private Long delayValue;

    public NTP() {
        this.delayValue = 0L;
        this.offsetValue = 0L;
    }

    public void updateOffsets() {
        String[] countryTags = {"es", "pt", "fr", "uk", "de", "lu", "it"};
        Random random = new Random();
        InetAddress hostAddr;
        int i = 0;
        TimeInfo info = null;
        while (i < nTries) {
            NTPUDPClient client = new NTPUDPClient();
            try {
                System.out.println("Enter");
                client.open();
                client.setSoTimeout(100);
                String tag = countryTags[random.nextInt(countryTags.length)];
                hostAddr = InetAddress.getByName(tag + ".pool.ntp.org");
                info = client.getTime(hostAddr); // TODO Ter timeout e de X em X tempo tentar again
                break;
            } catch (SocketTimeoutException e) {
                ++i;
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("TIMEOUT");
            client.close();
        }

        if (info != null) {
            info.computeDetails(); // compute offset/delay if not already done
            offsetValue = info.getOffset() * 1000000L;
            delayValue = info.getDelay() * 1000000L;
        }
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
