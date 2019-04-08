class CollectDeleteAcksThread implements Runnable {
    private Peer peer;
    private byte[] fileId;

    public CollectDeleteAcksThread(byte[] fileId, Peer peer) {
        this.peer = peer;
        this.fileId = fileId;
    }
    @Override
    public void run() {
        peer.putIdlePeersTasks(fileId);
    }
    
}