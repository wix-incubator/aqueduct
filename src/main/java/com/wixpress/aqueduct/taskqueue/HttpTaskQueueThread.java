package com.wixpress.aqueduct.taskqueue;

import com.wixpress.aqueduct.httpclient.HttpClient;
import com.wixpress.aqueduct.task.HttpTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 09/11/11
 * Time: 17:11
 */

class HttpTaskQueueThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTaskQueueThread.class);

    private AtomicBoolean stopped = new AtomicBoolean(false);

    private TaskStorage taskStorage;
    private HttpClient httpClient;
    private ManualResetEvent newTaskEvent;

    public HttpTaskQueueThread(TaskStorage taskStorage, ManualResetEvent newTaskEvent, HttpClient httpClient) {
        this.taskStorage = taskStorage;
        this.newTaskEvent = newTaskEvent;

        this.httpClient = httpClient;
    }


    public void run() {

        while (!stopped.get()) {

            try {
                doTasks();

                //LOGGER.debug("Entering newTaskEvent, waiting wor signal...");
                newTaskEvent.waitSignalWithTimeout(5, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                LOGGER.error("Task thread interrupted", e);
            }
        }

        httpClient.shutdown();
    }

    public void stop() {
        stopped.set(true);
        newTaskEvent.signal();
    }

    private void doTasks() {

        // LOGGER.debug("Start dispatching HTTP tasks...");

        try {
            List<HttpTask> taskList = taskStorage.leaseTasks();

            while (!taskList.isEmpty() && !stopped.get()) {
                LOGGER.debug(format("Got %d tasks to do...", taskList.size()));
                for (HttpTask task : taskList) {

                    if(stopped.get()) break;

                    //TODO: Handle minRetryInterval here somehow
                    httpClient.send(task);
                }
                taskList = taskStorage.leaseTasks();
            }
        } catch (Exception e) {
            LOGGER.error(format("Error dispatching tasks - %s", e.getMessage()), e);
        }
    }
}
