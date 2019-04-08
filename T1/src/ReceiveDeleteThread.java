
public class ReceiveDeleteThread implements Runnable {

	private Peer peer;
	private byte[] message;

	public ReceiveDeleteThread(byte[] message, Peer peer) {
		this.message = message;
		this.peer = peer;
	}

	@Override
	public void run() {
		String[] header = Utils.getHeader(message);
		byte[] fileId_b = Utils.hexStringToByteArray(header[3]);
		peer.getStorage().deleteChunks(fileId_b);
		byte[] message = peer.buildAckDeleteMessage(peer.getVersion(), peer.getId(), Utils.asciiToNumber(header[2]) ,fileId_b);
		peer.getScheduler().execute(new MessageSenderThread(message, "MC", peer));
	}

}
