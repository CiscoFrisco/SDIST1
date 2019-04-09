import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivePutChunkThread implements Runnable {
	
	private String[] header;
	private byte[] chunkContent;
	private Peer peer;
	private String protocol;
	
	public ReceivePutChunkThread(byte[] message, int length, Peer peer, String protocol) {

		this.header = Utils.getHeader(message);
		this.protocol = protocol;
		this.chunkContent = Utils.getChunkContent(message, length);
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
		
		this.peer.incNumChunksStored(header[3] + "-" + Utils.asciiToNumber(header[4]));
	
		System.out.println("receive: " + chunkContent.length);
		byte[] stored = peer.buildStoredMessage(peer.getVersion(), peer.getId(), fileId, chunkNo);
		storage.addChunk(new Chunk(fileId, chunkNo, chunkContent, chunkContent.length, replicationDegree));
		int interval = Utils.getRandomNumber(401);
		
		peer.getScheduler().schedule(new MessageSenderThread(stored,"MC", peer, protocol), interval, TimeUnit.MILLISECONDS);
	}

}
