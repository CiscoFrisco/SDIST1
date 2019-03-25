
public class ReceiveChunkThread implements Runnable {

	private Peer peer;
	private String message;
	
	
	public ReceiveChunkThread(String message, Peer peer) {
		this.message = message;
		this.peer = peer;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
