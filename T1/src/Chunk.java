
public class Chunk {
		
	private String fileId;
	private int chunkNo;
	private byte[] buffer;
	private int bufferSize;
	
	public Chunk(String fileId, int chunkNo, byte[] buffer, int bufferSize) {
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.buffer = buffer;
		this.bufferSize = bufferSize;
	}
}
