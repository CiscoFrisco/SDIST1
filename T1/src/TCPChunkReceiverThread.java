import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class TCPChunkReceiverThread implements Runnable {

    private Peer peer;
    private DataInputStream dis;

    public TCPChunkReceiverThread(Peer peer, ServerSocket serverSocket) {
        this.peer = peer;
        Socket server;

        try {
            server = serverSocket.accept();
            this.dis = new DataInputStream(server.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        try {
            int length = dis.readInt(); // read length of incoming message
            if (length > 0) {
                byte[] message = new byte[length];
                dis.readFully(message, 0, message.length); // read the message

                String[] header = Utils.getHeader(message);
                byte[] chunkContent = Utils.getChunkContent(message, length);

                peer.incNumChunkMessages();
                
                if(header[3].equals(peer.getRestoredFile())) {
                    peer.getStorage().putRestoredChunk(header[3] + "-" + Utils.asciiToNumber(header[4]), chunkContent);
                    peer.flagChunkReceived();
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

}