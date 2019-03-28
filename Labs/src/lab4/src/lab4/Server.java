package lab4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Server {

	private static DatagramSocket socket;
	private static HashMap<String, String> plates;

	public Server(int port) throws SocketException {
		socket = new DatagramSocket(port);
		plates = new HashMap<String, String>(); 
	}

	public static void main(String[] args) throws InterruptedException, UnknownHostException {
		if(args.length != 3) {
			System.out.println("Syntax: java Server <srvc_port> <mcast_addr> <mcast_port> ");
			return;
		}

		int port = Integer.parseInt(args[2]);
		String msg = "localhost:" + args[0];

		final ScheduledThreadPoolExecutor scheduler = 
				(ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);

		final ScheduledFuture<?> advertiser = 
				scheduler.scheduleAtFixedRate(new Advertise("localhost", Integer.parseInt(args[0]), port, msg, args[1]), 2, 1, TimeUnit.SECONDS);

//		scheduler.schedule(new Runnable() {
//
//			@Override
//			public void run() {
//				advertiser.cancel(true);
//				scheduler.shutdown();			
//			}
//		}, 10, TimeUnit.SECONDS);	


		int srvc_port = Integer.parseInt(args[0]);

		try {
			Server server = new Server(srvc_port);
			System.out.println("Oi");
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


		String platePattern = "[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}";
		Pattern r = Pattern.compile(platePattern);
		Matcher m = r.matcher(plate_number);

		if(plates.get(plate_number) != null || !m.find() || owner_name.length() > 256) {
			System.out.println("register " + plate_number + " " + owner_name + " :: -1");
			return "-1";
		}

		plates.put(plate_number, owner_name);
		System.out.println("register " + plate_number + " " + owner_name + " :: " + Integer.toString(plates.size()));

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

	static class Advertise implements Runnable {
		
		private int port;
		private String msg;
		private String address;
		private String srvc_addr;
		private int srvc_port;
		
		public Advertise(String srvc_addr, int srvc_port, int port, String msg, String address) throws UnknownHostException {
			this.port = port;
			this.msg = msg;
			this.address =  address;
			this.srvc_addr = srvc_addr;
			this.srvc_port = srvc_port;
		}

		public void run() {
			try {
				InetAddress addr = InetAddress.getByName(address);
				DatagramSocket serverSocket = new DatagramSocket();
				DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, port);

				serverSocket.send(msgPacket);
				
				System.out.println("multicast: " + this.address + " " + this.port + ": " + this.srvc_addr + " " + this.srvc_port);
				
				serverSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}     
		}
	}

}
