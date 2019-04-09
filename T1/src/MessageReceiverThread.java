import java.util.Arrays;

public class MessageReceiverThread implements Runnable {

	private byte[] message;
	private Peer peer;
	private int length;
	private String protocol;

	public MessageReceiverThread(byte[] message, int length, Peer peer, String protocol) {
		this.message = message;
		this.length = length;
		this.peer = peer;
		this.protocol = protocol;
	}

	@Override
	public void run() {

		String messageType = new String(message);
		messageType = messageType.substring(0, messageType.indexOf(" "));

		System.out.println(messageType);
		
		switch (messageType) {
		case "PUTCHUNK":
			peer.getScheduler().execute(new ReceivePutChunkThread(message, length, peer, protocol));
			break;
		case "STORED":
			peer.getScheduler().execute(new ReceiveStoredThread(message, length, peer));
			break;
		case "GETCHUNK":
			peer.getScheduler().execute(new ReceiveGetChunkThread(message, peer, protocol));
			break;
		case "CHUNK":
			peer.getScheduler().execute(new ReceiveChunkThread(message, length, peer));
			break;
		case "DELETE":
			peer.getScheduler().execute(new ReceiveDeleteThread(message, peer));
			break;
		case "REMOVED":
			peer.getScheduler().execute(new ReceiveRemovedThread(message, peer, protocol));
			break;
		default:
			break;
		}
	}
}
