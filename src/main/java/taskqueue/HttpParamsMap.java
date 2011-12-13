package taskqueue;

import java.util.HashMap;

/**
 * Created by evg.
 * Date: 08/12/11
 * Time: 13:42
 */
public class HttpParamsMap extends HashMap<String, String>{
    
    public HttpParamsMap(String[] paramNames, String[] paramValues){
        if (paramNames.length != paramValues.length) {
            throw new AssertionError("Parameter names count must equal to its values");
        }
        
        for (int i = 0; i < paramNames.length; i++){
            this.put(paramNames[i], paramValues[i]);
        }
    }
}
