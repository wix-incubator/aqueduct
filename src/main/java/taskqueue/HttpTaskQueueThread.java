package taskqueue;

import httpclient.HttpClient;
import task.HttpTask;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static logging.LogWrapper.*;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 09/11/11
 * Time: 17:11
 */

class HttpTaskQueueThread implements Runnable {

    private AtomicBoolean stopped = new AtomicBoolean(false);

    private TaskStorage taskStorage;
    private HttpClient httpClient;
    private ManualResetEvent newTaskEvent;

    public HttpTaskQueueThread(TaskStorage taskStorage, ManualResetEvent newTaskEvent, HttpTaskResultListener completedListener) {
        this.taskStorage = taskStorage;
        this.newTaskEvent = newTaskEvent;

        this.httpClient = new HttpClient(completedListener);
    }


    public void run() {

        while (!stopped.get()) {

            try {
                doTasks();

                debug("Entering newTaskEvent, waiting wor signal...");
                newTaskEvent.waitSignalWithTimeout(5, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
               error("Task thread interrupted");
            }
        }

        httpClient.shutdown();
    }

    public void stop() {
        stopped.set(true);
        newTaskEvent.signal();
    }

    private void doTasks() {

        debug("Start dispatching HTTP tasks...");

        try {
            List<HttpTask> taskList = taskStorage.leaseTasks();

            while (!taskList.isEmpty() && !stopped.get()) {
                debug("Got %d tasks to do...", taskList.size());
                for (HttpTask task : taskList) {

                    if(stopped.get()) break;

                    //TODO: Handle minRetryInterval here somehow
                    httpClient.send(task);
                }
                taskList = taskStorage.leaseTasks();
            }


        } catch (Exception e) {
            error("Error dispatching tasks - %s", e.getMessage());
        }
    }

}
