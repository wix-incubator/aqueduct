package com.wixpress.aqueduct.task;

import java.net.URI;

/**
 * Created by evg.
 * Date: 30/01/12
 * Time: 14:06
 */
public class HttpTaskFactory {

    public static HttpTask create(String verb, String url, boolean persistent) throws Exception {

        URI uri = new URI(url);

        HttpTask task;

        if(persistent){
            task = new HttpTask();
        } else{
            task = new VolatileHttpTask();
        }

        task.setVerb(verb);
        task.setUri(uri);

        return task;
    }

}
