class ReceiveAckDeleteThread implements Runnable {
    private Peer peer;
    private String[] header;

    public ReceiveAckDeleteThread(byte[] message, Peer peer) {
        this.peer = peer;
        this.header = Utils.getHeader(message);
    }

    @Override
    public void run() {

        if (peer.getVersion().equals("1.0")) {
            return;
        }

        int senderId = Integer.parseInt(header[2]);

        if (peer.getId() == Integer.parseInt(header[3]))
            peer.getStorage().addAckMesssage(header[4], senderId);

        peer.getStorage().removeConfirmationMessages(header[4], senderId);
    }
}