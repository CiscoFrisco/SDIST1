import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ControlChannel extends Channel {

	public ControlChannel(String IPaddress, String port, Peer peer) throws IOException {
		super(IPaddress, port, peer);
	}


	@Override
	public void run() {
		
		MulticastSocket Msocket;
		try {
			Msocket = new MulticastSocket(this.port);
			Msocket.joinGroup(address);

			byte[] buf = new byte[256];

			while(true){
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				Msocket.receive(msgPacket);
			}
			//Get advertisement
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}