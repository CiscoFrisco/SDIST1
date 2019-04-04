import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Storage {

	private ConcurrentHashMap<Chunk, Integer> chunks;
	private ConcurrentHashMap<String, String> restoredChunks;
	private ArrayList<StoredFile> storedFiles;
	private int peerId;
	private int capacity;

	public Storage(int peerId) {
		this.chunks = new ConcurrentHashMap<Chunk, Integer>();
		this.restoredChunks = new ConcurrentHashMap<String, String>();
		this.storedFiles = new ArrayList<StoredFile>();

		this.capacity = 2000 * 1000; // 2000 KBytes

		this.peerId = peerId;
	}

	public Storage(int peerId, ConcurrentHashMap<Chunk, Integer> chunks) {
		this.chunks = chunks;
		this.storedFiles = new ArrayList<StoredFile>();

		this.peerId = peerId;
	}

	public void addConfirmationMessage(byte[] fileId, int chunkNo, int peerId) {

		for(StoredFile file : storedFiles){
			if(Arrays.equals(file.getFileId(), fileId)){
				file.addConfirmationMessage(chunkNo, peerId);
				break;
			}
		}
	}

	public int getNumConfirmationMessages(byte[] fileId, int chunkNo) {
		
		for(StoredFile file : storedFiles){
			if(Arrays.equals(file.getFileId(), fileId)){
				return file.getPerceivedReplicationDegree(chunkNo);
			}
		}

		return 0;
	}

	public void putRestoredChunk(String id, String chunkBody) {
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

		for (Map.Entry<String, String> entry : restoredChunks.entrySet()) {
			String key = entry.getKey();
			if (key.contains(fileId)) {
				chunks.put(Integer.parseInt(key.substring(key.indexOf("-") + 1)), entry.getValue().getBytes());
			}
		}

		return chunks;
	}

	public void restoreFile(String fileId, String fileName) {
		File file = new File("peer" + peerId + "/restore/" + fileName);
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
		String path = "peer" + peerId;

		File directory = new File(path);
		File backup = new File(path.concat("/backup"));
		File restore = new File(path.concat("/restore"));

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

		File backup = new File("peer" + peerId + "/backup");

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
			if (Arrays.equals(storedFile.getFileId(), fileId)){
				return true;
			}
		}

		return false;
	}

	public void addChunk(Chunk chunk) {
		this.chunks.put(chunk, 1);

		byte[] fileId = chunk.getFileId();
		String path = "peer" + peerId + "/backup/";

		File filedir = new File(path + Utils.bytesToHex(fileId));

		if (!filedir.exists()) {
			filedir.mkdir();
		}

		chunk.serialize(path.concat(Utils.bytesToHex(fileId)));
	}

	public Chunk getChunk(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(),fileId)) {
				return key;
			}
		}

		return null;
	}

	public Chunk getChunksFromFile(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(),fileId)) {
				return key;
			}
		}

		return null;
	}

	public int getReplicationDegree(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(),fileId)) {
				return entry.getValue();
			}
		}

		return -1;
	}

	public boolean contains(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(),fileId)) {
				return true;
			}
		}

		return false;
	}

	public void updateNumConfirmationMessages(byte[] fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && Arrays.equals(key.getFileId(),fileId)) {
				chunks.replace(key, entry.getValue() + 1);
				return;
			}
		}
	}

	public boolean decrementReplicationDegree(byte[] fileId, int chunkNo) {

		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk chunk = entry.getKey();
			int value = entry.getValue();
			if (Arrays.equals(chunk.getFileId(),fileId) && chunk.getChunkNo() == chunkNo) {


				chunks.replace(chunk, value);
				return true;
			}
		}

		return false;
	}

	public void addFile(StoredFile file) {
		this.storedFiles.add(file);
	}

	public static Storage readStorage(String path, int peerId) {
		ConcurrentHashMap<Chunk, Integer> chunks = new ConcurrentHashMap<Chunk, Integer>();

		File backup = new File(path + peerId + "/backup");

		if (backup.listFiles() != null)
			for (File fileEntry : backup.listFiles()) {
				for (File entry : fileEntry.listFiles()) {
					Chunk chunk = Chunk.deserialize(entry.getPath());
					// TODO: o que fazer com replication degree
					chunks.put(chunk, 1);
				}
			}

		return new Storage(peerId, chunks);
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

	/**
	 * @return the peerId
	 */
	public int getPeerId() {
		return peerId;
	}

	/**
	 * @param peerId the peerId to set
	 */
	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}

	public void deleteChunks(byte[] fileId) {
		chunks.entrySet().removeIf(entry -> Arrays.equals(entry.getKey().getFileId(),fileId));

		File folder = new File("peer" + peerId + "/backup/" + Utils.bytesToHex(fileId));
		String[] entries = folder.list();
		for (String s : entries) {
			File currentFile = new File(folder.getPath(), s);
			currentFile.delete();
		}

		folder.delete();
	}
}
