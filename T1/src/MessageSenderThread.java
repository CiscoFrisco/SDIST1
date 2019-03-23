public class MessageSenderThread implements Runnable {
	
	private String message;
	private String channel;
	private Peer peer;
	
	public MessageSenderThread(String message, String channel, Peer peer){
		this.message = message;
		this.channel = channel;
		this.peer = peer;
	}

	@Override
	public void run() {
		switch(this.channel) {
		case "MC":
			peer.getMC().sendMessage(this.message);
			break;
		case "MDB":
			peer.getMDB().sendMessage(this.message);
			break;
		case "MR":
			peer.getMDR().sendMessage(this.message);
			break;
		default:
			break;
		}
	}

}
