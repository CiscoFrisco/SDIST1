public class MessageSenderThread implements Runnable {
	
	private byte[] message;
	private String channel;
	private Peer peer;
	private String protocol;
	private int numChunkMessages;
	private int previousRepDegree;
	
	public MessageSenderThread(byte[] message, String channel, Peer peer, String protocol){
		this.message = message;
		this.channel = channel;
		this.peer = peer;
		this.protocol = protocol;
		this.numChunkMessages = peer.numChunkMessages();

	}
	
	public MessageSenderThread(byte[] message, String channel, Peer peer, int previousRepDegree){
		this.message = message;
		this.channel = channel;
		this.peer = peer;
		this.protocol = protocol;
		this.numChunkMessages = peer.numChunkMessages();
		this.previousRepDegree = previousRepDegree;
	}

	@Override
	public void run() {
		
		String str_message = Utils.bytesToHex(message);
		String[] message_tokens = str_message.split("");

		int currRepDegree = peer.getNumChunksStored(message_tokens[3] + "-" + Utils.asciiToNumber(message_tokens[4]));
		
		if(currRepDegree != previousRepDegree && message_tokens[0].equals("PUTCHUNK") && this.protocol.equals("RECLAIM"))
			return;
		
		if(new String(message).substring(0, 5).equals("CHUNK") && peer.numChunkMessages() != numChunkMessages)
			return;

		peer.getChannel(this.channel).sendMessage(this.message);
	}

}
