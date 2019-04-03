public class MessageSenderThread implements Runnable {
	
	private String message;
	private String channel;
	private Peer peer;
	private int numChunkMessages;
	
	public MessageSenderThread(String message, String channel, Peer peer){
		this.message = message;
		this.channel = channel;
		this.peer = peer;
		this.numChunkMessages = peer.numChunkMessages();
	}

	@Override
	public void run() {

		if(this.message.substring(0, 5).equals("CHUNK") && peer.numChunkMessages() != numChunkMessages)
			return;

		peer.getChannel(this.channel).sendMessage(this.message);
	}

}
