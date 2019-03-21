import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Peer implements RemoteInterface {
	
	private int PeerID;
	private static ControlChannel MC;
	private static BackupChannel MDB;
	private static ScheduledThreadPoolExecutor scheduler;
	
	public static ScheduledThreadPoolExecutor getScheduler() {
		return scheduler;
	}
	
	public static ControlChannel getMC() {
		return MC;
	}
	
	public static BackupChannel getMDB() {
		return MDB;
	}

	public Peer(int PeerID, String MCaddress, String MCport, String MDBaddress, String MDBport) throws IOException {
		this.PeerID = PeerID;
		this.MC = new ControlChannel(MCport, MCaddress);
		this.MDB = new BackupChannel(MDBport, MDBaddress);

		this.scheduler = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
	}

	public static void main(String[] args) {

		// if(args.length != 9){
		// System.err.println("Usage: java Peer <protocol_version> <server_id>
		// <remote_object_name> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress>
		// <MDRport>");
		// System.exit(1);
		// }
		int peer_id = Integer.parseInt(args[1]);
		String remote_object_name = args[2];
		String MCaddress = args[3];
		String MCport = args[4];
		String MDBaddress = args[5];
		String MDBport = args[6];

		try {
			Peer obj = new Peer(peer_id, MCaddress, MCport, MDBaddress, MDBport);
			RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Naming.rebind(remote_object_name, stub);

			System.err.println("Peer ready");
		} catch (Exception e) {
			System.err.println("Peer exception: " + e.toString());
			e.printStackTrace();
		}

	}

	public static ArrayList<Chunk> splitFile(File f) throws IOException {
		int partCounter = 1;// I like to name parts from 001, 002, 003, ...
							// you can change it to 0 if you want 000, 001, ...

		int sizeOfFiles = 64 * 1000;// 64KByte
		byte[] buffer = new byte[sizeOfFiles];
		String fileName = f.getName();

		Path file = Paths.get(fileName);
		BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

		String dateModified = attr.lastModifiedTime().toString();
		String owner = Files.getOwner(file).getName();
		String fileId = encryptFileId(fileName, dateModified, owner);
		int chunkNo = 0;
		
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		// try-with-resources to ensure closing stream
		try (FileInputStream fis = new FileInputStream(f); BufferedInputStream bis = new BufferedInputStream(fis)) {

			
			int bytesAmount = 0;
			while ((bytesAmount = bis.read(buffer)) > 0) {
				// write each chunk of data into separate file with different number in name
				String filePartName = String.format("%s.%03d", fileName, partCounter++);
				File newFile = new File(f.getParent(), filePartName);
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					out.write(buffer, 0, bytesAmount);
				}

				chunks.add(new Chunk(fileId, chunkNo, buffer, bytesAmount));
				chunkNo++;
			}
		}
		return chunks;
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
	public String backup(String fileName, int replicationDegree) {
		
		File file = new File(fileName);
		
		try {
			ArrayList<Chunk> chunks = splitFile(file);
			
			for(int i = 0; i < chunks.size();i++) {
				String chunk_msg = buildChunkMsg("PUTCHUNK", chunks.get(i), replicationDegree);
				this.scheduler.execute(new MessageSenderThread(chunk_msg, "MDB"));
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public String buildChunkMsg(String Action, Chunk chunk, int replicationDegree) {
		
		String chunk_msg = Action + " " + this.PeerID + " " + chunk.getFileId() + " " + chunk.getChunkNo()
		      + " " + Integer.toString(replicationDegree) + " \r\n " + chunk.getBuffer(); 
		
		return chunk_msg;	
	}
}
