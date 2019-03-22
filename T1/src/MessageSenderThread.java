public class MessageSenderThread implements Runnable {
	
	private String message;
	private String channel;
	
	public MessageSenderThread(String message, String channel){
		this.message = message;
		this.channel = channel;
	}

	@Override
	public void run() {
		System.out.println("fdsss");
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
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
