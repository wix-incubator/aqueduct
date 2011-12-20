package taskqueue;

import org.jboss.netty.handler.codec.http.HttpMethod;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: evg
 * Date: 09/11/11
 * Time: 16:35
 */
public class HttpTask {

    private int taskID = -1;
    private URI uri;
    private HttpMethod method;
    private HttpHeaders headers = null;
    private byte[] data = null;

    private Date expiresOn;
    private int maxRetries = 3;
    private int retryCount = 0;

    private int[] successResponseCodes = new int[]{200};

    private boolean isSuccess = false;
    private Throwable lastError;

    private List<HttpTaskResult> results = new ArrayList<HttpTaskResult>();
    int lastResult = -1;

    public HttpTask() {
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public HttpHeaders getHeaders() {

        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
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

    public void addResult(HttpTaskResult result) {
        results.add(result);
        lastResult++;
    }

    public HttpTaskResult getLastResult() {
        if(lastResult >= 0)
            return results.get(lastResult);

        return null;
    }

    public static HttpTask create(HttpMethod method, String url, HttpParams params, HttpHeaders headers,
                                  HttpCookies cookies, byte[] data, String contentType) throws Exception {

        URI uri = new URI(url);

        validateTaskRequest(method, uri, params, data, contentType);

        if (null != params) {
            String paramsString = params.getEncodedString();

            if (isQueryRequiredInURL(method, contentType)) {
                if(!HttpCodecUtil.isEmptyString(uri.getQuery())){
                    paramsString = paramsString.concat("&").concat(uri.getQuery());
                }
                uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), paramsString, uri.getFragment());
            } else {
                data = paramsString.getBytes();
            }
        }

        if(null != data){
            if(null == headers) headers = new HttpHeaders();
            headers.addHeader(CONTENT_LENGTH, String.valueOf(data.length));
            headers.addHeader(CONTENT_TYPE, contentType);
        }

        if (null != cookies) {
            headers.addHeader(COOKIE, cookies.getEncodedString());
        }

        HttpTask task = new HttpTask();

        task.setMethod(method);
        task.setUri(uri);
        task.setHeaders(headers);
        task.setData(data);

        return task;
    }

    private static void validateTaskRequest(HttpMethod method, URI uri, HttpParams params, byte[] data, String contentType) throws Exception {
        if (null == uri.getHost()) throw new IllegalArgumentException("Invalid URL");

        if (!uri.getScheme().equalsIgnoreCase("http"))
            throw new IllegalArgumentException("Only HTTP protocol is supported");

        if(data != null && params != null && !isQueryRequiredInURL(method, contentType))
            throw new IllegalArgumentException("Cannot mix params and data for x-www-form-urlencoded request");

        if(data != null && HttpCodecUtil.isEmptyString(contentType))
            throw new IllegalArgumentException("Data content type is required");

    }

    private static boolean isQueryRequiredInURL(HttpMethod method, String contentType) {

        if (HttpMethod.GET == method) return true;

        if (HttpMethod.POST == method || HttpMethod.PUT == method) {
            if (!HttpTaskQueue.HttpContentType.X_WWW_FORM.equalsIgnoreCase(contentType)) return true;
        }

        return false;
    }
}
