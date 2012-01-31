package taskqueue;

import com.google.gson.Gson;
import task.HttpTask;

/**
 * Created by evg.
 * Date: 18/01/12
 * Time: 00:35
 */
public class DefaultTaskMarshaller implements TaskMarshaller{

    //TODO: The Default task marshaller uses Gson for JSON serialization. Either find better implementation such as Jackson or provide your own in the HttpTaskQueue constructor

    Gson gson = new Gson();

    public String marshal(HttpTask task) {
        return gson.toJson(task);
    }

    public HttpTask unmarshal(String json) {
        return gson.fromJson(json, HttpTask.class);
    }
}
