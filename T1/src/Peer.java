import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

	private char pathSeparator;

	private ConcurrentHashMap<byte[], ArrayList<Integer>> deleteAcks;

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
			this.storage = Storage.readStorage("peer", this);
		} else {
			this.storage = new Storage(this);
			this.storage.initializeStorage();
		}

		for (Channel channel : channels.values()) {
			this.scheduler.execute(channel);
		}

		this.numChunkMessages = 0;
		this.pathSeparator = Utils.getCharSeparator();

		this.deleteAcks = new ConcurrentHashMap<byte[], ArrayList<Integer>>();

		File tasks = new File("tasks.txt");
		if (tasks.exists()) {

		} else {
			tasks.createNewFile();
		}
	}

	public void readTasks() {
		try (BufferedReader br = new BufferedReader(new FileReader("tasks.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				storage.deleteChunks(line.split(" ")[1].getBytes());
			}

			PrintWriter writer = new PrintWriter("tasks.txt");
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

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
			byte[] chunk_msg = buildPutChunkMessage(this.protocol_version, this.peerID, file.getFileId(), i,
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

		// save fileId to compare when receiving chunks
		this.restoredFile = Utils.bytesToHex(fileId);

		for (int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
			this.latch = new CountDownLatch(1);

			byte[] message = buildGetChunkMessage(protocol_version, peerID, fileId, chunkNo);
			this.scheduler.execute(new MessageSenderThread(message, "MC", this));

			try {
				this.latch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	public String delete(String fileName) throws RemoteException {

		File file = new File(fileName);

		if (!file.exists())
			return "File not found";

		byte[] fileId = StoredFile.encryptFileId(fileName);

		byte[] message = buildDeleteMessage(protocol_version, peerID, fileId);
		this.scheduler.execute(new MessageSenderThread(message, "MC", this));
		
		if(protocol_version != "1.0")
			this.scheduler.schedule(new CollectDeleteAcksThread(fileId, this), Utils.getRandomNumber(401),
				TimeUnit.MILLISECONDS);

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

		String sender = Utils.numberToAscii(senderId);
		byte[] file = Utils.bytesToHex(fileId).getBytes();
		String chunkN = Utils.numberToAscii(chunkNo);
		String rep = Utils.numberToAscii(replicationDegree);

		String begin = "PUTCHUNK " + version + " " + sender + " ";
		byte[] begin_b = begin.getBytes();

		String mid = " " + chunkN + " " + rep + " \r\n\r\n";
		byte[] mid_b = mid.getBytes();

		byte[] chunkContent = chunk.getBuffer();

		byte[] temp = Utils.concatenateArrays(begin_b, file);

		byte[] temp2 = Utils.concatenateArrays(temp, mid_b);

		byte[] message = Utils.concatenateArrays(temp2, chunkContent);

		return message;
	}

	public byte[] buildGetChunkMessage(String version, int senderId, byte[] fileId, int chunkNo) {

		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);
		String message = "GETCHUNK " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n";

		return message.getBytes();

	}

	public byte[] buildStoredMessage(String version, int senderId, byte[] fileId, int chunkNo) {
		String message = "STORED " + version + " " + Utils.numberToAscii(senderId) + " " + Utils.bytesToHex(fileId)
				+ " " + Utils.numberToAscii(chunkNo) + " \r\n\r\n";
		return message.getBytes();

	}

	public byte[] buildChunkMessage(String version, int senderId, byte[] fileId, int chunkNo, Chunk chunk) {
		String sender = Utils.numberToAscii(senderId);
		byte[] file = Utils.bytesToHex(fileId).getBytes();
		String chunkN = Utils.numberToAscii(chunkNo);
		byte[] chunkContent = chunk.getBuffer();

		String begin = "CHUNK " + version + " " + sender + " ";
		byte[] begin_b = begin.getBytes();

		String mid = " " + chunkN + " \r\n\r\n";
		byte[] mid_b = mid.getBytes();

		byte[] temp = Utils.concatenateArrays(begin_b, file);
		byte[] temp2 = Utils.concatenateArrays(temp, mid_b);
		byte[] message = Utils.concatenateArrays(temp2, chunkContent);

		return message;
	}

	public byte[] buildDeleteMessage(String version, int senderId, byte[] fileId) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);

		String message = "DELETE " + version + " " + sender + " " + file + " \r\n\r\n";
		return message.getBytes();
	}

	public byte[] buildAckDeleteMessage(String version, int senderId, int initiatorId, byte[] fileId) {
		String sender = Utils.numberToAscii(senderId);
		String initiator = Utils.numberToAscii(initiatorId);

		String file = Utils.bytesToHex(fileId);

		String message = "ACKDELETE " + version + " " + sender + " " + initiator + " " + file + " \r\n\r\n";
		return message.getBytes();
	}

	public byte[] buildRemovedMessage(String version, int senderId, byte[] fileId, int chunkNo) {
		String sender = Utils.numberToAscii(senderId);
		String file = Utils.bytesToHex(fileId);
		String chunkN = Utils.numberToAscii(chunkNo);

		String message = "REMOVED " + version + " " + sender + " " + file + " " + chunkN + " \r\n\r\n";
		return message.getBytes();
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

	public char getPathSeparator() {
		return pathSeparator;
	}

	public void addAckMesssage(byte[] fileId, int peerId) {
		ArrayList<Integer> newList = deleteAcks.get(fileId);
		newList.add(peerId);

		deleteAcks.replace(fileId, newList);
	}

	public void putIdlePeersTasks(byte[] fileId) {
		File folder = new File(".");

		for (File file : folder.listFiles()) {
			String name = file.getName();
			if (file.isDirectory() && file.getName().contains("peer")) {
				int id = Character.getNumericValue(name.charAt(4));

				if (!deleteAcks.get(fileId).contains(id))
					putTask(fileId, id);
			}
		}
	}

	public void putTask(byte[] fileId, int peerId) {
		String message = "DELETE " + new String(fileId);

		try {
			Files.write(Paths.get("peer" + peerId + "/tasks.txt"), message.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
