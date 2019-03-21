import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RestoreChannel extends Channel {

    private InetAddress address;
    private int port;

    public RestoreChannel(String IPaddress, String port) {
       super(IPaddress, port);
    }

    @Override
    public void run() {

    }

}