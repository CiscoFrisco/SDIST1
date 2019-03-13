import java.net.InetAddress;
import java.net.UnknownHostException;

public class ControlChannel implements Runnable {

    private InetAddress address;
    private int port;

    public ControlChannel(String IPaddress, String port) {
        try {
            this.address = InetAddress.getByName(IPaddress);
            this.port = Integer.parseInt(port);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }

}