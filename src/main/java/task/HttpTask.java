package task;

import static task.HttpConstants.*;

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

    private static final int MAX_URL_LEN = 2048;

    private int taskID = -1;
    private URI uri;
    private String verb;
    private HttpHeaders headers = new HttpHeaders();
    private byte[] data = null;

    private String identity;

    private Date expiresOn;
    private int maxRetries = 3;
    private int retryCount = 0;
    
    private long timeoutMillis = 5000; // 5 seconds default connection timeout
    private long minRetryInterval = 1000 ; // minimum interval between retries;

    private int[] successResponseCodes = new int[]{200};

    private boolean isSuccess = false;

    private boolean isSanitized = false;

    private List<HttpTaskResult> results = new ArrayList<HttpTaskResult>();

    public HttpTask() {
    }


    ///////////  Task builders //////////////////////////

    public HttpTask withHeaders(HttpHeaders headers) {
        this.headers.putAll(headers);
        return this;
    }

    public HttpTask withCookies(HttpCookies cookies) {
        if (null != cookies) {
            this.headers.addHeader("Cookie", cookies.getEncodedString());
        }

        return this;
    }

    public HttpTask withParameters(HttpParams params) {

        if (null != params) {
            try {
                String paramsString = params.getEncodedString();

                if (!HttpCodecUtil.isEmptyString(uri.getQuery())) {
                    paramsString = paramsString.concat("&").concat(uri.getQuery());
                }

                uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), paramsString, uri.getFragment());

            } catch (Exception e) {
                throw new IllegalArgumentException("Bad parameters", e);
            }
        }
        return this;
    }

    public HttpTask withData(byte[] data, String contentType) {

        if (null != data) {
            this.data = data;
            headers.addHeader(HeaderNames.CONTENT_LENGTH, String.valueOf(data.length));
            headers.addHeader(HeaderNames.CONTENT_TYPE, contentType);
        }
        return this;
    }

    public HttpTask withSuccessResponseCodes(int[] codes) {
        this.successResponseCodes = codes;

        return this;
    }

    public HttpTask withIdentity(String identity) {
        this.identity = identity;

        return this;
    }

    public HttpTask willExpireOn(Date expireOn) {
        this.expiresOn = expireOn;

        return this;
    }

    public HttpTask withMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;

        return this;
    }


////////// Getters ////////////////////

    public URI getUri() {
        return uri;
    }

    public byte[] getData() {
        return data;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getVerb() {
        return verb;
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

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getRetryCount() {
        return retryCount;
    }


    public void triedOnce() {
        this.retryCount++;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int[] getSuccessResponseCodes() {
        return successResponseCodes;
    }

    public void addResult(HttpTaskResult result) {
        results.add(result);
    }

    public HttpTaskResult lastResult() {
        if (results.size() > 0)
            return results.get(results.size() - 1);

        return null;
    }

    public HttpTask sanitize() throws Exception {

        if (null == uri.getHost()) throw new IllegalArgumentException("Invalid URL");

        if (!uri.getScheme().equalsIgnoreCase("http"))
            throw new IllegalArgumentException("Only HTTP protocol is supported");


        if (canPassParamsAsBody()) {
            data = uri.getQuery().getBytes();
            headers.addHeader(HeaderNames.CONTENT_TYPE, HttpContentType.X_WWW_FORM);

            uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
        }

        if (uri.toASCIIString().length() > MAX_URL_LEN) {
            throw new IllegalArgumentException("URL is too long (max 2048 bytes)");
        }

        this.isSanitized = true;

        return this;
    }

    private boolean canPassParamsAsBody() {

        if (verb.equals(HttpVerb.GET)) return false;

        if (verb.equals(HttpVerb.POST) || verb.equals(HttpVerb.PUT)) {
            if (null == data) {
                return true;
            }
        }

        return false;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getIdentity() {
        return identity;
    }

    public boolean isSanitized() {
        return isSanitized;
    }
}
