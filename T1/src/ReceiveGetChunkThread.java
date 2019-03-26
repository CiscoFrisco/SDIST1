import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceiveGetChunkThread implements Runnable {

	private Peer peer;
	private String header[];

	public ReceiveGetChunkThread(String header[], Peer peer) {
		this.header = header;
		this.peer = peer;
	}

	@Override
	public void run() {
	
		int waitTime = Utils.getRandomNumber(401);

		int chunkNo = Integer.parseInt(header[4]);
		String fileId = header[3];
		if(peer.getStorage().contains(fileId, chunkNo)) {

			Chunk chunk = peer.getStorage().getChunk(fileId, chunkNo);

			String msg = peer.buildChunkMessage(peer.getVersion(), peer.getId(), fileId, chunkNo, chunk);
			peer.getScheduler().schedule(new MessageSenderThread(msg,"MDR",peer), waitTime, TimeUnit.MILLISECONDS);
		}

	}

}
