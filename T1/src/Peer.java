import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

public class Peer implements RemoteInterface {

	public static void main(String[] args) {
		
		// if(args.length != 9){
		// 	System.err.println("Usage: java Peer <protocol_version> <server_id> <remote_object_name> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>");
		// 	System.exit(1);
		// }

		String remote_object_name = args[2];

		try {
			Peer obj = new Peer();
			RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
            Naming.rebind(remote_object_name, stub);

			System.err.println("Peer ready");
		} catch (Exception e) {
			System.err.println("Peer exception: " + e.toString());
			e.printStackTrace();
		}

	}

	public static String encryptFileId(String fileName, String dateModified, String owner) {
		return getSHA(fileName + "-" + dateModified + "-" + owner);
	}

	public static String getSHA(String input) {
		try {

			// Static getInstance method is called with hashing SHA
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// digest() method called
			// to calculate message digest of an input
			// and return array of byte
			byte[] messageDigest = md.digest(input.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}

			return hashtext;
		}

		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown" + " for incorrect algorithm: " + e);

			return null;
		}
	}

	@Override
	public String backup(String fileName, int replicationDegree) throws RemoteException {
		return "sup";
	}

	@Override
	public String restore(String fileName) throws RemoteException {
		return null;
	}

	@Override
	public String delete(String fileName) throws RemoteException {
		return null;
	}

	@Override
	public String reclaim(int space) throws RemoteException {
		return null;
	}

	@Override
	public String state() throws RemoteException {
		return null;
	}
}
