import java.util.ArrayList;

class ReceiveAnnounceThread implements Runnable {
    private Peer peer;
    private String[] header;

    public ReceiveAnnounceThread(byte[] message, Peer peer) {
        this.header = Utils.getHeader(message);

        this.peer = peer;
    }

    @Override
    public void run() {
        ArrayList<String> tasks = peer.getStorage().getTasks(Integer.parseInt(header[2]));
        
        for(String fileId : tasks){
            byte[] delete = peer.buildDeleteMessage(peer.getVersion(), peer.getId(), Utils.hexStringToByteArray(fileId));
            this.peer.getScheduler().execute(new MessageSenderThread(delete, "MC", peer));
        }
    }
}