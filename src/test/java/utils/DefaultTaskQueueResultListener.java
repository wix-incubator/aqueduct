package utils;

import task.HttpTask;
import taskqueue.HttpTaskResultListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by evg.
 * Date: 18/12/11
 * Time: 11:49
 */
public class DefaultTaskQueueResultListener implements HttpTaskResultListener {
    
    HttpTask taskResult;
    CountDownLatch resultSignal;
    long timeout = 5;


    public DefaultTaskQueueResultListener(){
        this(5, TimeUnit.SECONDS);
    }

      public DefaultTaskQueueResultListener(long timeout, TimeUnit timeUnit){
        this(timeout, timeUnit, 1);
    }
    
    public DefaultTaskQueueResultListener(long timeout, TimeUnit timeUnit, int retryCount){
        this.timeout = timeUnit.toMillis(timeout);
        resultSignal = new CountDownLatch(retryCount);
    }
    
    public HttpTask getTask() throws InterruptedException {
        resultSignal.await(timeout, TimeUnit.MILLISECONDS);
        return taskResult;
    }

    public void taskComplete(HttpTask result) {
        taskResult = result;
        resultSignal.countDown();
    }
}
