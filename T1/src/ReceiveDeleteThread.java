
public class ReceiveDeleteThread implements Runnable {

	private Peer peer;
	private String fileId;

	public ReceiveDeleteThread(byte[] message, Peer peer) {
		this.fileId = Utils.getHeader(message)[3];
		this.peer = peer;
	}

	@Override
	public void run() {
		peer.getStorage().deleteChunks(Utils.hexStringToByteArray(fileId));
	}

}
