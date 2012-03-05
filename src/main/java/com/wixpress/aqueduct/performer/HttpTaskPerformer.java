package com.wixpress.aqueduct.performer;

import com.wixpress.aqueduct.httpclient.HttpClient;
import com.wixpress.aqueduct.task.*;

/**
 * Created by evg.
 * Date: 30/01/12
 * Time: 12:23
 */
public class HttpTaskPerformer {

    private HttpClient httpClient = new HttpClient(null);

    public HttpTaskPerformer() {
        // need shutdown hook in order to terminate all working threads properly
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }


    public HttpTaskResult perform(HttpTask task) throws Exception {

        if(!(task instanceof VolatileHttpTask)){
            throw new IllegalArgumentException("task must be instance of VolatileHttpTask");
        }

        httpClient.send(task.sanitize());

        return task.lastResult();
    }

    public HttpTask createGetTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpConstants.HttpVerb.GET, url, false);
    }

    public HttpTask createPostTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpConstants.HttpVerb.POST, url, false);
    }

    public HttpTask createPutTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpConstants.HttpVerb.PUT, url, false);
    }

    public HttpTask createDeleteTask(String url) throws Exception {
        return HttpTaskFactory.create(HttpConstants.HttpVerb.DELETE, url, false);
    }

    public void shutdown() {
        httpClient.shutdown();
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            shutdown();
        }
    }
}
