
public class ReceiveChunkThread implements Runnable {

	private Peer peer;
	private String[] header;
	private byte[] chunkContent;


	public ReceiveChunkThread(byte[] message, int length, Peer peer) {
		this.header = Utils.getHeader(message);
		this.chunkContent = Utils.getChunkContent(message, length);
		this.peer = peer;
	}

	@Override
	public void run() {
		int chunkNo = Integer.parseInt(header[4]);
		
		if(peer.getVersion().equals("1.0")) {
			peer.incNumChunkMessages(header[3], chunkNo);
			
			if(header[3].equals(peer.getRestoredFile())) {
				peer.getStorage().putRestoredChunk(header[3] + "-" + chunkNo, chunkContent);
				peer.flagChunkReceived();
			}
		}
	}

}
