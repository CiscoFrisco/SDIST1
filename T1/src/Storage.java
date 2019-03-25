import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Storage implements Serializable {
	
	private HashMap<Chunk, Integer> chunks;
	private ArrayList<StoredFile> storedFiles;
	private int peerId;
	
	public Storage(int peerId) {
		this.chunks = new HashMap<Chunk, Integer>();
		this.storedFiles = new ArrayList<StoredFile>();

		this.peerId = peerId;
	}
	
	public HashMap<Chunk, Integer> getChunks(){
		return chunks;
	}

	public void addChunk(Chunk chunk){
		this.chunks.put(chunk, 1);
	}
	
	public Chunk getChunk(String fileId, int chunkNo) {
		for(Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if(key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				return key;
			}
		}
		
		return null;
	}
	
	public boolean contains(String fileId, int chunkNo) {
		for(Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if(key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void updateNumConfirmationMessages(String fileId, int chunkNo) {
		for(Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if(key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				chunks.replace(key, entry.getValue() + 1);
				return;
			}
		}
	}

	public void addFile(StoredFile file){
		this.storedFiles.add(file);
	}
	
	public void serialize() {
		 try {
	         FileOutputStream fileOut =
	         new FileOutputStream("C:\\Users\\franc\\Desktop\\peerStorage" + peerId +".ser");
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this);
	         out.close();
	         fileOut.close();
	      } catch (IOException i) {
	         i.printStackTrace();
	      }
	}

	public static Storage deserialize(int peerId){

		Storage storage;

		try {
			FileInputStream fileIn = new FileInputStream("C:\\Users\\franc\\Desktop\\peerStorage" + peerId +".ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			storage = (Storage) in.readObject();
			in.close();
			fileIn.close();
			
			System.out.println(storage.getChunks().size());
			for (Map.Entry<Chunk, Integer> entry : storage.getChunks().entrySet()) {
			    System.out.println(((Chunk) entry.getKey()).getChunkNo() + ", " + entry.getValue());
			}
			
			return storage;
		 } catch (IOException i) {
			i.printStackTrace();
			return null;
		 } catch (ClassNotFoundException c) {
			System.out.println("Storage class not found");
			c.printStackTrace();
			return null;
		 }
	}

}
