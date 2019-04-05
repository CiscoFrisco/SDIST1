
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
		Storage storage = this.peer.getStorage();

		if (!storage.hasFile(fileId))
			return;

			storage.addConfirmationMessage(fileId, Utils.asciiToNumber(header[4]), Utils.asciiToNumber(header[2]));

		if (storage.contains(fileId, Utils.asciiToNumber(header[4]))) {
			storage.updateNumConfirmationMessages(fileId, Utils.asciiToNumber(header[4]));
		}
	}

}
