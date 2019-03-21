import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BackupChannel extends Channel {

	private InetAddress address;
	private int port;

	public BackupChannel(String IPaddress, String port) throws IOException {
		super(IPaddress, port);
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

				Peer.getScheduler().execute(new MessageReceiverThread(msgPacket.getData().toString()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}