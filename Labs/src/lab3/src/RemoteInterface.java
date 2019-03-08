

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    String sayHello() throws RemoteException;
    
    String lookup(String plate_number) throws RemoteException;
    
    String register(String plate_number, String owner_name) throws RemoteException;
}
