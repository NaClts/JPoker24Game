import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserManage extends Remote {
    String login(String loginName, String password) throws RemoteException;
    String register(String loginName, String password) throws RemoteException;
    String logout(String loginName) throws RemoteException;
}
