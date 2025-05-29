import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LeaderBoardHandle extends Remote {
    String[][] getLeaderBoard() throws RemoteException;
    PlayerStatistics getPlayerStatistics(String name) throws RemoteException;
}
