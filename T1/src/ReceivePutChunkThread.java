import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivePutChunkThread implements Runnable {
	
	private String[] header;
	private String chunkContent;
	private Peer peer;
	
	public ReceivePutChunkThread(String[] header, String chunkContent, Peer peer) {
		this.header = header;
		this.chunkContent = chunkContent;
		this.peer = peer;
	}

	@Override
	public void run() {
		int senderId = Utils.asciiToNumber(header[2]);

		byte[] fileId = Utils.hexStringToByteArray(header[3]);

		int chunkNo = Utils.asciiToNumber(header[4]);
		int replicationDegree = Utils.asciiToNumber(header[5]);

		Storage storage = peer.getStorage();

		// A peer cant store the chunks of its own files
		if(peer.getId() == senderId || storage.contains(fileId, chunkNo) || !storage.isAvailable()) {
			return;
		}
				
		String stored = peer.buildStoredMessage(peer.getVersion(), peer.getId(), fileId, chunkNo);
		storage.addChunk(new Chunk(fileId, chunkNo, chunkContent.getBytes(),chunkContent.length(), replicationDegree));
		
		int interval = Utils.getRandomNumber(401);
		
		peer.getScheduler().schedule(new MessageSenderThread(stored,"MC", peer), interval, TimeUnit.MILLISECONDS);
	}

}
