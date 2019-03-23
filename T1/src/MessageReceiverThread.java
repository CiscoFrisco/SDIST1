
public class MessageReceiverThread implements Runnable {

	private String message;
	private Peer peer;

	public MessageReceiverThread(String message, Peer peer) {
		this.message = message;
		this.peer = peer;
	}
	
	public String getHeader(String message) {
		return message.substring(0, message.indexOf("\r\n"));
	}
	
	public String getChunkContent(String message) {
		return message.substring(message.indexOf("\r\n"));
	}

	@Override
	public void run() {
		
		String header = getHeader(message);

		String[] splitHeader = message.split(" ");
		
		for(int i = 0; i < splitHeader.length; i++) {
			splitHeader[i] = splitHeader[i].trim();
			System.out.println(splitHeader[i]);
		}
	
		
		switch(splitHeader[0]) {
		case "PUTCHUNK":
			peer.getScheduler().execute(new ReceivePutChunkThread(splitHeader, getChunkContent(message), peer));
			break;
		case "STORED":
			peer.getScheduler().execute(new ReceiveStoredThread(splitHeader, peer));
			break;
		default:
			break;
		}

	}

}
