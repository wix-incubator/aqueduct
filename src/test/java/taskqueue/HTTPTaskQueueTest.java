package taskqueue;

import httptestserver.HttpTestServer;
import org.junit.Before;
import org.junit.After;

import utils.DefaultTaskQueueResultListener;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * User: evg
 * Date: 23/11/11
 * Time: 17:59
 */


public class HttpTaskQueueTest {
    HttpTaskQueue taskQueue;
    HttpTestServer testServer;
    int serverLocalPort = 0;

    @Before
    public void setup() {

        Logger logger = Logger.getLogger("root");
        logger.setLevel(Level.ALL);

        ConsoleHandler handler = new ConsoleHandler();
        logger.addHandler(handler);

        testServer = new HttpTestServer();
        testServer.start();

        serverLocalPort = testServer.getLocalPort();

        taskQueue = new HttpTaskQueue("test");
        taskQueue.purgeTasks();
    }

    @After
    public void tearDown(){
        testServer.stop();
    }

    @org.junit.Test
    public void testAddGetTaskSuccess() throws Exception {

        DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
        taskQueue.addListener(resultListener, true);

        String url = String.format("http://localhost:%d/test", serverLocalPort);
        taskQueue.addGetTask(url);

        assertEquals(200, resultListener.getTaskResult().getStatus());

        assertEquals(0, taskQueue.getPendingTasks().size());
    }

    @org.junit.Test
    public void testAddGetTaskFailedServerBadPort() throws Exception {

        DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
        taskQueue.addListener(resultListener, true);

        String url = String.format("http://localhost:%d/test", 1);
        int taskId = taskQueue.addGetTask(url);

        assertEquals(0, resultListener.getTaskResult().getStatus());
        
        assertEquals(taskId, taskQueue.getPendingTasks().get(0).getTaskID());
    }
}
