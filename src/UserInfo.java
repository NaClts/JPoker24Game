import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserInfo {

    private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "c3358";
    private Connection conn;

    public UserInfo() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?user="+DB_USER+"&password="+DB_PASS);
		System.out.println("Database connection successful");
    }

    public boolean contains(String loginName, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM UserInfo WHERE name = ? AND password = ?");
        stmt.setString(1, loginName);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean contains(String loginName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM UserInfo WHERE name = ?");
        stmt.setString(1, loginName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public void insert(String loginName, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO UserInfo (name, password) VALUES (?, ?)");
        stmt.setString(1, loginName);
        stmt.setString(2, password);
        stmt.execute();
    }

	private void read(String loginName) {
		try {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM UserInfo WHERE name = ?");
            stmt.setString(1, loginName);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println("Password of "+loginName+" is "+rs.getString(1));
			} else {
				System.out.println(loginName+" not found!");
			}
		} catch (SQLException e) {
			System.err.println("Error reading record: "+e);
		}
	}
	private void list() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name, password FROM UserInfo");
			while (rs.next()) {
				System.out.println("Password of "+rs.getString(1)+" is "+rs.getString(2));
			}
		} catch (SQLException e) {
			System.err.println("Error listing records: "+e);
		}
	}
	private void update(String loginName, String password) {
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE UserInfo SET password = ? WHERE name = ?");
			stmt.setString(1, password);
			stmt.setString(2, loginName);

			int rows = stmt.executeUpdate();
			if (rows > 0) {
				System.out.println("Password of "+loginName+" updated");
			} else {
				System.out.println(loginName+" not found!");
			}
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error updating record: "+e);
		}
	}
	private void delete(String loginName) {
		try {
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM UserInfo WHERE name = ?");
			stmt.setString(1, loginName);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				System.out.println("Record of "+loginName+" removed");
			} else {
				System.out.println(loginName+" not found!");
			}
		} catch (SQLException e) {
			System.err.println("Error deleting record: "+e);
		}
	}
}
