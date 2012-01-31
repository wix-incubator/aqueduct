package taskqueue;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 16/11/11
 * Time: 23:51
 */

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

class SQLiteDataSource implements DataSource {

    private Logger logger = Logger.getLogger("root");
    private final String jdbcPrefix = "jdbc:sqlite:";
    private String dbURL = "";
	private PrintWriter printWriter = null;
	private int loginTimeout = 0;
    private Properties prop = new Properties();

    public SQLiteDataSource(String dbFileName){

        try {
			Class.forName("org.sqlite.JDBC");
            dbURL = jdbcPrefix.concat(dbFileName);
            prop.setProperty("shared_cache", "true");

		} catch (ClassNotFoundException e) {
			logger.severe("Failed to create TasqQueue data file");
        }
    }

	public SQLiteDataSource(){
        this("taskqueue.db");
	}

	public Connection getConnection() throws SQLException {

		Connection conn = DriverManager.getConnection(dbURL, prop);
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		conn.setAutoCommit(true);

		return conn;
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		return getConnection();
	}

	public PrintWriter getLogWriter() throws SQLException {
		return printWriter;
	}

	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}

	public void setLogWriter(PrintWriter val) throws SQLException {
		this.printWriter = val;
	}

	public void setLoginTimeout(int val) throws SQLException {
		loginTimeout = val;
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}
}
