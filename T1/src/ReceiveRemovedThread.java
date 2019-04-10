import java.util.concurrent.TimeUnit;

public class ReceiveRemovedThread implements Runnable {

	private Peer peer;
	private String header[];

	public ReceiveRemovedThread(byte[] message, Peer peer) {
		this.header = Utils.getHeader(message);
		this.peer = peer;
	}

	@Override
	public void run() {
		System.out.println(peer.getId());	
		System.out.println(Integer.parseInt(header[2]));	

		if(peer.getId() == Integer.parseInt(header[2]))
			return;

		Storage storage = peer.getStorage();
		byte[] fileID = Utils.hexStringToByteArray(header[3]);
		int chunkNo = Integer.parseInt(header[4]);
		boolean decremented = storage.decrementReplicationDegree(fileID, chunkNo);
		Chunk chunk = storage.getChunk(fileID, chunkNo);

		if(!decremented)
			return;

		int desiredReplicationDegree = chunk.getDesiredReplicationDegree();

		if (decremented && storage.getReplicationDegree(fileID, chunkNo) < desiredReplicationDegree) {
			int waitTime = Utils.getRandomNumber(0, 401);
			byte[] chunk_msg = peer.buildPutChunkMessage(peer.getVersion(), peer.getId(), fileID, chunkNo,
					desiredReplicationDegree, chunk);
			System.out.println("SENDING PUTCHUNK...");

			int numReclaimMessages = peer.numChunkMessages(header[3], chunkNo);

			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(peer.numChunkMessages(header[3], chunkNo) != numReclaimMessages)
				return;

			peer.getScheduler().execute(new MessageSenderThread(chunk_msg, "MDB", peer));
			peer.getScheduler().schedule(new ConfirmationCollector(peer, chunk_msg, 1, 1, desiredReplicationDegree),
					1000, TimeUnit.MILLISECONDS);
		}
	}
}
