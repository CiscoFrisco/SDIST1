package lab4;

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class Client {

	public static void main(String[] args) {
		if(args.length < 4 || args.length > 5) {
			System.out.println("Syntax: java client <mcast_addr> <mcast_port> <oper> <opnd>*");
			return;
		}

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);

		try {
			InetAddress address = InetAddress.getByName(hostname);
			MulticastSocket Msocket = new MulticastSocket(port);
			Msocket.joinGroup(address);

			byte[] buf = new byte[256];

			//Get advertisement
			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			Msocket.receive(msgPacket);

			String multicast_args = new String(buf, 0, buf.length).trim();
			String[] data = multicast_args.split(":");
			System.out.println("multicast: " + hostname + " " + port + ": " + multicast_args);

			Msocket.close();

			DatagramPacket request;
			DatagramSocket Dsocket = new DatagramSocket();
			InetAddress srvc_address = InetAddress.getByName(data[0]);


			if(args[2].equals("register") && args.length == 5) {
				request = register(args[3], args[4], srvc_address, Integer.parseInt(data[1]));
			}
			else if(args[2].equals("lookup") && args.length == 4) {
				request = lookup(args[3], srvc_address, Integer.parseInt(data[1]));
			}
			else {
				System.out.println("Syntax: java client <mcast_addr> <mcast_port> <oper> <opnd>*");
				Dsocket.close();
				return;
			}

			Dsocket.send(request);


			byte[] buffer = new byte[512];
			DatagramPacket response = new DatagramPacket(buffer, buffer.length);
			Dsocket.receive(response);

			String reply = new String(buffer, 0, response.getLength());
			String output = "";

			if(args[2].equals("register")) {
				output+= "register " + args[3] + " " + args[4] + ": ";

				if(reply.equals("-1")) {
					output+="ERROR";
				}
				else {
					output+=reply;
				}
			}
			else {
				output+= "lookup " + args[3] + ": ";

				if(reply.equals("NOT_FOUND")) {
					output+="ERROR";
				}
				else {
					output+=reply;
				}
			}


			System.out.println(output);
			System.out.println();       

			Dsocket.close();

		}catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static DatagramPacket register(String plate_number, String owner_name, InetAddress address, int port) {
		String message = "REGISTER " + plate_number + " " + owner_name;

		DatagramPacket request = new DatagramPacket(message.getBytes(), message.length(), address, port);

		return request;
	}	

	public static DatagramPacket lookup(String plate_number, InetAddress address, int port) {
		String message = "LOOKUP " + plate_number;

		DatagramPacket request = new DatagramPacket(message.getBytes(), message.length(), address, port);

		return request;
	}
}
