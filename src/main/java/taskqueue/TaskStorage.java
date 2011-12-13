package taskqueue;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 15/11/11
 * Time: 13:34
 */


public class TaskStorage {

    private Logger logger = Logger.getLogger("root");
    private SQLiteDataSource dataSource;
    private Gson gson;

    public TaskStorage() {
        this("taskqueue.db");
    }

    public TaskStorage(String dbFileName) {
        try {
            initDB(dbFileName);
            gson = new Gson();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize TaskQueue storage");
        }
    }

    public int addTask(HttpTask task) throws Exception {

        int taskID = -1;
        String json = gson.toJson(task, HttpTask.class);

        if (json == null) {
            throw new Exception("Serialize error");
        }

        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("insert into task_queue(task_json, date_created) " +
                "values(?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, json);
        stmt.setDate(2, new Date(Calendar.getInstance().getTimeInMillis()));

        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) taskID = rs.getInt(1);

        closeConnection(rs, stmt, conn);

        return taskID;
    }

    public void completeTask(HttpTask task) throws Exception {

        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = null;

        if (task.isSuccess()) {
            stmt = conn.prepareStatement("delete from task_queue where id = ?");
        } else {
            stmt = conn.prepareStatement("update task_queue set leased = 0 where id = ?");
        }

        stmt.setInt(1, task.getTaskID());
        stmt.execute();
        closeConnection(stmt, conn);
    }

    public List<HttpTask> getPendingTasks() throws Exception {

        Connection conn = dataSource.getConnection();
        PreparedStatement fetchStmt = conn.prepareStatement("select * from task_queue where leased = 0 order by id limit 4");

        ResultSet rs = fetchStmt.executeQuery();

        ArrayList<HttpTask> taskList = new ArrayList<HttpTask>();
        while (rs.next()) {
            HttpTask task = gson.fromJson(rs.getString("task_json"), HttpTask.class);
            task.setTaskID(rs.getInt("id"));

            PreparedStatement leaseStmt = conn.prepareStatement("update task_queue set leased = 1 where id = ?", task.getTaskID());
            leaseStmt.execute();
            leaseStmt.close();

            taskList.add(task);
        }

        closeConnection(rs, fetchStmt, conn);
        return taskList;
    }

    public void purge() throws Exception {

        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("delete from task_queue");
        stmt.execute();
        closeConnection(stmt, conn);
    }

    private void initDB(String dbFileName) throws SQLException {
        dataSource = new SQLiteDataSource(dbFileName);

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("create table if not exists task_queue(\n" +
                    "id INTEGER PRIMARY KEY,\n" +
                    "task_json text,\n" +
                    "leased integer not null default 0,\n" +
                    "date_created datetime NOT NULL,\n" +
                    "date_updated datetime not null default CURRENT_TIMESTAMP)");

            st.execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create required table");
        } finally {
            closeConnection(st, conn);
        }
    }

    private static void closeConnection(ResultSet rs, Statement stmt, Connection con) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(con);
    }

    private static void closeConnection(Statement stmt, Connection con) {
        closeStatement(stmt);
        closeConnection(con);
    }

    private static void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
        }
    }

    private static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
        }
    }

    private static void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
        }
    }
}
