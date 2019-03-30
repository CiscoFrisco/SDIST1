
public class ShutdownHook extends Thread {
	
	private Peer peer;
	
	public ShutdownHook(Peer peer) {
		this.peer = peer;
	}
	
	public void run() {
		this.peer.getStorage().save();
	}

}
