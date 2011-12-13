package taskqueue;

import httpclient.HttpClient;
import httpclient.HttpTaskCompletedListener;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 09/11/11
 * Time: 17:11
 */

public class HttpTaskQueueThread implements Runnable{

    private Logger logger = Logger.getLogger("root");

    private AtomicBoolean stopped = new AtomicBoolean(false);

    private TaskStorage taskStorage;
    private HttpClient httpClient;
    private ManualResetEvent newTaskEvent;

    public HttpTaskQueueThread(TaskStorage taskStorage, ManualResetEvent newTaskEvent, HttpTaskCompletedListener completedListener){
        this.taskStorage = taskStorage;
        this.newTaskEvent = newTaskEvent;

        this.httpClient = new HttpClient(completedListener);
    }


    public void run(){

        while (!stopped.get()){

            try {
                doTasks();

                logger.log(Level.INFO, "Entering newTaskEvent, waiting wor signal...");
                newTaskEvent.waitSignalWithTimeout(5, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Task thread interrupted");
                httpClient.shutdown();
            }
        }
    }

    public void stop(){
        stopped.set(true);
        newTaskEvent.signal();
        httpClient.shutdown();
    }

    private void doTasks(){

        logger.log(Level.INFO, "Start dispatching HTTP tasks...");

        try {
            List<HttpTask> taskList = taskStorage.getPendingTasks();

            logger.info(String.format("Got %d tasks to do...", taskList.size()));
            for(HttpTask task : taskList){
                httpClient.send(task);
            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
