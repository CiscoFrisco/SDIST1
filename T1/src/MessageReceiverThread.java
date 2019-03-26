
public class MessageReceiverThread implements Runnable {

	private String message;
	private Peer peer;

	public MessageReceiverThread(String message, Peer peer) {
		this.message = message;
		this.peer = peer;
	}

	@Override
	public void run() {

		String header = Utils.getHeader(message);

		String[] splitHeader = message.split(" ");

		for (int i = 0; i < splitHeader.length; i++) {
			splitHeader[i] = splitHeader[i].trim();
		}

		System.out.println(splitHeader[0]);

		switch (splitHeader[0]) {
		case "PUTCHUNK":
			peer.getScheduler().execute(new ReceivePutChunkThread(splitHeader, Utils.getChunkContent(message), peer));
			break;
		case "STORED":
			peer.getScheduler().execute(new ReceiveStoredThread(splitHeader, peer));
			break;
		case "GETCHUNK":
			peer.getScheduler().execute(new ReceiveGetChunkThread(splitHeader, peer));
			break;
		case "CHUNK":
			peer.getScheduler().execute(new ReceiveChunkThread(message, peer));
			break;
		case "DELETE":
			peer.getScheduler().execute(new ReceiveDeleteThread(splitHeader, peer));
			break;
		case "REMOVE":
			peer.getScheduler().execute(new ReceiveRemovedThread(splitHeader, peer));
			break;
		default:
			break;
		}
	}
}
