package httpclient;

import taskqueue.HttpTask;

/**
 * Created by evg.
 * Date: 27/11/11
 * Time: 18:06
 */
public interface HttpTaskCompletedListener {

    void taskCompleted(HttpTask task);
}
