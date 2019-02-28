package lab1;

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Client {

	public static void main(String[] args) {
		if(args.length < 4 || args.length > 5) {
			System.out.println("Syntax: java Client <host_name> <port_number> <oper> <opnd>*");
			return;
		}
		
		String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try {
            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket request;
            
            
            if(args[2].equals("register") && args.length == 5) {
            	request = register(args[3], args[4], address, port);
            }
            else if(args[2].equals("lookup") && args.length == 4) {
            	request = lookup(args[3], address, port);
            }
            else {
            	System.out.println("Syntax: java Client <host_name> <port_number> <oper> <opnd>*");
            	socket.close();
            	return;
            }
            
            socket.send(request);
            
           
            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
                        
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
            
            socket.close();
        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
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
