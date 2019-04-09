import java.util.concurrent.TimeUnit;

public class ReceiveRemovedThread implements Runnable {

    private Peer peer;
    private String header[];
    private String protocol;

    public ReceiveRemovedThread(byte[] message, Peer peer, String protocol) {
        this.header = Utils.getHeader(message);
        this.peer = peer;
        this.protocol = protocol;
    }

    @Override
    public void run() {
    	System.out.println(peer.getId());	
    	System.out.println(Integer.parseInt(header[2]));	

    	if(peer.getId() == Integer.parseInt(header[2]))
    		return;
    	
    	Storage storage = peer.getStorage();
        byte[] fileID = Utils.hexStringToByteArray(header[3]);
        int chunkNo = Utils.asciiToNumber(header[4]);
        boolean decremented = storage.decrementReplicationDegree(fileID, chunkNo);
        Chunk chunk = storage.getChunk(fileID, chunkNo);
        
        if(!decremented)
        	return;
        
        int desiredReplicationDegree = chunk.getDesiredReplicationDegree();

        //TODO: verificar rececao putchunk

        if (decremented && storage.getReplicationDegree(fileID, chunkNo) < desiredReplicationDegree) {
            int waitTime = Utils.getRandomNumber(401);
            int currRepDegree = peer.getNumChunksStored(header[3] + "-" + chunkNo);
            byte[] chunk_msg = peer.buildPutChunkMessage(peer.getVersion(), peer.getId(), fileID, chunkNo,
                    desiredReplicationDegree, chunk);
            System.out.println("SENDING PUTCHUNK...");
            peer.getScheduler().schedule(new MessageSenderThread(chunk_msg, "MDB", peer, currRepDegree), waitTime,
                    TimeUnit.MILLISECONDS);
            peer.getScheduler().schedule(new ConfirmationCollector(peer, chunk_msg, 1, 1, desiredReplicationDegree, protocol),
                    waitTime + 1000, TimeUnit.MILLISECONDS);
        }
    }
}
