package taskqueue;

import org.jboss.netty.handler.codec.http.HttpMethod;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: evg
 * Date: 09/11/11
 * Time: 16:35
 */
public class HttpTask {

    private int taskID = -1;
    private URI uri;
    private HttpMethod method;
    private Map<String, String> params = null;
    private Map<String, String> cookies = null;
    private Map<String, String> headers = null;
    private byte [] data = null;

    private Date expiresOn;
    private int maxRetries = 3;
    private int retryCount = 0;
    
    private int[] successResponseCodes = new int[] {200};

    private boolean isSuccess = false;
    private Throwable lastError;

    private List<HttpTaskResult> results = new ArrayList<HttpTaskResult>();
    int lastResult = -1;

    public HttpTask(){}

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<String, String> getHeaders() {

        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public Date getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(Date expiresOn) {
        this.expiresOn = expiresOn;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Throwable getLastError() {
        return lastError;
    }

    public void setLastError(Throwable lastError) {
        this.lastError = lastError;
    }

    public int[] getSuccessResponseCodes() {
        return successResponseCodes;
    }

    public void setSuccessResponseCodes(int[] successResponseCodes) {
        this.successResponseCodes = successResponseCodes;
    }

    public void addResult(HttpTaskResult result){
        results.add(result);
        lastResult++;
    }

    public HttpTaskResult getLastResult(){
        return results.get(lastResult);
    }
}
