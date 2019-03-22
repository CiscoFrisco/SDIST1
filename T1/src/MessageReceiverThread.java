
public class MessageReceiverThread implements Runnable {

	private String message;

	public MessageReceiverThread(String message) {
		this.message = message;
	}

	@Override
	public void run() {

		System.out.println(message);

		String[] splitMessage = message.split(" ");
		
		for(int i = 0; i < splitMessage.length; i++) {
			splitMessage[i] = splitMessage[i].trim();
			System.out.println(splitMessage[i]);
		}
	
		
		switch(splitMessage[0]) {
		case "PUTCHUNK":
			Peer.getScheduler().execute(new ReceivePutChunkThread(this.message));
			break;
		default:
			break;
		}

	}

}
