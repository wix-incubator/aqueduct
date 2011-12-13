package taskqueue;

import java.util.concurrent.TimeUnit;

/**
 * User: evg
 * Date: 22/11/11
 * Time: 12:20
 */

public class ManualResetEvent {
    private final Object lock = new Object();

    public void signal(){
        synchronized (lock){
            lock.notify();
        }
    }

    public void waitSignal() throws InterruptedException {
        synchronized (lock){
            lock.wait();
        }
    }

    public void waitSignalWithTimeout(long interval, TimeUnit timeUnit) throws InterruptedException {
        synchronized (lock){
            lock.wait(timeUnit.toMillis(interval));
        }
    }
}
