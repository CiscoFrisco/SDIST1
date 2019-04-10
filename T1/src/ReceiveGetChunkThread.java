import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceiveGetChunkThread implements Runnable {

	private Peer peer;
	private String header[];

	public ReceiveGetChunkThread(byte[] message, Peer peer) {
		this.header = Utils.getHeader(message);
		this.peer = peer;
	}

	@Override
	public void run() {
		int waitTime = Utils.getRandomNumber(200, 401);
		int chunkNo = Integer.parseInt(header[4]);
		
		byte[] fileId = Utils.hexStringToByteArray(header[3]);
		if(peer.getStorage().contains(fileId, chunkNo)) {
			Chunk chunk = peer.getStorage().getChunk(fileId, chunkNo);
			byte[] msg = peer.buildChunkMessage(peer.getVersion(), peer.getId(), fileId, chunkNo, chunk);
				
			int numChunkMessages = peer.numChunkMessages(header[3], chunkNo);
			
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(peer.numChunkMessages(header[3], chunkNo) != numChunkMessages) {
				return;
			}
			
			peer.getScheduler().execute(new MessageSenderThread(msg,"MDR",peer));
		}

	}

}
