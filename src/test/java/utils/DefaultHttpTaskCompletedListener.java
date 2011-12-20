package utils;

import httpclient.HttpResponseCompletedListener;
import httpclient.HttpTaskCompletedListener;
import org.jboss.netty.channel.Channel;
import taskqueue.HttpTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by evg.
 * Date: 20/12/11
 * Time: 01:19
 */
public class DefaultHttpTaskCompletedListener implements HttpTaskCompletedListener{
    HttpTask task;
    CountDownLatch resultSignal;
    long timeout = 5;

    public DefaultHttpTaskCompletedListener(){
        this(5);
    }

    public DefaultHttpTaskCompletedListener(long timeout){
        this.timeout = timeout;
        resultSignal = new CountDownLatch(1);
    }

    public HttpTask getTaskResult() throws InterruptedException {
        resultSignal.await(timeout, TimeUnit.SECONDS);
        return task;
    }

    public void taskCompleted(HttpTask task) {
        this.task = task;
    }
}
