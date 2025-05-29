import java.rmi.*;
import java.rmi.server.*;
import java.sql.SQLException;

public class UserManager extends UnicastRemoteObject implements UserManage {

    OnlineUser onlineUser;
    UserInfo userInfo;

    public UserManager() throws RemoteException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        onlineUser = new OnlineUser();
        userInfo = new UserInfo(); 
        onlineUser.deleteAll();
    };

    public synchronized String login(String loginName, String password) throws RemoteException {
        String errorMessage = "";
        try {
            if ( ! userInfo.contains(loginName, password) ) {
                errorMessage = "Login name or password is incorrect";
            } else if ( onlineUser.contains(loginName) ) {
                errorMessage = "The user has already signed in";
            } else {
                onlineUser.insert(loginName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorMessage = "Internal server error. Please try again later.";
        }
        return errorMessage;
    };

    public synchronized String register(String loginName, String password) throws RemoteException {
        String errorMessage = "";

        if ( loginName.isEmpty() ) {
            errorMessage = "Login name should not be empty";
        } else if ( loginName.length() > 32 ) {
            errorMessage = "Maximum length of login name should be 32 characters";
        } else if ( loginName.contains(",") ) {
            errorMessage = "Login name should not contain comma (,)";
        } else if ( password.isEmpty() ) {
            errorMessage = "Password should not be empty";
        } else if ( password.length() > 32 ) {
            errorMessage = "Maximum length of password should be 32 characters";
        } else {
            try {
                if ( userInfo.contains(loginName) ) {
                    errorMessage = "A user with that username already exists";
                } else {
                    userInfo.insert(loginName, password);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = "Internal server error. Please try again later.";
            }
        }
        return errorMessage;
    };

    public synchronized String logout(String loginName) throws RemoteException {
        String errorMessage = "";
        try {
            if ( ! onlineUser.contains(loginName) ) {
                errorMessage = "The user has not signed in yet";
            } else {
                onlineUser.delete(loginName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorMessage = "Internal server error. Please try again later.";
        }
        return errorMessage;
    };
}
