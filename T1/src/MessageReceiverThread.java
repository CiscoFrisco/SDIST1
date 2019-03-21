
public class MessageReceiverThread implements Runnable {

	private String message;

	public MessageReceiverThread(String message) {
		this.message = message;
	}

	@Override
	public void run() {
		String messageType = message.substring(0, message.indexOf(' '));

		switch(messageType) {
		case "PUTCHUNK":
			Peer.getScheduler().execute(new ReceivePutChunkThread(this.message));
			break;
		default:
			break;
		}

	}

}
