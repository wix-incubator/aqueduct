package taskqueue;

import task.HttpTask;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by evg
 * Date: 15/11/11
 * Time: 13:34
 */


public class TaskStorage {

    private Logger logger = Logger.getLogger("root");
    private SQLiteDataSource pendingDataSource;
    private TaskMarshaller taskMarshaller;

    //private ObjectMapper jsonMapper = new ObjectMapper();
    //private Gson gson = new Gson();

    public TaskStorage() {
        this("taskqueue.db");
    }

    public TaskStorage(String dbFileName) {
        this(dbFileName, new DefaultTaskMarshaller());
    }

    public TaskStorage(String dbFileName, TaskMarshaller marshaller) {
        try {
            this.taskMarshaller = marshaller;
            initDB(dbFileName);
        } catch (SQLException e) {
            logger.severe("Failed to initialize TaskQueue storage");
        }
    }

    public int addTask(HttpTask task) throws Exception {

        int taskID = -1;
        //String json = jsonMapper.writeValueAsString(task);
        String json = taskMarshaller.marshal(task);


        if (null == json) {
            throw new Exception("Serialize error");
        }

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("insert into task_queue(task_json, date_updated, date_created) " +
                "values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, json);
        stmt.setLong(2, System.currentTimeMillis());
        stmt.setLong(3, System.currentTimeMillis());

        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) taskID = rs.getInt(1);

        closeConnection(rs, stmt, conn);

        task.setTaskID(taskID);
        return taskID;
    }

    public void deleteTask(HttpTask task) throws Exception {

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("delete from task_queue where id = ?");

        stmt.setInt(1, task.getTaskID());
        stmt.execute();
        closeConnection(stmt, conn);
    }

    public void saveTask(HttpTask task) throws Exception{

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
                "update task_queue set task_json = ?, date_updated = ?, leased = 0 where id = ?");

        //stmt.setString(1, jsonMapper.writeValueAsString(task));
        stmt.setString(1, taskMarshaller.marshal(task));

        stmt.setLong(2, System.currentTimeMillis());
        stmt.setInt(3, task.getTaskID());

        stmt.execute();
        closeConnection(stmt, conn);
    }

    public void giveUpTask(HttpTask task){
    }

    public List<HttpTask> leaseTasks() throws Exception {

        //TODO: Here may be race condition if we have more than one dispatching thread or under very high events rate

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement fetchStmt = conn.prepareStatement(
                "select * from task_queue where leased = 0 order by date_updated limit 4");

        ResultSet rs = fetchStmt.executeQuery();

        LinkedList<HttpTask> taskList = new LinkedList<HttpTask>();
        while (rs.next()) {

            //HttpTask task = jsonMapper.readValue(rs.getString("task_json"), HttpTask.class);
            HttpTask task = taskMarshaller.unmarshal(rs.getString("task_json"));

            task.setTaskID(rs.getInt("id"));

            PreparedStatement leaseStmt = conn.prepareStatement(
                    "update task_queue set leased = 1, date_updated = ? where id = ?");
            leaseStmt.setLong(1, System.currentTimeMillis());
            leaseStmt.setInt(2, task.getTaskID());
            leaseStmt.execute();
            leaseStmt.close();

            taskList.add(task);
        }

        closeConnection(rs, fetchStmt, conn);
        return taskList;
    }

    public void purge() throws Exception {

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("delete from task_queue");
        stmt.execute();
        closeConnection(stmt, conn);
    }

    public List<HttpTask> getActiveTasks() throws Exception{

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement fetchStmt = conn.prepareStatement("select * from task_queue where leased = 1");

        ResultSet rs = fetchStmt.executeQuery();

        ArrayList<HttpTask> taskList = new ArrayList<HttpTask>();
        while (rs.next()) {
            //HttpTask task = jsonMapper.readValue(rs.getString("task_json"), HttpTask.class);
            HttpTask task = taskMarshaller.unmarshal(rs.getString("task_json"));
            task.setTaskID(rs.getInt("id"));

            taskList.add(task);
        }

        closeConnection(rs, fetchStmt, conn);
        return taskList;
    }

    public List<HttpTask> getPendingTasks() throws Exception{

        Connection conn = pendingDataSource.getConnection();
        PreparedStatement fetchStmt = conn.prepareStatement("select * from task_queue");

        ResultSet rs = fetchStmt.executeQuery();

        ArrayList<HttpTask> taskList = new ArrayList<HttpTask>();
        while (rs.next()) {
            //HttpTask task = jsonMapper.readValue(rs.getString("task_json"), HttpTask.class);
            HttpTask task = taskMarshaller.unmarshal(rs.getString("task_json"));
            task.setTaskID(rs.getInt("id"));

            taskList.add(task);
        }

        closeConnection(rs, fetchStmt, conn);
        return taskList;
    }


    private void initDB(String dbFileName) throws SQLException {
        pendingDataSource = new SQLiteDataSource(dbFileName);

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = pendingDataSource.getConnection();
            st = conn.prepareStatement("create table if not exists task_queue(\n" +
                    "id INTEGER PRIMARY KEY,\n" +
                    "task_json text,\n" +
                    "leased integer not null default 0,\n" +
                    "date_created numeric NOT NULL,\n" +
                    "date_updated numeric not null)");

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
