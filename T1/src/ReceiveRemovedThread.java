import java.util.concurrent.TimeUnit;

public class ReceiveRemovedThread implements Runnable {

    private Peer peer;
    private String header[];

    public ReceiveRemovedThread(byte[] message, Peer peer) {
        this.header = Utils.getHeader(message);
        this.peer = peer;
    }

    @Override
    public void run() {
        Storage storage = peer.getStorage();
        byte[] fileID = Utils.hexStringToByteArray(header[3]);
        int chunkNo = Utils.asciiToNumber(header[4]);
        boolean decremented = storage.decrementReplicationDegree(fileID, chunkNo);
        Chunk chunk = storage.getChunk(fileID, chunkNo);
        int desiredReplicationDegree = chunk.getDesiredReplicationDegree();

        //TODO: verificar rececao putchunk

        if (decremented && storage.getReplicationDegree(fileID, chunkNo) < desiredReplicationDegree) {
            int waitTime = Utils.getRandomNumber(401);

            byte[] chunk_msg = peer.buildPutChunkMessage(peer.getVersion(), peer.getId(), fileID, chunkNo,
                    desiredReplicationDegree, chunk);
            peer.getScheduler().schedule(new MessageSenderThread(chunk_msg, "MDB", peer), waitTime,
                    TimeUnit.MILLISECONDS);
            peer.getScheduler().schedule(new ConfirmationCollector(peer, chunk_msg, 1, 1, desiredReplicationDegree),
                    waitTime + 1000, TimeUnit.MILLISECONDS);
        }
    }
}
