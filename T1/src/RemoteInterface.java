

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {    
    String backup(String fileName, int replicationDegree, boolean enhancement) throws RemoteException;
    
    String restore(String fileName, boolean enhancement) throws RemoteException;

    String delete(String fileName, boolean enhancement) throws RemoteException;

    String reclaim(int space) throws RemoteException;

    String state() throws RemoteException;
}
