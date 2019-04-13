
public class ReceiveDeleteThread implements Runnable {

	private Peer peer;
	private String[] header;

	public ReceiveDeleteThread(byte[] message, Peer peer) {
		this.header = Utils.getHeader(message);
		this.peer = peer;
	}

	@Override
	public void run() {
		byte[] fileId = Utils.hexStringToByteArray(header[3]);
		peer.getStorage().deleteChunks(fileId);

		if (peer.getVersion().equals("2.0")) {
			byte[] message = peer.buildAckDeleteMessage(peer.getVersion(), peer.getId(), Integer.parseInt(header[2]),
					fileId);
			peer.getScheduler().execute(new MessageSenderThread(message, "MC", peer));
		}
	}

}
