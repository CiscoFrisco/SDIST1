package lab4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

	public static void main(String[] args) {
		if(args.length < 5 || args.length > 6) {
			System.out.println("Syntax: java client <host_name> <port_number> <delay> <oper> <opnd>*");
			return;
		}

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);

		Socket client;
		OutputStream out;
		DataOutputStream output;

		try {
			client = new Socket(hostname, port);
			out = client.getOutputStream();
			output = new DataOutputStream(out);
			
			String request;

			if(args[3].equals("register") && args.length == 6) {
				request = register(args[4], args[5]);
			}
			else if(args[3].equals("lookup") && args.length == 5) {
				request = lookup(args[4]);
			}
			else {
				System.out.println("Syntax: java client <host_name> <port_number> <delay> <oper> <opnd>*");
				return;
			}
			
			output.writeUTF(request);
			
			InputStream in = client.getInputStream();
			DataInputStream input = new DataInputStream(in);

			String reply = input.readUTF();
			String result = "";
			
			if(args[3].equals("register")) {
				result+= "register " + args[4] + " " + args[5] + ": ";

				if(reply.equals("-1")) {
					result+="ERROR";
				}
				else {
					result+=reply;
				}
			}
			else {
				result+= "lookup " + args[4] + ": ";

				if(reply.equals("NOT_FOUND")) {
					result+="ERROR";
				}
				else {
					result+=reply;
				}
			}


			System.out.println(result);
			System.out.println();
			
			try {
				Thread.sleep(Integer.parseInt(args[2]));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			client.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static String register(String plate_number, String owner_name) {
		return "REGISTER " + plate_number + " " + owner_name;
	}	

	public static String lookup(String plate_number) {
		return "LOOKUP " + plate_number;
	}
}
