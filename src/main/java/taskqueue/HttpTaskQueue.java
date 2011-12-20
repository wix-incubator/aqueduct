package taskqueue;

import httpclient.HttpTaskCompletedListener;
import org.jboss.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 09/11/11
 * Time: 16:34
 */
public class HttpTaskQueue {

    public static final class HttpContentType{
        public static final String X_WWW_FORM = "application/x-www-form-urlencoded";
        public static final String XML = "application/xml";
        public static final String JSON = "application/json";
    }

    private Logger logger = Logger.getLogger("root");

    private TaskStorage taskStorage;
    private ManualResetEvent newTaskEvent = new ManualResetEvent();
    private HttpTaskQueueThread taskQueueThread;
    private HttpTaskResultListener resultListener;
    private boolean notifyIfFailed = false;

    public HttpTaskQueue(String appID) {
        this.taskStorage = new TaskStorage(appID.concat(".db"));
        taskQueueThread = new HttpTaskQueueThread(taskStorage, newTaskEvent, new TaskCompletedListener());
        Executors.newSingleThreadExecutor().submit(taskQueueThread);

        // need shutdown hook in order to terminate all working threads properly
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public int addGetTask(String url) throws Exception {
        return addGetTask(url, null, null, null);
    }

    public int addGetTask(String url, HttpParams params) throws Exception {
        return addGetTask(url, params, null, null);
    }

    public int addGetTask(String url, HttpParams params, HttpHeaders headers) throws Exception {
        return addGetTask(url, params, headers, null);
    }
    
    public int addGetTask(String url, HttpParams params, HttpHeaders headers, HttpCookies cookies) throws Exception {

        int taskID = -1;
        HttpTask task = HttpTask.create(HttpMethod.GET, url, params, headers, cookies, null, null);

        taskID = taskStorage.addTask(task);

        newTaskEvent.signal();
        return taskID;
    }

    public int addPostTask(String url, HttpParams params, HttpHeaders headers, HttpCookies cookies) throws Exception {

        int taskID = -1;
        HttpTask task = HttpTask.create(HttpMethod.POST, url, params, headers, cookies, null, HttpContentType.X_WWW_FORM);

        taskID = taskStorage.addTask(task);

        newTaskEvent.signal();
        return taskID;
    }

    public int addPostTask(String url, HttpParams params, HttpHeaders headers, HttpCookies cookies, byte[] data, String contentType) throws Exception {

        int taskID = -1;
        HttpTask task = HttpTask.create(HttpMethod.POST, url, params, headers, cookies, data, contentType);

        taskID = taskStorage.addTask(task);

        newTaskEvent.signal();
        return taskID;
    }

    public int addPutTask(String url, byte[] data, String contentType, HttpParams params, HttpHeaders headers, HttpCookies cookies) throws Exception {

        int taskID = -1;
        HttpTask task = HttpTask.create(HttpMethod.PUT, url, params, headers, cookies, data, contentType);

        taskID = taskStorage.addTask(task);

        newTaskEvent.signal();
        return taskID;
    }

    public int addDeleteTask(String url, Map<String, String> params, Map<String, String> headers, Map<String, String> cookies) throws Exception {

        return -1;
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

    private class TaskCompletedListener implements HttpTaskCompletedListener {

        public void taskCompleted(HttpTask task) {
            logger.log(Level.INFO, String.format("Task for %s completed with status %d", task.getUri().toASCIIString(), task.getLastResult().getStatus()));
            try {
                taskStorage.completeTask(task);

                if (resultListener != null) {
                    if (task.isSuccess() || notifyIfFailed) {
                        resultListener.taskComplete(task.getLastResult());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
