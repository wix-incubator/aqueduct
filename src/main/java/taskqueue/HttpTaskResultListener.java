package taskqueue;

import task.HttpTask;

/**
 * Created by evg.
 * Date: 10/12/11
 * Time: 23:57
 */
public interface HttpTaskResultListener {
    public void taskComplete(HttpTask result);
}
