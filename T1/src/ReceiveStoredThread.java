
public class ReceiveStoredThread implements Runnable {

	private String[] header;
	private Peer peer;

	public ReceiveStoredThread(String[] header, Peer peer) {
		this.header = header;
		this.peer = peer;
	}

	@Override
	public void run() {

		if (!this.peer.getStorage().hasFile(header[3]))
			return;

		this.peer.addConfirmationMessage(header[3] + "-" + header[4], Integer.parseInt(header[2]));

		if (this.peer.getStorage().contains(header[3], Integer.parseInt(header[4]))) {
			this.peer.getStorage().updateNumConfirmationMessages(header[3], Integer.parseInt(header[4]));
		}
	}

}
