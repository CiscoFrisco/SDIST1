import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.Map;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements RemoteInterface {

	private int peerID;

	private ConcurrentHashMap<String, Channel> channels;

	private ScheduledThreadPoolExecutor scheduler;
	private Storage storage;
	private String protocol_version;
	private int numChunkMessages;
	private ConcurrentHashMap<String, Integer> confirmationMessages;
	private CountDownLatch latch;

	private String restoredFile;

	public ScheduledThreadPoolExecutor getScheduler() {
		return scheduler;
	}

	public Channel getChannel(String channel) {
		return channels.get(channel);
	}

	public Peer(String protocol_version, int peerID, String MCaddress, String MCport, String MDBaddress, String MDBport,
			String MDRaddress, String MDRport) throws IOException {
		this.protocol_version = protocol_version;
		this.peerID = peerID;

		this.channels = new ConcurrentHashMap<String, Channel>();
		this.channels.put("MC", new Channel(MCaddress, MCport, this));
		this.channels.put("MDB", new Channel(MDBaddress, MDBport, this));
		this.channels.put("MDR", new Channel(MDRaddress, MDRport, this));
		this.restoredFile = null;

		this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(20);

		if (new File("peer" + peerID).exists()) {
			this.storage = Storage.readStorage("peer", this.peerID);
		} else {
			this.storage = new Storage(this.peerID);
			this.storage.initializeStorage();
		}

		for (Channel channel : channels.values()) {
			this.scheduler.execute(channel);
		}

		this.confirmationMessages = new ConcurrentHashMap<String, Integer>();

		this.numChunkMessages = 0;
	}

	public void addConfirmationMessage(String message, int peer) {
		System.out.println("add: " + message);
		if (!confirmationMessages.containsKey(message)){
			this.confirmationMessages.put(message, peer);
		}
	}

	public int getNumConfirmationMessages(String message) {
		System.out.println(message);
		int num = 0;
		for (String entry : confirmationMessages.keySet()) {
			if (entry.equals(message))
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
		
		File file = new File(fileName);

		if (!file.exists())
			return "File not found";

		byte[] fileId = StoredFile.encryptFileId(fileName);
		int numChunks = (int) Math.ceil((double) file.length() / (64 * 1000));

		//save fileId to compare when receiving chunks
		this.restoredFile = Utils.bytesToHex(fileId);
		
		this.latch = new CountDownLatch(numChunks);
		
		for (int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
			String message = buildGetChunkMessage(protocol_version, peerID, fileId, chunkNo);
			this.scheduler.execute(new MessageSenderThread(message, "MC", this));
		}
		
		try {
			this.latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//storage.restoreFile(Utils.bytesToHex(fileId), fileName);
		return "sup yo";
	}
	
	public void flagChunkReceived() {
		System.out.println("CountingDown");
		this.latch.countDown();
	}
	
	public String getRestoredFile() {
		return this.restoredFile;
	}

	@Override
	public String delete(String fileName) throws RemoteException {

		File file = new File(fileName);

		if (!file.exists())
			return "File not found";

		byte[] fileId = StoredFile.encryptFileId(fileName);

		String message = buildDeleteMessage(protocol_version, peerID, fileId);
		this.scheduler.execute(new MessageSenderThread(message, "MC", this));

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

			state += info;
		}

		state += storage.getChunksInfo();

		state += storage.getStorageInfo();

		return state;
	}

	public String buildPutChunkMessage(String version, int senderId, byte[] fileId, int chunkNo, int replicationDegree,
			Chunk chunk) {

		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);
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

	public String buildGetChunkMessage(String version, int senderId, byte[] fileId, int chunkNo) {

		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);

		return "GETCHUNK " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n";

	}

	public String buildStoredMessage(String version, int senderId, byte[] fileId, int chunkNo) {
		return "STORED " + version + " " + Utils.numberToAscii(senderId) + " " + Utils.bytesToHex(fileId)
				+ " " + Utils.numberToAscii(chunkNo) + " \r\n\r\n";
	}

	public String buildChunkMessage(String version, int senderId, byte[] fileId, int chunkNo, Chunk chunk) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);
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

	public String buildDeleteMessage(String version, int senderId, byte[] fileId) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);

		return "DELETE " + version + " " + sender + " " + file + " \r\n\r\n";
	}

	public String buildRemovedMessage(String version, int senderId, byte[] fileId, int chunkNo) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);

		return "REMOVED " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n";
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

	public void incNumChunkMessages() {
		numChunkMessages++;
	}

	public int numChunkMessages() {
		return numChunkMessages;
	}
}
