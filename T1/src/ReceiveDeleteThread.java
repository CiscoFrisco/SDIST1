
public class ReceiveDeleteThread implements Runnable {

	private Peer peer;
	private String message[];
	
	
	public ReceiveDeleteThread(String message[], Peer peer) {
		this.message = message;
		this.peer = peer;
	}

	@Override
	public void run() {
        
        peer.getStorage().deleteChunks(message[3]);

	}

}
