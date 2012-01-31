package task;

import java.util.concurrent.CountDownLatch;

/**
 * Created by evg.
 * Date: 30/01/12
 * Time: 14:14
 */
public class VolatileHttpTask extends HttpTask{

    private CountDownLatch completedSignal;

    public VolatileHttpTask() {

        completedSignal = new CountDownLatch(1);
        setTaskID(System.identityHashCode(this));
    }

    @Override
    public void addResult(HttpTaskResult result) {
        super.addResult(result);
        completedSignal.countDown();
    }

    @Override
    public HttpTaskResult lastResult() {
        try {
            completedSignal.await();
        } catch (InterruptedException e) {

        }

        return super.lastResult();
    }
}
