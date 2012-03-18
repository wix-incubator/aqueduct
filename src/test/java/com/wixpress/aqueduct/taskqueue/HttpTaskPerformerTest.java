package com.wixpress.aqueduct.taskqueue;

import com.wixpress.aqueduct.httptestserver.HttpTestServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.wixpress.aqueduct.performer.HttpTaskPerformer;
import com.wixpress.aqueduct.task.HttpParams;
import com.wixpress.aqueduct.task.HttpTaskResult;

import static org.junit.Assert.*;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by evg.
 * Date: 30/01/12
 * Time: 13:11
 */
public class HttpTaskPerformerTest {
    HttpTaskPerformer taskPerformer;
    HttpTestServer testServer;

    @Before
    public void setup() {

        Logger logger = Logger.getLogger("root");
        logger.setLevel(Level.ALL);

        ConsoleHandler handler = new ConsoleHandler();
        logger.addHandler(handler);

        testServer = new HttpTestServer();
        testServer.start();

        taskPerformer = new HttpTaskPerformer();
    }

    @After
    public void tearDown() {
        taskPerformer.shutdown();
        testServer.stop();
    }

    @org.junit.Test
    public void testPerform() throws Exception {

        HttpTaskResult result = taskPerformer.perform(
                taskPerformer.createGetTask(testServer.getLocalUrl())
                .withParameters((new HttpParams()).add("foo", "bar"))
        );

        assertEquals(200, result.getStatus());
    }

    @Test
    public void testPerformMany() throws Exception {
        HttpTaskResult result = null;

        for (int i = 0; i < 5; i++) {
            result = taskPerformer.perform(
                    taskPerformer.createGetTask("http://static.wix.com/index.html") //testServer.getLocalUrl())
            );

            System.out.println(i);
            System.out.println(result.getStatus());
        }

        String content = new String(result.getContent());
        assertEquals(200, result.getStatus());
    }


}
