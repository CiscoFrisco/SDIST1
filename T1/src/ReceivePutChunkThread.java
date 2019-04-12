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
		int senderId = Integer.parseInt(header[2]);

		byte[] fileId = Utils.hexStringToByteArray(header[3]);

		int chunkNo = Integer.parseInt(header[4]);
		int replicationDegree = Integer.parseInt(header[5]);
		Storage storage = peer.getStorage();
		System.out.println("chunk: " + chunkNo);
		// A peer cant store the chunks of its own files
		if(peer.getId() == senderId || storage.contains(fileId, chunkNo) || !storage.isAvailable()) {
			return;
		}

		this.peer.incNumReclaimMessages(header[3], Integer.parseInt(header[4]));
	
		System.out.println("receive: " + chunkContent.length);
		byte[] stored = peer.buildStoredMessage(peer.getVersion(), peer.getId(), fileId, chunkNo);
		storage.addChunk(new Chunk(fileId, chunkNo, chunkContent, chunkContent.length, replicationDegree));
		int interval = Utils.getRandomNumber(0, 401);
		
		peer.getScheduler().schedule(new MessageSenderThread(stored,"MC", peer), interval, TimeUnit.MILLISECONDS);
	}

}

