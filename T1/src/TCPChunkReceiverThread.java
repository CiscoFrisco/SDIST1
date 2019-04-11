import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class TCPChunkReceiverThread implements Runnable {

    private Peer peer;
    private DataInputStream dis;
    private DataOutputStream dos;
    private byte[] file;
    private ServerSocket serverSocket;
    private int numChunks;

    public TCPChunkReceiverThread(Peer peer, ServerSocket serverSocket, byte[] file, int numChunks) {
        this.peer = peer;
        this.file = file;
        this.serverSocket = serverSocket;
        this.numChunks = numChunks;
    }

    @Override
    public void run() {

        while (!serverSocket.isClosed()) {
        	System.out.println("OI");
            try {

                Socket server;

                try {
                    server = serverSocket.accept();
                    this.dis = new DataInputStream(server.getInputStream());
                    this.dos = new DataOutputStream(server.getOutputStream());

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                int chunk = dis.readInt();
                boolean acceptChunk = !peer.getStorage().hasRestoredChunk(Utils.bytesToHex(file) + "-" + chunk);
                System.out.println(chunk + ":" + acceptChunk);
                dos.writeBoolean(acceptChunk);
                dos.flush();

                if (!acceptChunk) {
                    continue;
                }

                int length = dis.readInt(); // read length of incoming message
                if (length > 0) {
                    byte[] message = new byte[length];
                    dis.readFully(message, 0, message.length); // read the message

                    String[] header = Utils.getHeader(message);
                    byte[] chunkContent = Utils.getChunkContent(message, length);

                    peer.incNumChunkMessages(header[3], chunk);

                    if (header[3].equals(peer.getRestoredFile())) {
                        peer.getStorage().putRestoredChunk(header[3] + "-" + Integer.parseInt(header[4]), chunkContent);
                        peer.flagChunkReceived();
                    }

                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}