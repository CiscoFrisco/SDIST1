
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
		int chunkNo = Integer.parseInt(header[4]);

		storage.addConfirmationMessage(fileId, chunkNo, Integer.parseInt(header[2]));

    if (storage.contains(fileId, chunkNo)) {
      storage.updateNumConfirmationMessages(fileId, chunkNo);
    }
  }
}
