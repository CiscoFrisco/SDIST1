import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class TCPChunkSenderThread implements Runnable {

    private byte[] message;
    private DataOutputStream dos;
    private DataInputStream dis;
    
    public TCPChunkSenderThread(byte[] message, Peer peer, String ip, int port){
        this.message = message;

        Socket client;
        try {
            client = new Socket(ip, port);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            int oi = Integer.parseInt(Utils.getHeader(message)[4]);
            Thread.sleep(400);

            dos.writeInt(oi);

            if(!dis.readBoolean()){
                return;
            }

            dos.writeInt(message.length); // write length of the message
            dos.write(message); 
            dos.flush();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}