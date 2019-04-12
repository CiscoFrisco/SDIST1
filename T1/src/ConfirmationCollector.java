import java.util.concurrent.TimeUnit;

public class ConfirmationCollector implements Runnable {

	private int timeout;
	private byte[] chunk_msg;
	private final int MAX_TRIES = 5;
	private int numTries;
	private int replicationDegree;
	private Peer peer;
	
	public ConfirmationCollector(Peer peer, byte[] chunk_msg, int timeout, int numTries, int replicationDegree) {
		this.peer = peer;
		this.chunk_msg = chunk_msg;
		this.timeout = timeout;
		this.numTries = numTries;
		this.replicationDegree = replicationDegree;
	}

	@Override
	public void run() {
		
		String[] split = new String(chunk_msg).split(" ");
		System.out.println("fds1: " + Integer.parseInt(split[4]));
		if(this.peer.getStorage().getNumConfirmationMessages(Utils.hexStringToByteArray(split[3]), Integer.parseInt(split[4])) < replicationDegree && numTries < MAX_TRIES) {
			System.out.println("fds: " + Integer.parseInt(split[4]));
			peer.getScheduler().execute(new MessageSenderThread(chunk_msg, "MDB", peer));
			peer.getScheduler().schedule(new ConfirmationCollector(peer, chunk_msg, timeout*2, numTries + 1, replicationDegree), timeout*2, TimeUnit.SECONDS);
		}

	}

}
