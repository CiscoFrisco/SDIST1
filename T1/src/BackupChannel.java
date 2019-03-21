import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BackupChannel implements Runnable {

	private InetAddress address;
	private int port;

	public BackupChannel(String IPaddress, String port) throws IOException {

		try {
			this.address = InetAddress.getByName(IPaddress);
			this.port = Integer.parseInt(port);
		}
		catch (IOException ex) {
				ex.printStackTrace();
		}
	}
	
	public void sendMessage(String msg) {
		try {
			DatagramSocket socket = new DatagramSocket();
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

			byte[] buf = new byte[256];

			while(true){
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				Msocket.receive(msgPacket);
				
				
				System.out.println("HEEEY" + msgPacket);
			}
			//Get advertisement
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}