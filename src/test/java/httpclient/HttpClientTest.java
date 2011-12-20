package httpclient;

import httptestserver.HttpTestServer;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import taskqueue.HttpCookies;
import taskqueue.HttpHeaders;
import taskqueue.HttpParams;
import taskqueue.HttpTask;
import utils.DefaultHttpTaskCompletedListener;

import java.net.URI;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;


/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 13:24
 */
public class HttpClientTest {
    private HttpTestServer httpTestServer;
    private DefaultHttpTaskCompletedListener taskListener;
    private int serverPort = 0;
    private final String baseUrlString = "http://localhost";
    private final String uriPath = "/test";
    
    @Before
    public void setUp() throws Exception {

        taskListener = new DefaultHttpTaskCompletedListener();
        httpTestServer = new HttpTestServer();
        httpTestServer.start();

        serverPort = httpTestServer.getLocalPort();
    }

    @After
    public void tearDown() throws Exception {
        httpTestServer.stop();
    }

    @Test
    public void testSendGetSuccess() throws Exception {

        HttpTask task = createHttpTask(HttpMethod.GET, HttpParams.fromArrays(new String[]{"a"}, new String[]{"b"}),
                HttpHeaders.fromArrays(new String[]{"h1", "h2"}, new String[]{"v1", "v2"}), null, null);
                
        httpTestServer.registerForRequestInterception();
        
        HttpClient client = new HttpClient(taskListener);
        client.send(task);

        HttpRequest r = httpTestServer.waitUntilRequestArrives();

        assertThat(r.getHeaderNames(), hasItems("h1", "h2"));

        assertEquals(HttpMethod.GET, r.getMethod());
        assertEquals(task.getUri().toASCIIString(), r.getUri());

        System.out.printf("Http request arrived for %s\n", r.getUri());
    }

    @Test
    public void testShutdown() throws Exception {

    }

    private HttpTask createHttpTask(HttpMethod method, HttpParams params, HttpHeaders headers,
                                    HttpCookies cookies, byte[] data) throws Exception {

        String url = String.format("%s:%d%s", baseUrlString, serverPort, uriPath);
        
        URI uri = new URI(url);
        HttpTask task = new HttpTask();

        task.setMethod(method);
        task.setUri(uri);
        task.setHeaders(headers);
        task.setData(data);

        return task;
    }
}
