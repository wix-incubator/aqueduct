package com.wixpress.aqueduct.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import com.wixpress.aqueduct.task.HttpHeaders;
import com.wixpress.aqueduct.task.HttpTask;
import com.wixpress.aqueduct.task.HttpTaskResult;

import java.util.Arrays;

/**
 * Created by evg.
 * Date: 06/02/12
 * Time: 14:09
 */

public class HttpTaskIsEqual extends BaseMatcher<HttpTask> {

    private HttpTask taskA;

    private HttpTaskIsEqual(HttpTask task) {
        this.taskA = task;
    }

    public boolean matches(Object item) {

        if (null == item) return false;

        HttpTask taskB = (HttpTask) item;

        return taskA.isSanitized() == ((HttpTask) item).isSanitized()
                && taskA.isSuccess() == taskB.isSuccess()
                && taskA.getConnectTimeoutMillis() == taskB.getConnectTimeoutMillis()
                && Arrays.equals(taskA.getData(), taskB.getData())
                // && taskA.getTtl().equals(taskB.getTtl())
                && taskA.getHeaders().size() == taskB.getHeaders().size()
                && taskA.getIdentity().equals(taskB.getIdentity())
                && taskA.getMaxRetries() == taskB.getMaxRetries()
                && taskA.getRequestTimeoutMillis() == taskB.getConnectTimeoutMillis()
                && taskA.getRetryCount() == taskB.getRetryCount()
                && taskA.getSuccessResponseCodes() == taskB.getSuccessResponseCodes()
                && taskA.getTaskID() == taskB.getTaskID()
                && taskA.getUri().toASCIIString().equals(taskB.getUri().toASCIIString())
                && taskA.getVerb().equals(taskB.getVerb())
                && taskA.getResults().equals(taskB.getResults());
    }

    public void describeTo(Description description) {

        description.appendText("Task: ")
                .appendValue(taskA.getVerb())
                .appendText(", URL: ")
                .appendValue(taskA.getUri().toASCIIString())
                .appendText(", with Success: ")
                .appendValue(taskA.isSuccess())
                .appendText(", with Results: ")
                .appendValueList("Result ", "##", ";;", taskA.getResults());
    }

    public static Matcher<HttpTask> equalsToTask(HttpTask task) {
        return new HttpTaskIsEqual(task);
    }

    public static HttpTaskResult failedResult(){
        HttpTaskResult failed = new HttpTaskResult();
        failed.setStatus(503);
        failed.setCause(new Exception("failed"));

        return failed;
    }

    public static HttpTaskResult successResult(){
        HttpTaskResult success = new HttpTaskResult();
        success.setStatus(200);
        success.setContent("ok".getBytes());
        success.setHeaders(new HttpHeaders().addHeader("h1", "v1").addHeader("h2", "v2"));

        return success;
    }
}