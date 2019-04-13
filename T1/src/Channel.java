import java.io.IOException;
import java.net.DatagramPacket;
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
	
	public void sendMessage(byte[] msg) {
		try {
			MulticastSocket socket = new MulticastSocket(this.port);
			DatagramPacket msgPacket = new DatagramPacket(msg, msg.length, address, port);

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

			while (true) {
				byte[] buf = new byte[65 * 1000];
				
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				Msocket.receive(msgPacket);
				int length = 0;
				for(int i = buf.length - 1; i > 0; i--){
					if (buf[i] != 0){
						length = i + 1;
						break;
					}

				}

				peer.getScheduler().execute(new MessageReceiverThread(buf, length, peer));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
