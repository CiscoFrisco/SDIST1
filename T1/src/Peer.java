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

	private int peerID;
	private static ControlChannel MC;
	private static BackupChannel MDB;
	private static ScheduledThreadPoolExecutor scheduler;
	private Storage storage;

	public static ScheduledThreadPoolExecutor getScheduler() {
		return scheduler;
	}

	public static ControlChannel getMC() {
		return MC;
	}

	public static BackupChannel getMDB() {
		return MDB;
	}

	public Peer(int peerID, String MCaddress, String MCport, String MDBaddress, String MDBport) throws IOException {
		this.peerID = peerID;
		this.MC = new ControlChannel(MCport, MCaddress);
		this.MDB = new BackupChannel(MDBport, MDBaddress);

		this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
		this.storage = new Storage(this.peerID);
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

	@Override
	public String backup(String fileName, int replicationDegree) {

		StoredFile file = new StoredFile(fileName);
		this.storage.addFile(file);

		ArrayList<Chunk> chunks = new ArrayList<Chunk>();

		try {
			chunks = file.splitFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < chunks.size(); i++) {
			String chunk_msg = buildPutChunkMessage("1.0", this.peerID, file.getFileId(), i, replicationDegree, chunks.get(i));
			this.scheduler.execute(new MessageSenderThread(chunk_msg, "MDB"));
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

		String chunk_msg = Action + " " + this.peerID + " " + chunk.getFileId() + " " + chunk.getChunkNo() + " "
				+ Integer.toString(replicationDegree) + " \r\n " + chunk.getBuffer();

		return chunk_msg;
	}

	public String fileIdToAscii(String fileId){

		String hex = "";
		for(int i = 0; i < fileId.length(); i++){
			hex.concat(String.format("%04x", (int) fileId.charAt(i)));
		}

		byte[] bytes = hex.getBytes();

		StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
	}

	public String numberToAscii(int number){

		int digit = 0;
		String ascii = "";

		while(number > 0) {
			digit = number % 10;
			digit += 48;
			ascii.concat(Integer.toString(digit));
			number /= 10;
		}

		return ascii;
	}

	public String buildPutChunkMessage(String version, int senderId, String fileId, int chunkNo, int replicationDegree, Chunk chunk){
		return "PUTCHUNK " + version + " " + numberToAscii(senderId) + " " + fileIdToAscii(fileId) + " " + numberToAscii(chunkNo) + " " + numberToAscii(replicationDegree) + "\r\n" + chunk.getBuffer();
	}

	public String buildStoredMessage(String version, int senderId, String fileId, int chunkNo){
		return "STORED" + version + " " + numberToAscii(senderId) + " " + fileIdToAscii(fileId) + " " + numberToAscii(chunkNo);
	}
}
