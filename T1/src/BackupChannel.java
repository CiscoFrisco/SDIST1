import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BackupChannel extends Channel {

	public BackupChannel(String IPaddress, String port) throws IOException {
		super(IPaddress, port);
	}

	@Override
	public void run() {
		System.out.println("aquiiiii");
		MulticastSocket Msocket;
		try {
			Msocket = new MulticastSocket(this.port);
			System.out.println("aqui");
			Msocket.joinGroup(address);
			System.out.println("aqui2");
			byte[] buf = new byte[65 * 1000];

			while (true) {
				System.out.println("isso");
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				Msocket.receive(msgPacket);
				String message = new String(buf, 0, buf.length).trim();
				System.out.println(message);
				Peer.getScheduler().execute(new MessageReceiverThread(message));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}