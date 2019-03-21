import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Storage implements Serializable {
	
	private ArrayList<Chunk> chunks;
	private int peerId;
	
	public Storage(ArrayList<Chunk> chunks, int peerId) {
		this.chunks = chunks;
		this.peerId = peerId;
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

}
