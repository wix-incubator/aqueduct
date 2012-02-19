package taskqueue;

import task.HttpTask;
import task.HttpTaskFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static logging.LogWrapper.*;
import static task.HttpConstants.HttpVerb;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 09/11/11
 * Time: 16:34
 */
public class HttpTaskQueue {

    private TaskStorage taskStorage;
    private ManualResetEvent newTaskEvent = new ManualResetEvent();
    private HttpTaskQueueThread taskQueueThread;
    private HttpTaskResultListener resultListener;
    private boolean notifyIfFailed = false;

    public HttpTaskQueue(String appID){
        // this(appID, new DefaultTaskMarshaller());
        this(appID, new HttpTask.DefaultMarshaler());
    }
    
    public HttpTaskQueue(String appID, TaskMarshaller taskMarshaller) {
        this.taskStorage = new TaskStorage(appID.concat(".db"), taskMarshaller);
        taskQueueThread = new HttpTaskQueueThread(taskStorage, newTaskEvent, new TaskCompletedListener());
        Executors.newSingleThreadExecutor().submit(taskQueueThread);

        // need shutdown hook in order to terminate all working threads properly
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public void queue(HttpTask task) throws Exception {

        taskStorage.addTask(task.sanitize());
        
        // Notify worker thread about new task 
        newTaskEvent.signal();
    }
    
    public HttpTask createGetTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpVerb.GET, url, true);
    }

    public HttpTask createPostTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpVerb.POST, url, true);
    }

    public HttpTask createPutTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpVerb.PUT, url, true);
    }

    public HttpTask createDeleteTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpVerb.DELETE, url, true);
    }

    public void purgeTasks() {
        try {
            taskStorage.purge();
        } catch (Exception e) {
        }
    }
    
    public List<HttpTask> getPendingTasks(){
        try {
            return taskStorage.getPendingTasks();
        } catch (Exception e) {
            
        }
        return null;
    }
    
    public List<HttpTask> getActiveTasks(){
        try {
            return taskStorage.getActiveTasks();
        } catch (Exception e) {
            
        }
        
        return null;
    }

    public void shutdown() {
        taskQueueThread.stop();
    }

    public void addListener(HttpTaskResultListener resultListener) {
        addListener(resultListener, false);
    }

    public void addListener(HttpTaskResultListener resultListener, boolean notifyIfFailed) {
        this.notifyIfFailed = notifyIfFailed;
        this.resultListener = resultListener;
    }

    private class TaskCompletedListener implements HttpTaskResultListener {

        public void taskComplete(HttpTask task) {
            debug("Task (%d) for %s completed with status %d",
                    task.getTaskID(), task.getUri().toASCIIString(), task.lastResult().getStatus());
            try {
                if(task.isSuccess()){
                    taskStorage.deleteTask(task);
                } else {
                    task.triedOnce();
                    if(task.getMaxRetries() == task.getRetryCount()){
                        debug("Task (%d) exceeded max retry count, giving up...", task.getTaskID());
                        taskStorage.giveUpTask(task);
                    } else{
                        debug("Task (%d) failed, queueing for retry...", task.getTaskID());
                        taskStorage.saveTask(task);
                    }
                }

                if (null != resultListener) {
                    if (task.isSuccess() || notifyIfFailed) {
                        debug("Notifying listener...");
                        resultListener.taskComplete(task);
                    }
                }
            } catch (Exception e) {
                error("Error completing task, %s", e);
            }
        }
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            shutdown();
        }
    }
}
