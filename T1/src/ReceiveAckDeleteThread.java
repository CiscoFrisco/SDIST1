class ReceiveAckDeleteThread implements Runnable {
    private Peer peer;
    private String[] header;

    public ReceiveAckDeleteThread(byte[] message, Peer peer) {
        this.peer = peer;
        this.header = Utils.getHeader(message);
    }

    @Override
    public void run() {

        if(peer.getId() != Integer.parseInt(header[3])){
            return;
        }
        System.out.println(header[4] + "-" + Integer.parseInt(header[2]));
        peer.getStorage().addAckMesssage(header[4], Integer.parseInt(header[2]));
    }
}