class ReceiveAckDeleteThread implements Runnable {
    private Peer peer;
    private String[] header;

    public ReceiveAckDeleteThread(byte[] message, Peer peer) {
        this.peer = peer;
        this.header = Utils.getHeader(message);
    }

    @Override
    public void run() {
        if(peer.getId() != Utils.asciiToNumber(header[3])){
            return;
        }

        peer.addAckMesssage(Utils.hexStringToByteArray(header[4]), Utils.asciiToNumber(header[2]));
    }
}