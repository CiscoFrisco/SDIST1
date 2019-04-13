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
		
		switch (messageType) {
		case "PUTCHUNK":
			peer.getScheduler().execute(new ReceivePutChunkThread(message, length, peer));
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
		case "REMOVED":
			peer.getScheduler().execute(new ReceiveRemovedThread(message, peer));
			break;
		default:
			break;
		}
	}
}
