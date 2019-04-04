
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

		String[] split = this.message.split(" \r\n\r\n",2);
		String[] header = split[0].split(" ");

		if(header[3].equals(peer.getRestoredFile())) {
			System.out.println("HEY");
			peer.getStorage().putRestoredChunk(header[3] + "-" + Utils.asciiToNumber(header[4]), split[1]);
			peer.flagChunkReceived();
		}
	}

}
