import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivePutChunkThread implements Runnable {
	
	private String[] header;
	private byte[] chunkContent;
	private Peer peer;
	
	public ReceivePutChunkThread(byte[] message, int length, Peer peer) {

		this.header = Utils.getHeader(message);

		this.chunkContent = Utils.getChunkContent(message, length);
		this.peer = peer;
	}

	@Override
	public void run() {
		int senderId = Utils.asciiToNumber(header[2]);

		byte[] fileId = Utils.hexStringToByteArray(header[3]);

		int chunkNo = Utils.asciiToNumber(header[4]);
		int replicationDegree = Utils.asciiToNumber(header[5]);

		// A peer cant store the chunks of its own files
		if(peer.getId() == senderId) {
			return;
		}
				
		// If this peer already stored this chunk
		if(peer.getStorage().contains(fileId, chunkNo)) {
			return;
		}
		System.out.println("receive: " + chunkContent.length);
		byte[] stored = peer.buildStoredMessage(peer.getVersion(), peer.getId(), fileId, chunkNo);
		peer.getStorage().addChunk(new Chunk(fileId, chunkNo, chunkContent, chunkContent.length, replicationDegree));
		int interval = Utils.getRandomNumber(401);
		
		peer.getScheduler().schedule(new MessageSenderThread(stored,"MC", peer), interval, TimeUnit.MILLISECONDS);
	}

}
