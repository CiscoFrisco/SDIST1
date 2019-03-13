

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {    
    String backup(String fileName, int replicationDegree) throws RemoteException;
    
    String restore(String fileName) throws RemoteException;

    String delete(String fileName) throws RemoteException;

    String reclaim(int space) throws RemoteException;

    String state() throws RemoteException;
}
