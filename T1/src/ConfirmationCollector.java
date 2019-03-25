import java.util.concurrent.TimeUnit;

public class ConfirmationCollector implements Runnable {

	private int timeout;
	private String chunk_msg;
	private final int MAX_TRIES = 5;
	private int numTries;
	private int replicationDegree;
	private Peer peer;
	
	public ConfirmationCollector(Peer peer, String chunk_msg, int timeout, int numTries, int replicationDegree) {
		this.peer = peer;
		this.chunk_msg = chunk_msg;
		this.timeout = timeout;
		this.numTries = numTries;
		this.replicationDegree = replicationDegree;
	}

	@Override
	public void run() {
		
		String[] split = Utils.getHeader(chunk_msg).split(" ");
		
		if(this.peer.getNumConfirmationMessages(split[3] + "-" + split[4]) < replicationDegree && numTries < MAX_TRIES) {
			peer.getScheduler().execute(new MessageSenderThread(chunk_msg, "MDB", peer));
			peer.getScheduler().schedule(new ConfirmationCollector(peer, chunk_msg, timeout*2, numTries + 1, replicationDegree), timeout*2, TimeUnit.SECONDS);
		}

	}

}
