package taskqueue;

import java.util.HashMap;

/**
 * Created by evg.
 * Date: 08/12/11
 * Time: 13:48
 */
public class HttpCookiesMap extends HashMap<String,String>{

    public HttpCookiesMap(String[] cookieNames, String[] cookieValues){
        if ((cookieNames.length != cookieValues.length)) {
            throw new AssertionError("Cookies names count must equal to its values");
        }

        for (int i = 0; i < cookieNames.length; i++){
            this.put(cookieNames[i], cookieValues[i]);
        }
    }
}
