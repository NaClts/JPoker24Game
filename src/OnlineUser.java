import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OnlineUser {

    private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "c3358";
    private Connection conn;

    public OnlineUser() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?user="+DB_USER+"&password="+DB_PASS);
		System.out.println("Database connection successful");
    }

    public boolean contains(String loginName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM OnlineUser WHERE name = ?");
        stmt.setString(1, loginName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public void insert(String loginName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO OnlineUser (name) VALUES (?)");
        stmt.setString(1, loginName);
        stmt.execute();
    }

    public void delete(String loginName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM OnlineUser WHERE name = ?");
        stmt.setString(1, loginName);
        stmt.execute();
    }

    public void deleteAll() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DELETE FROM OnlineUser");
    }

	private void read(String loginName) {
		try {
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM OnlineUser WHERE name = ?");
            stmt.setString(1, loginName);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println(loginName+" is online");
			} else {
				System.out.println(loginName+" is NOT online!");
			}
		} catch (SQLException e) {
			System.err.println("Error reading record: "+e);
		}
	}
	private void list() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name FROM OnlineUser");
			while (rs.next()) {
				System.out.println(rs.getString(1)+" is online");
			}
		} catch (SQLException e) {
			System.err.println("Error listing records: "+e);
		}
	}
	private void update(String oldLoginName, String newLoginName) {
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE OnlineUser SET name = ? WHERE name = ?");
			stmt.setString(1, newLoginName);
			stmt.setString(2, oldLoginName);

			int rows = stmt.executeUpdate();
			if (rows > 0) {
				System.out.println("Name of "+oldLoginName+" updated");
			} else {
				System.out.println(oldLoginName+" not found!");
			}
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Error updating record: "+e);
		}
	}
}
