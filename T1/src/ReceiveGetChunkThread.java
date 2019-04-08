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
		int waitTime = Utils.getRandomNumber(401);
		int chunkNo = Utils.asciiToNumber(header[4]);
		
		byte[] fileId = Utils.hexStringToByteArray(header[3]);
		if(peer.getStorage().contains(fileId, chunkNo)) {
			Chunk chunk = peer.getStorage().getChunk(fileId, chunkNo);
			byte[] msg = peer.buildChunkMessage(peer.getVersion(), peer.getId(), fileId, chunkNo, chunk);

			if(peer.getVersion().equals("1.0")){
				peer.getScheduler().schedule(new MessageSenderThread(msg,"MDR",peer), waitTime, TimeUnit.MILLISECONDS);
			}
			else {
				peer.getScheduler().schedule(new TCPChunkSenderThread(msg, peer), waitTime, TimeUnit.MILLISECONDS);
			}
		}

	}

}
