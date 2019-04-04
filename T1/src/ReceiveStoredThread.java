
public class ReceiveStoredThread implements Runnable {

	private String[] header;
	private Peer peer;

	public ReceiveStoredThread(String[] header, Peer peer) {
		this.header = header;
		this.peer = peer;
	}

	@Override
	public void run() {

		byte[] fileId = Utils.hexStringToByteArray(header[3]);


		if (!this.peer.getStorage().hasFile(fileId))
			return;

			this.peer.addConfirmationMessage(header[3] + "-" + header[4], Integer.parseInt(header[2]));

		if (this.peer.getStorage().contains(fileId, Integer.parseInt(header[4]))) {
			this.peer.getStorage().updateNumConfirmationMessages(fileId, Integer.parseInt(header[4]));
		}
	}

}
