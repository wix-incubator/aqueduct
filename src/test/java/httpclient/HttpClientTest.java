package httpclient;

import httptestserver.HttpTestServer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import task.*;
import taskqueue.HttpTaskResultListener;
import utils.DefaultTaskQueueResultListener;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import static task.HttpConstants.HttpVerb;


/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 13:24
 */
public class HttpClientTest {
    HttpTestServer testServer;
    HttpTaskResultListener listener;
    HttpClient client;

    @Before
    public void setUp() throws Exception {

        listener = new DefaultTaskQueueResultListener();
        client = new HttpClient(listener);

        testServer = new HttpTestServer();
        testServer.start();
    }

    @After
    public void tearDown() throws Exception {
        testServer.stop();
    }

    @Test
    public void testSendGetSuccess() throws Exception {

        HttpTask task = HttpTaskFactory.create(HttpVerb.GET, testServer.getLocalUrl(), false)
                .withParameters(HttpParams.fromArrays(new String[]{"a"}, new String[]{"b"}))
                .withHeaders(HttpHeaders.fromArrays(new String[]{"h1", "h2"}, new String[]{"v1", "v2"}));

        testServer.registerForRequestInterception();

        client.send(task);
        HttpRequest r = testServer.waitUntilRequestArrives();

        assertEquals(HttpVerb.GET, r.getMethod().getName());
        assertThat(r.getHeaderNames(), hasItems("h1", "h2"));
        assertEquals(task.getUri().toASCIIString(), r.getUri());
    }

    @Test
    public void testShutdown() throws Exception {

    }
}
