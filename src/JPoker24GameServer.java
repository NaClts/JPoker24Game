import java.rmi.Naming;

public class JPoker24GameServer {
    public static void main(String[] args) {
        try {
            UserManager userManager = new UserManager();
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("UserManager", userManager);
            System.out.println("UserManager Service registered");
            
            LeaderBoardHandler leaderBoardHandler = new LeaderBoardHandler();
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("LeaderBoardHandler", leaderBoardHandler);
            System.out.println("LeaderBoardHandler Service registered");

            Game game = new Game(leaderBoardHandler);
            game.start();
        } catch (Exception e) {
            System.err.println("Exception thrown: "+e);
        }
    }
}
