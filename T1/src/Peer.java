import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements RemoteInterface {

	private int peerID;

	private ControlChannel MC;
	private BackupChannel MDB;
	private RestoreChannel MDR;

	private ScheduledThreadPoolExecutor scheduler;
	private Storage storage;
	private String protocol_version;

	private ConcurrentHashMap<String, Integer> confirmationMessages;

	public ScheduledThreadPoolExecutor getScheduler() {
		return scheduler;
	}

	public ControlChannel getMC() {
		return MC;
	}

	public BackupChannel getMDB() {
		return MDB;
	}

	public Peer(String protocol_version, int peerID, String MCaddress, String MCport, String MDBaddress, String MDBport,
			String MDRaddress, String MDRport) throws IOException {
		this.protocol_version = protocol_version;
		this.peerID = peerID;
		this.MC = new ControlChannel(MCaddress, MCport, this);
		this.MDB = new BackupChannel(MDBaddress, MDBport, this);
		this.MDR = new RestoreChannel(MDRaddress, MDRport, this);

		this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

		if (new File("C:\\Users\\franc\\Desktop\\peerStorage" + peerID + ".ser").isFile()) {
			this.storage = Storage.deserialize(this.peerID);
		} else
			this.storage = new Storage(this.peerID);

		this.scheduler.execute(this.MC);
		this.scheduler.execute(this.MDB);
		this.scheduler.execute(this.MDR);

		this.confirmationMessages = new ConcurrentHashMap<String, Integer>();

		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
	}

	public void addConfirmationMessage(String message, int peer) {

		if (!confirmationMessages.containsKey(message))
			this.confirmationMessages.put(message, peer);
	}

	public int getNumConfirmationMessages(String message) {

		int num = 0;
		for (String entry : confirmationMessages.keySet()) {
			if (entry == message)
				num++;
		}

		return num;
	}

	public static void main(String[] args) {

		if (args.length != 9) {
			System.err.println(
					"Usage: java Peer <protocol_version> <server_id> <remote_object_name> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>");
			System.exit(1);
		}

		String remote_object_name = args[2];

		try {
			Peer obj = new Peer(args[0], Integer.parseInt(args[1]), args[3], args[4], args[5], args[6], args[7],
					args[8]);
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
		StoredFile file = new StoredFile(fileName, replicationDegree);
		this.storage.addFile(file);

		ArrayList<Chunk> chunks = new ArrayList<Chunk>();

		try {
			chunks = file.splitFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < chunks.size(); i++) {
			String chunk_msg = buildPutChunkMessage(this.protocol_version, this.peerID, file.getFileId(), i,
					replicationDegree, chunks.get(i));
			this.scheduler.execute(new MessageSenderThread(chunk_msg, "MDB", this));
			this.scheduler.schedule(new ConfirmationCollector(this, chunk_msg, 1, 1, replicationDegree), 1,
					TimeUnit.SECONDS);
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

		ArrayList<StoredFile> files = storage.getStoredFiles();
		String state = "";

		for (StoredFile file : files) {
			String info = file.toString();

			state+=info;
		}

		state+=storage.getChunksInfo();

		return state;
	}

	public String buildPutChunkMessage(String version, int senderId, String fileId, int chunkNo, int replicationDegree,
			Chunk chunk) {

		String sender = Utils.numberToAscii(senderId);
		String file = Utils.fileIdToAscii(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);
		String rep = Utils.numberToAscii(replicationDegree);
		String chunkContent = "";

		try {
			chunkContent = new String(chunk.getBuffer(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "PUTCHUNK " + version + " " + sender + " " + file + " " + chunkN + " " + rep + " \r\n\r\n"
				+ chunkContent;
	}

	public String buildGetChunkMessage(String version, int senderId, String fileId, int chunkNo) {

		String sender = Utils.numberToAscii(senderId);
		String file = Utils.fileIdToAscii(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);

		return "GETCHUNK " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n";

	}

	public String buildStoredMessage(String version, int senderId, String fileId, int chunkNo) {
		return "STORED" + version + " " + Utils.numberToAscii(senderId) + " " + Utils.fileIdToAscii(fileId) + " "
				+ Utils.numberToAscii(chunkNo) + " \r\n\r\n";
	}

	public String buildChunkMessage(String version, int senderId, String fileId, int chunkNo, Chunk chunk) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.fileIdToAscii(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);
		String chunkContent = "";

		try {
			chunkContent = new String(chunk.getBuffer(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "CHUNK " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n" + chunkContent;
	}

	public String buildDeleteMessage(String version, int senderId, String fileId) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.fileIdToAscii(fileId);

		return "DELETE " + version + " " + sender + " " + file + " \r\n\r\n";
	}

	public String buildRemovedMessage(String version, int senderId, String fileId, int chunkNo) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.fileIdToAscii(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);

		return "REMOVED " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n";
	}

	public RestoreChannel getMDR() {
		return MDR;
	}

	public int getId() {
		return peerID;
	}

	public Storage getStorage() {
		// TODO Auto-generated method stub
		return storage;
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return protocol_version;
	}
}
