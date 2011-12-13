package httpclient;

import httptestserver.HttpRequestArrivedListener;
import httptestserver.HttpTestServer;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import taskqueue.HttpCookiesMap;
import taskqueue.HttpHeadersMap;
import taskqueue.HttpParamsMap;
import taskqueue.HttpTask;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 13:24
 */
public class HttpClientTest {
    private HttpTestServer httpTestServer;
    private int serverPort = 0;
    private final String baseUrlString = "http://localhost";
    private final String uriPath = "/test";
    
    @Before
    public void setUp() throws Exception {
        httpTestServer = new HttpTestServer();
        httpTestServer.start();

        serverPort = httpTestServer.getLocalPort();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSend() throws Exception {

        HttpTask task = createHttpTask(HttpMethod.GET, new HttpParamsMap(new String[]{"a"}, new String[] {"b"}),
                                        new HttpHeadersMap(new String[]{"h1", "h2"}, new String[]{"v1", "v2"}), null, null);
                
        httpTestServer.registerForRequestInterception();
        
        HttpClient client = new HttpClient(null);
        client.send(task);

        HttpRequest r = httpTestServer.waitUntilRequestArrives();
        
        assertEquals(HttpMethod.GET, r.getMethod());
        assertEquals(task.getUri().toASCIIString(), r.getUri());
        assertEquals(task.getHeaders().get("h1"), r.getHeader("h1"));
        assertEquals(task.getHeaders().get("h2"), r.getHeader("h2"));

        System.out.printf("Http request arrived for %s", r.getUri());
    }

    @Test
    public void testShutdown() throws Exception {

    }

    private HttpTask createHttpTask(HttpMethod method, HttpParamsMap params, HttpHeadersMap headers,
                                    HttpCookiesMap cookies, byte[] data) throws Exception {

        String url = String.format("%s:%d%s", baseUrlString, serverPort, uriPath);
        
        URI uri = new URI(url);
        HttpTask task = new HttpTask();

        task.setMethod(method);
        task.setUri(uri);
        task.setCookies(cookies);
        task.setParams(params);
        task.setHeaders(headers);
        task.setData(data);

        return task;
    }
}
