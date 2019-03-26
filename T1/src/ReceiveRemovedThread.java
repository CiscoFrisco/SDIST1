import java.util.concurrent.TimeUnit;

public class ReceiveRemovedThread implements Runnable {

    private Peer peer;
    private String message[];

    public ReceiveRemovedThread(String message[], Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        Storage storage = peer.getStorage();
        String fileID = message[3];
        int chunkNo = Integer.parseInt(message[4]);
        boolean decremented = storage.decrementReplicationDegree(fileID, chunkNo);
        Chunk chunk = storage.getChunk(fileID, chunkNo);
        int desiredReplicationDegree = chunk.getDesiredReplicationDegree();

        //TODO: verificar rececao putchunk

        if (decremented && storage.getReplicationDegree(fileID, chunkNo) < desiredReplicationDegree) {
            int waitTime = Utils.getRandomNumber(401);

            String chunk_msg = peer.buildPutChunkMessage(peer.getVersion(), peer.getId(), fileID, chunkNo,
                    desiredReplicationDegree, chunk);
            peer.getScheduler().schedule(new MessageSenderThread(chunk_msg, "MDB", peer), waitTime,
                    TimeUnit.MILLISECONDS);
            peer.getScheduler().schedule(new ConfirmationCollector(peer, chunk_msg, 1, 1, desiredReplicationDegree),
                    waitTime + 1000, TimeUnit.MILLISECONDS);
        }
    }
}
