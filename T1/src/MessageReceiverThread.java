import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MessageReceiverThread implements Runnable {

	private byte[] message;
	private Peer peer;
	private int length;

	public MessageReceiverThread(byte[] message, int length, Peer peer) {
		this.message = message;
		this.length = length;
		this.peer = peer;
	}

	@Override
	public void run() {

		String messageType = new String(message);
		messageType = messageType.substring(0, messageType.indexOf(" "));

		System.out.println(messageType);
		int interval = Utils.getRandomNumber(0, 401);

		switch (messageType) {
		case "PUTCHUNK":
			peer.getScheduler().schedule(new ReceivePutChunkThread(message, length, peer), interval, TimeUnit.MILLISECONDS);
			break;
		case "STORED":
			peer.getScheduler().execute(new ReceiveStoredThread(message, length, peer));
			break;
		case "GETCHUNK":
			peer.getScheduler().execute(new ReceiveGetChunkThread(message, peer));
			break;
		case "CHUNK":
			peer.getScheduler().execute(new ReceiveChunkThread(message, length, peer));
			break;
		case "DELETE":
			peer.getScheduler().execute(new ReceiveDeleteThread(message, peer));
			break;
		case "ACKDELETE":
			peer.getScheduler().execute(new ReceiveAckDeleteThread(message, peer));
			break;
		case "ANNOUNCE":
			peer.getScheduler().execute(new ReceiveAnnounceThread(message, peer));
			break;
		case "REMOVED":
			peer.getScheduler().execute(new ReceiveRemovedThread(message, peer));
			break;
		default:
			break;
		}
	}
}
