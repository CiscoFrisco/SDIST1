
public class ReceiveStoredThread implements Runnable {

	private String[] header;
	private Peer peer;

	public ReceiveStoredThread(byte[] message, int length, Peer peer) {
		this.header = Utils.getHeader(message);
		this.peer = peer;
	}

	@Override
	public void run() {

		byte[] fileId = Utils.hexStringToByteArray(header[3]);
		Storage storage = this.peer.getStorage();

		if (!storage.hasFile(fileId))
			return;

			storage.addConfirmationMessage(fileId, Utils.asciiToNumber(header[4]), Utils.asciiToNumber(header[2]));

		if (storage.contains(fileId, Utils.asciiToNumber(header[4]))) {
			storage.updateNumConfirmationMessages(fileId, Utils.asciiToNumber(header[4]));
		}
	}

}
