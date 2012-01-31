package taskqueue;

import task.HttpTask;

/**
 * Created by evg.
 * Date: 18/01/12
 * Time: 00:33
 */
public interface TaskMarshaller {
    public String marshal(HttpTask task);
    public HttpTask unmarshal(String json);
}
