package task;

import java.util.*;

/**
 * Created by evg.
 * Date: 08/12/11
 * Time: 13:54
 */
public class HttpHeaders extends HashMap<String, List<String>> {

    public static HttpHeaders fromArrays (String[] headerNames, String[] headerValues){
        if (headerNames.length != headerValues.length) {
            throw new IllegalArgumentException("Header names count must equal to its values");
        }

        HttpHeaders headersMap = new HttpHeaders();
        for (int i = 0; i < headerNames.length; i++){

            headersMap.addHeader(headerNames[i], headerValues[i]);
        }

        return headersMap;
    }

    public HttpHeaders addHeader(String name, String value){

        HttpCodecUtil.validateHeaderName(name);
        HttpCodecUtil.validateHeaderValue(value);

        if(!this.containsKey(name)){
            List<String> values = new ArrayList<String>();
            values.add(value);
            this.put(name, values);
        } else{
            this.get(name).add(value);
        }

        return this;
    }

    public String getFirstHeader(String name){

        if (containsKey(name))
            return get(name).get(0);
        else
            return "";
    }
}
