package task;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by evg.
 * Date: 08/12/11
 * Time: 13:42
 */
public class HttpParams extends HashMap<String, List<String>> {
    
    static final String encoding = "UTF-8";

    public static HttpParams fromArrays (String[] paramNames, String[] paramValues){
        if (paramNames.length != paramValues.length) {
            throw new IllegalArgumentException("Parameter names count must equal to its values");
        }

        HttpParams paramsMap = new HttpParams();
        for (int i = 0; i < paramNames.length; i++){

            paramsMap.add(paramNames[i], paramValues[i]);
        }

        return paramsMap;
    }

    public HttpParams add(String name, String value){

        HttpCodecUtil.validateParameterName(name);

        if(!this.containsKey(name)){
            List<String> values = new LinkedList<String>();
            values.add(value);
            this.put(name, values);
        } else{
            this.get(name).add(value);
        }
        
        return this;
    }

    public void appendFromQueryString(String queryString){

        for(String paramsPair : queryString.split("&")){
            String[] params = paramsPair.split("=");
            add(params[0], params[1]);
        }
    }

    public String getEncodedString() throws UnsupportedEncodingException {
        
        StringBuilder sb = new StringBuilder();
        String separator = "";

        for (Map.Entry<String, List<String>> entry : this.entrySet() ){

            for(String value : entry.getValue()){
                sb.append(separator);
                separator = "&";
                sb.append(URLEncoder.encode(entry.getKey(), encoding));

                if(!HttpCodecUtil.isEmptyString(value)){
                    sb.append("=");
                    sb.append(URLEncoder.encode(value,encoding));
                }
            }
        }
        return sb.toString();
    }
}
