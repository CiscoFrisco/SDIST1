
public class ReceiveChunkThread implements Runnable {

	private Peer peer;
	private String message;
	
	
	public ReceiveChunkThread(String message, Peer peer) {
		this.message = message;
		this.peer = peer;
	}

	@Override
	public void run() {
		peer.incNumChunkMessages();

		String[] split = this.message.split(" \r\n\r\n");

		peer.getStorage().putRestoredChunk(split[3] + "-" + split[4], split[5]);
	}

}
