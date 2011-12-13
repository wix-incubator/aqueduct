package taskqueue;

/**
 * Created by evg.
 * Date: 10/12/11
 * Time: 23:57
 */
public interface HttpTaskResultListener {
    public void taskComplete(HttpTaskResult result);
}
