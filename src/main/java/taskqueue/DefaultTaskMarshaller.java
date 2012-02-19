package taskqueue;

import com.google.gson.Gson;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import task.HttpTask;

import java.io.IOException;

/**
 * Created by evg.
 * Date: 18/01/12
 * Time: 00:35
 */
public class DefaultTaskMarshaller implements TaskMarshaller{

    //TODO: The Default task marshaller uses Gson for JSON serialization. Either find better implementation such as Jackson or provide your own in the HttpTaskQueue constructor

    Gson gson = new Gson();
    ObjectMapper mapper = new ObjectMapper();



    public String marshal(HttpTask task) throws Exception {

        //return gson.toJson(task);

//        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
//        mapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.ANY);
//        mapper.configure(SerializationConfig.Feature.WRAP_EXCEPTIONS, false);

        return mapper.writeValueAsString(task);
    }

    public HttpTask unmarshal(String json) throws Exception{
        
        class A{
            String s = "a";
            int  i = 0;
        }
        
        A a1 = new A();

        
        //mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS,false);
        //mapper.configure(DeserializationConfig.Feature.WRAP_EXCEPTIONS, true);
        mapper.setVisibility(JsonMethod.SETTER, JsonAutoDetect.Visibility.NONE);


        return mapper.readValue(json, HttpTask.class);
        //return gson.fromJson(json, HttpTask.class);
    }
}
