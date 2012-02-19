package taskqueue;

import org.codehaus.jackson.JsonGenerationException;
import task.HttpTask;

import java.io.IOException;

/**
 * Created by evg.
 * Date: 18/01/12
 * Time: 00:33
 */
public interface TaskMarshaller {
    public String marshal(HttpTask task) throws Exception;
    public HttpTask unmarshal(String json) throws Exception;
}
