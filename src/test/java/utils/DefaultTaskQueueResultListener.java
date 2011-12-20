package utils;

import taskqueue.HttpTaskResult;
import taskqueue.HttpTaskResultListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by evg.
 * Date: 18/12/11
 * Time: 11:49
 */
public class DefaultTaskQueueResultListener implements HttpTaskResultListener{
    
    HttpTaskResult taskResult;
    CountDownLatch resultSignal;
    long timeout = 5;
    
    public DefaultTaskQueueResultListener(){
        this(5);
    }
    
    public DefaultTaskQueueResultListener(long timeout){
        this.timeout = timeout;
        resultSignal = new CountDownLatch(1);
    }
    
    public HttpTaskResult getTaskResult() throws InterruptedException {
        resultSignal.await(timeout, TimeUnit.SECONDS);
        return taskResult;
    }
    
    public void taskComplete(HttpTaskResult result) {
        taskResult = result;
        resultSignal.countDown();
    }
}
