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

	public String toString(){
		return "Id: " + chunkNo + "\n Size (KBytes): " + bufferSize;
	}
	
	
}
