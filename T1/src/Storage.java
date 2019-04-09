import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Storage {

	private ConcurrentHashMap<Chunk, Integer> chunks;
	private ConcurrentHashMap<String, byte[]> restoredChunks;
	private ArrayList<StoredFile> storedFiles;
	private int capacity;
	private Peer peer;
	private ConcurrentHashMap<String, Integer> reclaimedChunks;
	private ConcurrentHashMap<String, ArrayList<Integer>> confirmationMessages;
	private String backupPath;
	private String restorePath;

	public Storage(Peer peer) {
		this.chunks = new ConcurrentHashMap<Chunk, Integer>();
		this.restoredChunks = new ConcurrentHashMap<String, byte[]>();
		this.reclaimedChunks = new ConcurrentHashMap<String, Integer>();
		this.storedFiles = new ArrayList<StoredFile>();
		this.confirmationMessages = new ConcurrentHashMap<String, ArrayList<Integer>>();

		this.capacity = 2000 * 1000; // 2000 KBytes

		this.peer = peer;
		this.backupPath = "peer" + peer.getId() + Utils.getCharSeparator() + "backup" + Utils.getCharSeparator();
		this.restorePath = this.backupPath.replace("backup", "restore");

	}

	public Storage(Peer peer, ConcurrentHashMap<Chunk, Integer> chunks) {
		this.chunks = chunks;
		this.storedFiles = new ArrayList<StoredFile>();
		this.restoredChunks = new ConcurrentHashMap<String, byte[]>();
		this.reclaimedChunks = new ConcurrentHashMap<String, Integer>();

		this.peer = peer;

		this.backupPath = "peer" + peer.getId() + Utils.getCharSeparator() + "backup" + Utils.getCharSeparator();
		this.restorePath = this.backupPath.replace("backup", "restore");
	}

	public void addConfirmationMessage(byte[] fileId, int chunkNo, int peerId) {

		String key = Utils.bytesToHex(fileId) + "-" + chunkNo;
		ArrayList<Integer> list;

		if(confirmationMessages.containsKey(key)){
			list = confirmationMessages.get(key);
			list.add(peerId);
			confirmationMessages.replace(key, list);
		}
		else{
			list = new ArrayList<Integer>();
			list.add(peerId);
			confirmationMessages.put(key, list);
		}

		for (StoredFile file : storedFiles) {
			if (Arrays.equals(file.getFileId(), fileId)) {
				file.addConfirmationMessage(chunkNo, peerId);
				break;
			}
		}
	}

	public int getNumConfirmationMessages(byte[] fileId, int chunkNo) {
		return confirmationMessages.get(Utils.bytesToHex(fileId) + "-" + chunkNo).size();
	}

	public void putRestoredChunk(String id, byte[] chunkBody) {
		this.restoredChunks.put(id, chunkBody);
	}

	public int getNumberRestoredChunks(String fileId) {

		int number = 0;

		for (String key : restoredChunks.keySet()) {
			if (key.contains(fileId))
				number++;
		}

		return number;
	}

	public ConcurrentHashMap<Integer, byte[]> getChunks(String fileId) {

		ConcurrentHashMap<Integer, byte[]> chunks = new ConcurrentHashMap<Integer, byte[]>();

		for (Map.Entry<String, byte[]> entry : restoredChunks.entrySet()) {
			String key = entry.getKey();
			if (key.contains(fileId)) {
				chunks.put(Integer.parseInt(key.substring(key.indexOf("-") + 1)), entry.getValue());
			}
		}

		return chunks;
	}

	public void restoreFile(String fileId, String fileName) {
		// TODO: windows vs linux filepaths
		char separator = peer.getPathSeparator();
		String path = restorePath + fileName;
		File file = new File(path);
		ConcurrentHashMap<Integer, byte[]> chunks = getChunks(fileId);

		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			for (byte[] bytes : chunks.values()) {
				bos.write(bytes);
			}

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initializeStorage() {
		String path = "peer" + peer.getId();

		File directory = new File(path);
		File backup = new File(backupPath);
		File restore = new File(restorePath);

		directory.mkdir();
		backup.mkdir();
		restore.mkdir();
	}

	public ConcurrentHashMap<Chunk, Integer> getChunks() {
		return chunks;
	}

	public String getChunksInfo() {
		String info = "";

		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			info += entry.getKey().toString() + "\nPerceived replication degree: " + entry.getValue() + '\n';
		}

		return info;
	}

	public int getUsedSpace() {
		int space = 0;

		File backup = new File(backupPath);

		for (File fileFolder : backup.listFiles()) {
			for (File chunk : fileFolder.listFiles()) {
				space += chunk.length();
			}
		}

		return space / 1000;
	}

	public String getStorageInfo() {
		String info = "STORAGE INFO\n Storage capacity: " + capacity / 1000 + " KBytes\n";

		info += " Amount of storage used for chunks: " + getUsedSpace() + " KBytes";

		return info;
	}

	public boolean hasFile(byte[] fileId) {
		for (StoredFile storedFile : storedFiles) {
			if (Arrays.equals(storedFile.getFileId(), fileId)) {
				return true;
			}
		}

		return false;
	}

	public void addReclaimedChunk(String fileId, int chunkNo) {
		reclaimedChunks.put(fileId, chunkNo);
	}

	public boolean hasReclaimedChunk(String fileId, int chunkNo) {
		return reclaimedChunks.get(fileId).equals(chunkNo);
	}

	public void addChunk(Chunk chunk) {
		this.chunks.put(chunk, 1);

		byte[] fileId = chunk.getFileId();

		File filedir = new File(backupPath + Utils.bytesToHex(fileId));

		if (!filedir.exists()) {
			filedir.mkdir();
		}

		chunk.serialize(backupPath.concat(Utils.bytesToHex(fileId)));
	}

	public Chunk getChunk(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(), fileId)) {
				return key;
			}
		}

		return null;
	}

	public Chunk getChunksFromFile(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(), fileId)) {
				return key;
			}
		}

		return null;
	}

	public int getReplicationDegree(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(), fileId)) {
				return entry.getValue();
			}
		}

		return -1;
	}

	public boolean contains(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(), fileId)) {
				return true;
			}
		}

		return false;
	}

	public void updateNumConfirmationMessages(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(), fileId)) {
				chunks.replace(key, entry.getValue() + 1);
				return;
			}
		}
	}

	public boolean decrementReplicationDegree(byte[] fileId, int chunkNo) {

		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk chunk = entry.getKey();
			int value = entry.getValue();
			if (Arrays.equals(chunk.getFileId(), fileId) && chunk.getChunkNo() == chunkNo) {

				chunks.replace(chunk, value);
				return true;
			}
		}

		return false;
	}

	public void addFile(StoredFile file) {
		this.storedFiles.add(file);
	}

	public static Storage readStorage(String path, Peer peer) {
		ConcurrentHashMap<Chunk, Integer> chunks = new ConcurrentHashMap<Chunk, Integer>();

		File backup = new File(path + peer.getId() + Utils.getCharSeparator() + "backup");

		if (backup.listFiles() != null)
			for (File fileEntry : backup.listFiles()) {
				for (File entry : fileEntry.listFiles()) {
					Chunk chunk = Chunk.deserialize(entry.getPath());
					// TODO: o que fazer com replication degree
					chunks.put(chunk, 1);
					peer.incNumChunksStored(Utils.bytesToHex(chunk.getFileId()) + "-" + chunk.getChunkNo());
				}
			}

		return new Storage(peer, chunks);
	}

	/**
	 * @param chunks the chunks to set
	 */
	public void setChunks(ConcurrentHashMap<Chunk, Integer> chunks) {
		this.chunks = chunks;
	}

	/**
	 * @return the storedFiles
	 */
	public ArrayList<StoredFile> getStoredFiles() {
		return storedFiles;
	}

	/**
	 * @param storedFiles the storedFiles to set
	 */
	public void setStoredFiles(ArrayList<StoredFile> storedFiles) {
		this.storedFiles = storedFiles;
	}

	public void deleteChunks(byte[] fileId) {
		chunks.entrySet().removeIf(entry -> Arrays.equals(entry.getKey().getFileId(), fileId));

		File folder = new File(backupPath + Utils.bytesToHex(fileId));
		String[] entries = folder.list();
		for (String s : entries) {
			File currentFile = new File(folder.getPath(), s);
			currentFile.delete();
		}

		folder.delete();
	}

	public void deleteChunk(Chunk chunk) {
		this.chunks.remove(chunk);
		char separator = peer.getPathSeparator();

		File file = new File(backupPath
				+ Utils.bytesToHex(chunk.getFileId()) + separator + "chk" + chunk.getChunkNo() + ".ser");
		file.delete();
	}

	public void reclaim(int space) {
		capacity = space * 1000;

		int spaceToReclaim = getUsedSpace() - capacity;

		if (spaceToReclaim <= 0)
			return;

		List<Chunk> sortedChunks = new ArrayList<Chunk>(this.chunks.keySet());

		Collections.sort(sortedChunks, Comparator.comparing(Chunk::getBufferSize));
		Collections.reverse(sortedChunks);
		
		for (Iterator<Chunk> i = sortedChunks.iterator(); spaceToReclaim > 0;) {
			Chunk chunk = (Chunk) i.next();
			deleteChunk(chunk);
			byte[] message = this.peer.buildRemovedMessage(peer.getVersion(), peer.getId(), chunk.getFileId(),
					chunk.getChunkNo());
			this.peer.getScheduler().execute(new MessageSenderThread(message, "MC", this.peer, "RECLAIM"));

			spaceToReclaim -= chunk.getBufferSize();
		}
	}

	public boolean isAvailable() {
		return capacity > getUsedSpace();
	}
}
