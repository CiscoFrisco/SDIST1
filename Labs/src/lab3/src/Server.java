

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements RemoteInterface {

	private static HashMap<String, String> plates;
	
	public Server() {
		plates = new HashMap<String, String>(); 
	}

	public String sayHello() {
		return "Hello, world!";
	}

	public static void main(String args[]) {

		if(args.length != 1) {
			System.err.println("Usage: java Server <remote_object_name>");
			System.exit(1);
		}
		
		String remote_object_name = args[0];

		try {
			Server obj = new Server();
			RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
            Naming.rebind(remote_object_name, stub);

			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public String lookup(String plate_number) throws RemoteException {
		System.out.println("lookup " + plate_number);
		String owner_name;

		if((owner_name = plates.get(plate_number)) == null) {
			return "NOT_FOUND";
		}

		return plate_number + " " + owner_name;
	}

	@Override
	public String register(String plate_number, String owner_name) throws RemoteException {
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
}