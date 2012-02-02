# Aqueduct 
Aqueduct is durable HTTP client designed for efficient server to server communication. Its asynchronous persistent 
HttpTaskQueue API can be used for reliable notifications and messages between applications and services. Each queued task
is stored transactionally first. All the network communication work is performed in background by http client built 
on [Netty NIO framework](http://netty.io/). Aqueduct also provides Synchronous API and can be used as simple Http Client

## Working asynchronously

Creating the TaskQueue instance and results callback interface:

```java
HttpTaskQueue taskQueue = new HttpTaskQueue("my_app");

HttpTaskResultListener listener = new DefaultTaskQueueResultListener();
taskQueue.addListener(listener, false);

private class DefaultTaskQueueResultListener implements HttpTaskResultListener {

    public void taskComplete(HttpTask result) {
        // do something with completed task...
    }
}
```

appID string in the constructor will be used for name of database file. If omitted the name of data file will be taskqueue.db. It is useful where several application are running on the same server and each one has its own task queue.

addListener receives second parameter notifyIfFailed. If true the callback will be called when tasks fails upon each retry. Otherwise only successful results will be pushed.

## Queuing a task

```java
byte [] payload = new byte[4096];
Arrays.fill(payload, (byte) '1');

// Big PUT with custom success response code
taskQueue.queue(
    taskQueue.createPutTask("http://archive.storage.com/data")
        .withIdentity("my_unique_guid")
        .withParameters(new HttpParams().add("param1", "value1"))
        .withHeaders((new HttpHeaders()).addHeader("Test-Status", "409"))
        .withSuccessResponseCodes(new int[]{409})
        .withData(payload, HttpContentType.JSON)
);
```

## Working with Synchronous API.

```java
HttpTaskPerformer taskPerformer = new HttpTaskPerformer();

HttpTaskResult result = taskPerformer.perform(
    taskPerformer.createGetTask("http://static.wix.com/index.html")
            .withParameters((new HttpParams()).add("foo", "bar"))
);

if(200 == result.getStatus()){
    String content = new String(result.getContent());
}
```

## HttpTestServer
Using Test Http Server

```java
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
                                .withHeaders(HttpHeaders.fromArrays(new String[]{"h1", "h2"}, new String[]{"v1", "v2"}));

        testServer.registerForRequestInterception();
        client.send(task);

        HttpRequest r = testServer.waitUntilRequestArrives();

        assertEquals(HttpVerb.GET, r.getMethod().getName());
        assertThat(r.getHeaderNames(), hasItems("h1", "h2"));
    }
}
```
