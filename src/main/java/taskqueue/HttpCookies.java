package taskqueue;

import org.jboss.netty.handler.codec.http.CookieEncoder;

import java.util.HashMap;

/**
 * Created by evg.
 * Date: 08/12/11
 * Time: 13:48
 */
public class HttpCookies{

    private CookieEncoder encoder;

    public HttpCookies(){
        this.encoder = new CookieEncoder(false);
    }

    public static HttpCookies fromArrays(String[] cookieNames, String[] cookieValues){
        if ((cookieNames.length != cookieValues.length)) {
            throw new AssertionError("Cookies names count must equal to its values");
        }

        HttpCookies cookies = new HttpCookies();

        for (int i = 0; i < cookieNames.length; i++){
            cookies.add(cookieNames[i], cookieValues[i]);
        }

        return cookies;
    }

    public HttpCookies add(String name, String value){

        encoder.addCookie(name, value);

        return this;
    }

    public String getEncodedString(){
        return encoder.encode();
    }
}
