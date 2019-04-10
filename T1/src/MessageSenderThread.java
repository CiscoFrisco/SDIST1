public class MessageSenderThread implements Runnable {
	
	private byte[] message;
	private String[] header;
	private String channel;
	private Peer peer;
	
	public MessageSenderThread(byte[] message, String channel, Peer peer){
		this.message = message;
		header = Utils.getHeader(message);
		this.channel = channel;
		this.peer = peer;
	}

	@Override
	public void run() {
		peer.getChannel(this.channel).sendMessage(this.message);
	}

}
