

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	private Client() {}

	public static void main(String[] args) {
		
		if(args.length < 4 || args.length > 5) {
			System.err.println("Usage: java Client <host_name> <remote_object_name> <oper> <opnd>");
			System.exit(1);
		}
		
		String host_name = args[0];
		String remote_object_name = args[1];
		
		try {
			Registry registry = LocateRegistry.getRegistry(host_name);
			RemoteInterface stub = (RemoteInterface) registry.lookup(remote_object_name);
			String response;
			
			if(args[2].equals("register") && args.length == 5) {
				response = stub.register(args[3], args[4]);
			}
			else if(args[2].equals("lookup") && args.length == 4) {
				response = stub.lookup(args[3]);
			}
			else {
				System.err.println("Usage: java Client <host_name> <remote_object_name> <oper> <opnd>");
				return;
			}
			
			System.out.println("response: " + response);

		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
