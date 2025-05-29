import java.rmi.*;
import java.rmi.server.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LeaderBoardHandler extends UnicastRemoteObject implements LeaderBoardHandle {

    private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "c3358";
    private Connection conn;

    public LeaderBoardHandler() throws RemoteException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?user="+DB_USER+"&password="+DB_PASS);
		System.out.println("Database connection successful");
    };

    public String[][] getLeaderBoard() throws RemoteException {
        String[][] leaderBoardArray;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT player_rank, name, games_won, games_played, avg_winning_time FROM LeaderBoardView");
            leaderBoardArray = resultSetToStringArray(rs);
        } catch (SQLException e) {
            System.err.println("Error reading record: "+e);
            leaderBoardArray = new String[0][0];
        }
        return leaderBoardArray;
    }

    public PlayerStatistics getPlayerStatistics(String name) throws RemoteException {
        PlayerStatistics playerStatistics = new PlayerStatistics();
        playerStatistics.player_rank = -1;
        playerStatistics.name = name;
        playerStatistics.games_won = 0;
        playerStatistics.ganes_played = 0;
        playerStatistics.avg_winning_time = -1;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT player_rank, name, games_won, games_played, avg_winning_time FROM LeaderBoardView WHERE name = ?");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                playerStatistics.player_rank = rs.getInt(1);
                playerStatistics.games_won = rs.getInt(3);
                playerStatistics.ganes_played = rs.getInt(4);
                playerStatistics.avg_winning_time = rs.getDouble(5);
            } else {
                System.out.println("LeaderBoard record of "+name+" not found!");
            }
        } catch (SQLException e) {
            System.err.println("Error reading record: "+e);
        }
        return playerStatistics;
    }

    public synchronized void setNewPlayerRecord(String name, boolean isWinning, int gameDuration) {
        try {
            if (contains(name)) {
                // Update
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE LeaderBoard SET games_won = games_won + ? , games_played = games_played + 1 , total_winning_time = total_winning_time + ? WHERE name = ?"
                    );
                if (isWinning) {
                    stmt.setInt(1, 1);  // games_won += 1
                    stmt.setInt(2, gameDuration);  // total_winning_time += gameDuration
                } else {
                    stmt.setInt(1, 0);  // games_won += 0
                    stmt.setInt(2, 0);  // total_winning_time += 0
                }
                stmt.setString(3, name);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Updated LeaderBoard record of "+name);
                } else {
                    System.out.println("LeaderBoard record of "+name+" not found!");
                }
            } else {
                // Insert
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO LeaderBoard (name, games_won, games_played, total_winning_time) VALUES (?, ?, 1, ?)"
                );
                stmt.setString(1, name);
                if (isWinning) {
                    stmt.setInt(2, 1);  // games_won = 1
                    stmt.setInt(3, gameDuration);  // total_winning_time = gameDuration
                } else {
                    stmt.setInt(2, 0);  // games_won = 0
                    stmt.setInt(3, 0);  // total_winning_time = 0
                }
                stmt.execute();
                System.out.println("Created LeaderBoard record of "+name);
            }
        } catch (SQLException e) {
            System.err.println("Error setting record: "+e);
        }
    };
    
    private boolean contains(String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM LeaderBoardView WHERE name = ?");
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    private static String[][] resultSetToStringArray(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Use ArrayList to store rows dynamically
        ArrayList<String[]> rows = new ArrayList<>();
        
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getString(i + 1);  // JDBC columns are 1-based
            }
            rows.add(row);
        }
        
        // Convert ArrayList to 2D array
        return rows.toArray(new String[0][0]);
    }
}
