package taskqueue;

import httpclient.HttpTaskCompletedListener;
import org.jboss.netty.handler.codec.http.HttpMethod;

import java.net.URI;
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

    private Logger logger = Logger.getLogger("root");

    private TaskStorage taskStorage;
    private ManualResetEvent newTaskEvent = new ManualResetEvent();
    private HttpTaskQueueThread taskQueueThread;
    private HttpTaskResultListener resultListener;
    private boolean notifyIfFailed = false;

    public HttpTaskQueue(String appID){
        this.taskStorage = new TaskStorage(appID.concat(".db"));
        taskQueueThread = new HttpTaskQueueThread(taskStorage, newTaskEvent, new TaskCompletedListener());
        Executors.newSingleThreadExecutor().submit(taskQueueThread);
        
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public int addGetTask(String url, HttpParamsMap params, HttpHeadersMap headers, HttpCookiesMap cookies) throws Exception {

        int taskID = -1;
        HttpTask task = createHttpTask(HttpMethod.GET, url, params, headers, cookies, null);

        taskID = taskStorage.addTask(task);

        newTaskEvent.signal();
        return taskID;
    }

    public int addPostTask(String url, HttpParamsMap params, HttpHeadersMap headers, HttpCookiesMap cookies) throws Exception {

        int taskID = -1;
        HttpTask task = createHttpTask(HttpMethod.POST, url, params, headers, cookies, null);

        taskID = taskStorage.addTask(task);

        newTaskEvent.signal();
        return taskID;
    }

    public int addPutTask(String url, Map<String, String> params, Byte[] data, Map<String, String> headers, Map<String, String> cookies) throws Exception {

        return -1;
    }

    public int addDeleteTask(String url, Map<String, String> params, Map<String, String> headers, Map<String, String> cookies) throws Exception {

        return -1;
    }

    public void purgeTasks(){
        try {
            taskStorage.purge();
        } catch (Exception e) {
        }
    }

    public void shutdown(){
        taskQueueThread.stop();
    }

    public void addListener(HttpTaskResultListener resultListener){
        addListener(resultListener, false);
    }

    public void addListener(HttpTaskResultListener resultListener, boolean notifyIfFailed){
        this.notifyIfFailed = notifyIfFailed;
        this.resultListener = resultListener;
    }

    private HttpTask createHttpTask(HttpMethod method, String url, HttpParamsMap params, HttpHeadersMap headers,
                                           HttpCookiesMap cookies, byte[] data) throws Exception {

        URI uri = new URI(url);
        HttpTask task = new HttpTask();

        task.setMethod(method);
        task.setUri(uri);
        task.setCookies(cookies);
        task.setParams(params);
        task.setHeaders(headers);
        task.setData(data);

        return task;
    }

    private class TaskCompletedListener implements HttpTaskCompletedListener {

        public void taskCompleted(HttpTask task) {
            logger.log(Level.INFO, String.format("Task for %s completed with status %d", task.getUri().toASCIIString(), task.getLastResult().getStatus()));
            try {
                taskStorage.completeTask(task);
                
                if(resultListener != null){
                    if(task.isSuccess() || notifyIfFailed){
                        resultListener.taskComplete(task.getLastResult());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private class ShutdownHook extends Thread{
        @Override
        public void run(){
            shutdown();
        }
    }
}
