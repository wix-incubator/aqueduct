package taskqueue;

import org.junit.Before;
import taskqueue.HttpTaskQueue;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
  * User: evg
 * Date: 23/11/11
 * Time: 17:59
 */


public class HTTPTaskQueueTest {

    @Before
    public void setup(){

        Logger logger = Logger.getLogger("root");
		logger.setLevel(Level.ALL);

        ConsoleHandler handler = new ConsoleHandler();
        logger.addHandler(handler);

    }

    @org.junit.Test
    public void testAddGetTask(){

        try {
            HttpTaskQueue taskQueue = new HttpTaskQueue("test");
            taskQueue.purgeTasks();

            taskQueue.addGetTask("http://www.google.co.il", null, null,null);

            Thread.sleep(10000);

            taskQueue.addGetTask("http://noc.co.il", null, null,null);
            Thread.sleep(10000);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
