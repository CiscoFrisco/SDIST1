import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RestoreChannel extends Channel {

    public RestoreChannel(String IPaddress, String port, Peer peer) {
       super(IPaddress, port, peer);
    }

    @Override
    public void run() {

    }

}