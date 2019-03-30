import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Chunk implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8102200653806457589L;
	private String fileId;
	private int chunkNo;
	private byte[] buffer;
	private int bufferSize;
	private int desiredReplicationDegree;

	public Chunk(String fileId, int chunkNo, byte[] buffer, int bufferSize, int desiredReplicationDegree) {
		this.setFileId(fileId);
		this.setChunkNo(chunkNo);
		this.setBuffer(buffer);
		this.setBufferSize(bufferSize);
		this.setDesiredReplicationDegree(desiredReplicationDegree);
	}

	/**
	 * @return the desiredReplicationDegree
	 */
	public int getDesiredReplicationDegree() {
		return desiredReplicationDegree;
	}

	/**
	 * @param desiredReplicationDegree the desiredReplicationDegree to set
	 */
	public void setDesiredReplicationDegree(int desiredReplicationDegree) {
		this.desiredReplicationDegree = desiredReplicationDegree;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String toString() {
		return "Id: " + chunkNo + "\n Size (KBytes): " + bufferSize;
	}

	public void serialize(String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path + "/chk" + chunkNo + ".ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in /tmp/employee.ser");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static Chunk deserialize(String path) {
		Chunk chunk = null;
		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			chunk = (Chunk) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Chunk class not found");
			c.printStackTrace();
			return null;
		}

		return chunk;
	}

}
