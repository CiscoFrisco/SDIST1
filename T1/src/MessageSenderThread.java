public class MessageSenderThread implements Runnable {
	
	private String message;
	private String channel;
	
	MessageSenderThread(String message, String channel){
		this.message = message;
		this.channel = channel;
	}

	@Override
	public void run() {
		
		switch(this.channel) {
		case "MC":
			Peer.getMC().sendMessage(this.message);
			break;
		case "MDB":
			Peer.getMDB().sendMessage(this.message);
			break;
		case "MR":
			break;
		default:
			break;
		}
		
	}

}
