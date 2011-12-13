package taskqueue;

import java.util.HashMap;

/**
 * Created by evg.
 * Date: 08/12/11
 * Time: 13:54
 */
public class HttpHeadersMap extends HashMap<String,String>{

    public HttpHeadersMap(String[] headerNames, String[] headerValues){
        if (headerNames.length != headerValues.length) {
            throw new AssertionError("Header names count must equal to its values");
        }

        for (int i = 0; i < headerNames.length; i++){
            this.put(headerNames[i], headerValues[i]);
        }
    }
}
