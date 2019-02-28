package lab1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

	private static DatagramSocket socket;
	private static HashMap<String, String> plates;

	public Server(int port) throws SocketException {
		socket = new DatagramSocket(port);
		
		plates = new HashMap<String, String>(); 
	}

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Syntax: java Server <port_number>");
			return;
		}

		int port = Integer.parseInt(args[0]);

		try {
			Server server = new Server(port);
			server.service();
		}
		catch(SocketException ex) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	public void service() throws IOException {

		int SIZE = 512;
		byte[] buffer = new byte[SIZE];

		while(true) {
			DatagramPacket request = new DatagramPacket(buffer, SIZE);
			socket.receive(request);
			
			InetAddress clientAddress = request.getAddress();
			int clientPort = request.getPort();
			
			String operation = new String(buffer, 0, request.getLength());

			String[] operands = operation.split(" ");
			String res = "";

			if(operands[0].equals("REGISTER")) {
				res = register(operands[1], operands[2]);
			}
			else if(operands[0].equals("LOOKUP")) {
				res = lookup(operands[1]);
			}
			else {
				System.out.println("Invalid operation!");
			}

			DatagramPacket reply = new DatagramPacket(res.getBytes(), res.length(), clientAddress, clientPort);
			socket.send(reply);

		}
	}

	public static String register(String plate_number, String owner_name) {

		System.out.println("register " + plate_number + " " + owner_name);

		String platePattern = "[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}";
		Pattern r = Pattern.compile(platePattern);
		Matcher m = r.matcher(plate_number);

		if(plates.get(plate_number) != null || !m.find() || owner_name.length() > 256) {
			return "-1";
		}

		plates.put(plate_number, owner_name);

		return Integer.toString(plates.size());
	}

	public static String lookup(String plate_number) {

		System.out.println("lookup " + plate_number);
		String owner_name;

		if((owner_name = plates.get(plate_number)) == null) {
			return "NOT_FOUND";
		}

		return plate_number + " " + owner_name;
	}



}
