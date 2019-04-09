import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class TCPChunkSenderThread implements Runnable {

    private byte[] message;
    private DataOutputStream dos;
    private DataInputStream dis;
    
    public TCPChunkSenderThread(byte[] message, Peer peer){
        this.message = message;
        // TODO: hostname, port ?
        Socket client;
        try {
            client = new Socket("localhost", 3002);
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
            int oi = Utils.asciiToNumber(Utils.getHeader(message)[4]);
            System.out.println(oi);
            dos.writeInt(oi);

            if(!dis.readBoolean()){
                return;
            }

            dos.writeInt(message.length); // write length of the message
            dos.write(message); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}