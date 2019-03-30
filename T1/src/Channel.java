import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Channel implements Runnable {
	
	protected InetAddress address;
	protected int port;
	protected Peer peer;

	public Channel(String IPaddress, String port, Peer peer) {
		try {
			this.address = InetAddress.getByName(IPaddress);
			this.port = Integer.parseInt(port);
			this.peer = peer;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String msg) {
		try {
			MulticastSocket socket = new MulticastSocket(this.port);
			DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);

			socket.send(msgPacket);
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}     
	}

	@Override
	public void run() {
		MulticastSocket Msocket;
		try {
			Msocket = new MulticastSocket(this.port);
			Msocket.joinGroup(address);
			byte[] buf = new byte[65 * 1000];

			while (true) {
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				Msocket.receive(msgPacket);
				String message = new String(buf, 0, buf.length).trim();
				peer.getScheduler().execute(new MessageReceiverThread(message, peer));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
