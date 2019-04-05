public class MessageSenderThread implements Runnable {
	
	private byte[] message;
	private String channel;
	private Peer peer;
	private int numChunkMessages;
	
	public MessageSenderThread(byte[] message, String channel, Peer peer){
		this.message = message;
		this.channel = channel;
		this.peer = peer;
		this.numChunkMessages = peer.numChunkMessages();
	}

	@Override
	public void run() {
		
		if(new String(message).substring(0, 5).equals("CHUNK") && peer.numChunkMessages() != numChunkMessages)
			return;

		peer.getChannel(this.channel).sendMessage(this.message);
	}

}
