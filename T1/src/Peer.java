import java.io.File;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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
	private ConcurrentHashMap<String, Integer> chunkMessages;
	private ConcurrentHashMap<String, Integer> reclaimMessages;

	private CountDownLatch latch;

	private String restoredFile;

	private char pathSeparator;

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

		this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(300);

		if (new File("peer" + peerID).exists()) {
			this.storage = Storage.readStorage("peer", this);
		} else {
			this.storage = new Storage(this);
			this.storage.initializeStorage();
		}

		for (Channel channel : channels.values()) {
			this.scheduler.execute(channel);
		}

		this.pathSeparator = Utils.getCharSeparator();

		this.chunkMessages = new ConcurrentHashMap<String, Integer>();
		this.reclaimMessages = new ConcurrentHashMap<String, Integer>();
		if (this.protocol_version.equals("2.0"))
			this.scheduler.execute(
					new MessageSenderThread(buildAnnounceMessage(this.protocol_version, this.peerID), "MC", this));
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
	public String backup(String fileName, int replicationDegree, boolean enhancement) {

		if ((protocol_version.equals("1.0") && enhancement) || (!protocol_version.equals("1.0") && !enhancement)) {
			return "ERROR: Peer and TestApp are not synchronized. Please try again with the proper protocol version.";
		}

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
			byte[] chunk_msg = buildPutChunkMessage(this.protocol_version, this.peerID, file.getFileId(), i,
					replicationDegree, chunks.get(i));
			this.scheduler.execute(new MessageSenderThread(chunk_msg, "MDB", this));
			this.scheduler.schedule(new ConfirmationCollector(this, chunk_msg, 1, 1, replicationDegree), 1,
					TimeUnit.SECONDS);
		}

		return "sup";
	}

	@Override
	public String restore(String fileName, boolean enhancement) throws RemoteException {
		if ((protocol_version.equals("1.0") && enhancement) || (!protocol_version.equals("1.0") && !enhancement)) {
			return "ERROR: Peer and TestApp are not synchronized. Please try again with the proper protocol version.";
		}

		File file = new File(fileName);

		if (!file.exists())
			return "File not found";

		byte[] fileId = StoredFile.encryptFileId(fileName);
		int numChunks = (int) Math.ceil((double) file.length() / (64 * 1000));

		// save fileId to compare when receiving chunks
		this.restoredFile = Utils.bytesToHex(fileId);

		this.latch = new CountDownLatch(numChunks);

		try {
			ServerSocket serverSocket = new ServerSocket(3003);

			if (protocol_version.equals("2.0")) {
				this.scheduler.execute(new TCPChunkReceiverThread(this, serverSocket, fileId));
			}

			for (int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
				System.out.println("crl: " + chunkNo);
				byte[] message = buildGetChunkMessage(protocol_version, peerID, fileId, chunkNo);
				this.scheduler.execute(new MessageSenderThread(message, "MC", this));
			}

			try {
				this.latch.await();
				if (protocol_version.equals("2.0"))
					serverSocket.close();
				System.out.println("Socket closed");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		fileName = fileName.substring(fileName.lastIndexOf(pathSeparator) + 1);
		storage.restoreFile(Utils.bytesToHex(fileId), fileName);
		return "sup yo";
	}

	public void flagChunkReceived() {
		this.latch.countDown();
	}

	public String getRestoredFile() {
		return this.restoredFile;
	}

	@Override
	public String delete(String fileName, boolean enhancement) throws RemoteException {
		if ((protocol_version.equals("1.0") && enhancement) || (!protocol_version.equals("1.0") && !enhancement)) {
			return "ERROR: Peer and TestApp are not synchronized. Please try again with the proper protocol version.";
		}

		File file = new File(fileName);

		if (!file.exists())
			return "File not found";

		byte[] fileId = StoredFile.encryptFileId(fileName);

		byte[] message = buildDeleteMessage(protocol_version, peerID, fileId);
		this.scheduler.execute(new MessageSenderThread(message, "MC", this));

		return null;
	}

	@Override
	public String reclaim(int space) throws RemoteException {
		storage.reclaim(space);

		return null;
	}

	@Override
	public String state() throws RemoteException {

		ArrayList<StoredFile> files = storage.getStoredFiles();
		String state = "STORED FILES\n";

		for (StoredFile file : files) {
			String info = file.toString();

			state += info;
		}

		state += "STORED CHUNKS\n" + storage.getChunksInfo();

		state += storage.getStorageInfo();

		return state;
	}

	public byte[] buildPutChunkMessage(String version, int senderId, byte[] fileId, int chunkNo, int replicationDegree,
			Chunk chunk) {

		String file = Utils.bytesToHex(fileId);
		byte[] chunkContent = chunk.getBuffer();

		String header = "PUTCHUNK " + version + " " + senderId + " " + file + " " + chunkNo + " " + replicationDegree
				+ " \r\n\r\n";
		byte[] header_b = header.getBytes(StandardCharsets.US_ASCII);

		return Utils.concatenateArrays(header_b, chunkContent);
	}

	public byte[] buildGetChunkMessage(String version, int senderId, byte[] fileId, int chunkNo) {

		String file = Utils.bytesToHex(fileId);

		String message = "GETCHUNK " + version + " " + senderId + " " + file + " " + chunkNo + " \r\n\r\n";

		return message.getBytes(StandardCharsets.US_ASCII);

	}

	public byte[] buildAnnounceMessage(String version, int senderId) {

		String message = "ANNOUNCE " + version + " " + senderId + " \r\n\r\n";

		return message.getBytes(StandardCharsets.US_ASCII);

	}

	public byte[] buildAckDeleteMessage(String version, int senderId, int initiatorId, byte[] fileId) {
		String file = Utils.bytesToHex(fileId);

		String message = "ACKDELETE " + version + " " + senderId + " " + initiatorId + " " + file + " \r\n\r\n";
		return message.getBytes(StandardCharsets.US_ASCII);
	}

	public byte[] buildStoredMessage(String version, int senderId, byte[] fileId, int chunkNo) {
		String message = "STORED " + version + " " + senderId + " " + Utils.bytesToHex(fileId) + " " + chunkNo
				+ " \r\n\r\n";
		return message.getBytes(StandardCharsets.US_ASCII);

	}

	public byte[] buildChunkMessage(String version, int senderId, byte[] fileId, int chunkNo, Chunk chunk) {
		String file = Utils.bytesToHex(fileId);
		byte[] chunkContent = chunk.getBuffer();

		String header = "CHUNK " + version + " " + senderId + " " + file + " " + chunkNo + " \r\n\r\n";
		byte[] header_b = header.getBytes(StandardCharsets.US_ASCII);

		return Utils.concatenateArrays(header_b, chunkContent);
	}

	public byte[] buildDeleteMessage(String version, int senderId, byte[] fileId) {
		String file = Utils.bytesToHex(fileId);

		String message = "DELETE " + version + " " + senderId + " " + file + " \r\n\r\n";
		return message.getBytes(StandardCharsets.US_ASCII);
	}

	public byte[] buildRemovedMessage(String version, int senderId, byte[] fileId, int chunkNo) {
		String file = Utils.bytesToHex(fileId);

		String message = "REMOVED " + version + " " + senderId + " " + file + " " + chunkNo + " \r\n\r\n";
		return message.getBytes(StandardCharsets.US_ASCII);
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

	public void incNumChunkMessages(String fileId, int chunkNo) {

		String key = fileId + "-" + chunkNo;
		Integer value = chunkMessages.get(key);
		if (value != null) {
			chunkMessages.replace(key, value + 1);
		} else {
			chunkMessages.put(key, 1);
		}
	}

	public int numChunkMessages(String fileId, int chunkNo) {
		Integer value = chunkMessages.get(fileId + "-" + chunkNo);

		if (value == null)
			return 0;

		return value;
	}

	public void incNumReclaimMessages(String fileId, int chunkNo) {

		String key = fileId + "-" + chunkNo;
		Integer value = reclaimMessages.get(key);
		if (value != null) {
			reclaimMessages.replace(key, value + 1);
		}
	}

	public void putReclaimMessage(String fileId, int chunkNo) {

		String key = fileId + "-" + chunkNo;
		Integer value = reclaimMessages.get(key);
		if (value != null) {
			reclaimMessages.replace(key, value + 1);
		}
	}

	public int numReclaimMessages(String fileId, int chunkNo) {
		Integer value = reclaimMessages.get(fileId + "-" + chunkNo);

		if (value == null)
			return 0;

		return value;
	}

	public char getPathSeparator() {
		return pathSeparator;
	}
}
