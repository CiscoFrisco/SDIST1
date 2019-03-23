import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Storage implements Serializable {
	
	private HashMap<Chunk, Integer> chunks;
	private ArrayList<StoredFile> storedFiles;
	private int peerId;
	
	public Storage(int peerId) {
		this.chunks = new HashMap<Chunk, Integer>();
		this.storedFiles = new ArrayList<StoredFile>();

		this.peerId = peerId;
	}

	public void addChunk(Chunk chunk, int numConfirmationMessagess){
		this.chunks.put(chunk, numConfirmationMessagess);
	}
	
	public boolean contains(String[] header) {
		
		for(Chunk chunk : chunks.keySet()) {
			if(chunk.getFileId() == header[3] && chunk.getChunkNo() == Integer.parseInt(header[4]))
				return true;
		}
		
		return false;
	}

	public void addFile(StoredFile file){
		this.storedFiles.add(file);
	}
	
	public void serialize() {
		 try {
	         FileOutputStream fileOut =
	         new FileOutputStream("/tmp/peerStorage" + peerId +".ser");
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
			FileInputStream fileIn = new FileInputStream("/tmp/peerStorage" + peerId +".ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			storage = (Storage) in.readObject();
			in.close();
			fileIn.close();

			return storage;
		 } catch (IOException i) {
			i.printStackTrace();
			return null;
		 } catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return null;
		 }
	}

}
