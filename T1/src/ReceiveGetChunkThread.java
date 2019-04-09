import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceiveGetChunkThread implements Runnable {

	private Peer peer;
	private String header[];
	private String protocol;

	public ReceiveGetChunkThread(byte[] message, Peer peer, String protocol) {
		this.header = Utils.getHeader(message);
		this.peer = peer;
		this.protocol = protocol;
	}

	@Override
	public void run() {
		int waitTime = Utils.getRandomNumber(401);
		int chunkNo = Utils.asciiToNumber(header[4]);
		
		byte[] fileId = Utils.hexStringToByteArray(header[3]);
		if(peer.getStorage().contains(fileId, chunkNo)) {
			Chunk chunk = peer.getStorage().getChunk(fileId, chunkNo);
			byte[] msg = peer.buildChunkMessage(peer.getVersion(), peer.getId(), fileId, chunkNo, chunk);
			peer.getScheduler().schedule(new MessageSenderThread(msg,"MDR",peer, protocol), waitTime, TimeUnit.MILLISECONDS);
		}

	}

}
