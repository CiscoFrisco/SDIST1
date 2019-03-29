package lab4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

	private static ServerSocket serverSocket;
	private static HashMap<String, String> plates;

	public Server(int port) throws IOException {
		plates = new HashMap<String, String>(); 
		serverSocket = new ServerSocket(port);
	}

	public static void main(String[] args) throws InterruptedException, UnknownHostException {
		if(args.length != 1) {
			System.out.println("Syntax: java Server <srvc_port>");
			return;
		}

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

		while(true) {
			
			Socket server = serverSocket.accept();
			
			DataInputStream in = new DataInputStream(server.getInputStream());
			
			DataOutputStream out = new DataOutputStream(server.getOutputStream());
			
			Thread t = new Handler(this, in, out);
			t.start();
		}
	}

	public String register(String plate_number, String owner_name) {


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

	public String lookup(String plate_number) {

		System.out.println("lookup " + plate_number);
		String owner_name;

		if((owner_name = plates.get(plate_number)) == null) {
			return "NOT_FOUND";
		}

		return plate_number + " " + owner_name;
	}
}

class Handler extends Thread {
	
	private Server server;
	private DataInputStream dis;
	private DataOutputStream dos;

	public Handler(Server server, DataInputStream dis, DataOutputStream dos) {
		this.server = server;
		this.dis = dis;
		this.dos = dos;
	}
	
	@Override
	public void run() {
		String operation = "";
		try {
			operation = dis.readUTF();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println(operation);

		String[] operands = operation.split(" ");
		String res = "";

		if(operands[0].equals("REGISTER")) {
			res = server.register(operands[1], operands[2]);
		}
		else if(operands[0].equals("LOOKUP")) {
			res = server.lookup(operands[1]);
		}
		else {
			System.out.println("Invalid operation!");
		}
		
		System.out.println(res);

		try {
			dos.writeUTF(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
