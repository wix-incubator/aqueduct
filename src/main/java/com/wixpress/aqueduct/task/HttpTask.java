package com.wixpress.aqueduct.task;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import com.wixpress.aqueduct.taskqueue.TaskMarshaller;

import static com.wixpress.aqueduct.task.HttpConstants.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private String identity = "";

    private long ttl = 0;
    private int maxRetries = 3;
    private int retryCount = 0;

    private int connectTimeoutMillis = 5000; // 5 seconds default connection timeout
    private long requestTimeoutMillis = 10000; // 10 seconds overall request timeout
    private long minRetryInterval = 1000; // minimum interval between retries;

    private int[] successResponseCodes = new int[]{200};

    private boolean success = false;

    private boolean sanitized = false;

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

    public HttpTask withTTL(long ttl, TimeUnit timeUnit) {

        this.ttl = timeUnit.toMillis(ttl);
        return this;
    }

    public HttpTask withMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;

        return this;
    }

    public HttpTask withConnectTimeout(int timeout, TimeUnit timeUnit) {
        this.connectTimeoutMillis = (int) timeUnit.toMillis(timeout);

        return this;
    }

    public HttpTask withRequestTimeout(long timeout, TimeUnit timeUnit) {
        this.requestTimeoutMillis = timeUnit.toMillis(timeout);

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

    public long getTtl() {
        return ttl;
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
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

        this.sanitized = true;

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
        return sanitized;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public long getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    public List<HttpTaskResult> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpTask task = (HttpTask) o;

        if (connectTimeoutMillis != task.connectTimeoutMillis) return false;
        if (sanitized != task.sanitized) return false;
        if (success != task.success) return false;
        if (maxRetries != task.maxRetries) return false;
        if (minRetryInterval != task.minRetryInterval) return false;
        if (requestTimeoutMillis != task.requestTimeoutMillis) return false;
        if (retryCount != task.retryCount) return false;
        if (taskID != task.taskID) return false;
        if (!Arrays.equals(data, task.data)) return false;
        if (ttl != task.ttl) return false;
        if (!headers.equals(task.headers)) return false;
        if (!identity.equals(task.identity)) return false;
        if (!results.equals(task.results)) return false;
        if (!Arrays.equals(successResponseCodes, task.successResponseCodes)) return false;
        if (!uri.equals(task.uri)) return false;
        if (!verb.equals(task.verb)) return false;

        return results.equals(task.getResults());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String separator = " ## ";

        sb.append("taskID=").append(taskID).append(separator);
        sb.append("uri=").append(uri.toASCIIString()).append(separator);
        sb.append("verb=").append(verb).append(separator);

        //headers
        sb.append("headers=");
        for (String h : headers.keySet()) {
            for (String v : headers.get(h)) {
                sb.append(h).append(":").append(v).append(";");
            }
        }
        sb.append(separator);

        if (null != data) sb.append("data=").append(new BASE64Encoder().encode(data));
        sb.append("identity=").append(identity).append(separator);
        sb.append("ttl=").append(ttl).append(separator);
        sb.append("maxRetries=").append(maxRetries).append(separator);
        sb.append("retryCount=").append(retryCount).append(separator);
        sb.append("connectTimeoutMillis=").append(connectTimeoutMillis).append(separator);
        sb.append("requestTimeoutMillis=").append(requestTimeoutMillis).append(separator);
        sb.append("minRetryInterval=").append(minRetryInterval).append(separator);
        sb.append("successResponseCodes=");
        for (int i = 0; successResponseCodes != null && i < successResponseCodes.length; ++i)
            sb.append(i == 0 ? "" : ",").append(successResponseCodes[i]);
        sb.append(separator);
        sb.append("success=").append(success).append(separator);
        sb.append("sanitized=").append(sanitized).append(separator);
        sb.append("results=").append(results);


        return sb.toString();
    }

    public static class DefaultMarshaler implements TaskMarshaller {

        //TODO: either delete this ugly code or make it readable

        static final BASE64Encoder encoder = new BASE64Encoder();
        static final BASE64Decoder decoder = new BASE64Decoder();
        static final char valTerminator = '\n';
        static final char keyTerminator = '=';

        @Override
        public String marshal(HttpTask task) throws Exception {

            final StringBuilder sb = new StringBuilder();


            appendValue(sb, "taskID", task.taskID);

            // uri.toASCIIString() is not good here, utf8 urls will be double-url-encoded (?)
            appendValue(sb, "uri", task.uri.toString());
            appendValue(sb, "verb", task.verb);

            //headers
            appendHeaders(sb, task.headers, keyTerminator, valTerminator);
            sb.append(valTerminator);

            if (null != task.data) appendValue(sb, "data", encoder.encode(task.data));
            appendValue(sb, "identity", task.identity);
            appendValue(sb, "ttl", task.ttl);
            appendValue(sb, "maxRetries", task.maxRetries);
            appendValue(sb, "retryCount", task.retryCount);
            appendValue(sb, "connectTimeoutMillis", task.connectTimeoutMillis);
            appendValue(sb, "requestTimeoutMillis", task.requestTimeoutMillis);
            appendValue(sb, "minRetryInterval", task.minRetryInterval);

            sb.append("successResponseCodes").append(keyTerminator);
            sb.append(task.successResponseCodes.length).append(':');
            for (int i = 0; i < task.successResponseCodes.length; i++)
                sb.append(task.successResponseCodes[i]).append(',');
            sb.append(valTerminator);

            appendValue(sb, "success", task.success);
            appendValue(sb, "sanitized", task.sanitized);

            sb.append("results=");
            for (HttpTaskResult result : task.results) {
                sb.append("status:").append(result.getStatus()).append('\r');
                appendHeaders(sb, result.getHeaders(), ':', '\r');
                sb.append("ts:").append(result.getTimestamp()).append('\r');

                if(null != result.getContent())
                    sb.append("content:").append(encoder.encode(result.getContent())).append('\r');
                sb.append("cause:").append(result.getErrorCause()).append('\r');
                sb.append('\f');
            }
            sb.append('\n');

            return sb.toString();
        }

        private static void appendValue(StringBuilder sb, String key, Object val) {
            sb.append(key).append(keyTerminator).append(val).append(valTerminator);
        }

        private static void appendHeaders(StringBuilder sb, HttpHeaders headers, char keyTerm, char valTerm) {
            sb.append("headers").append(keyTerm);
            for (String h : headers.keySet()) {
                for (String v : headers.get(h)) {
                    sb.append(h).append(":").append(v).append('\t');
                }
            }
            sb.append(valTerm);
        }

        @Override
        public HttpTask unmarshal(String json) throws Exception {

            HttpTask task = new HttpTask();
            ValueBuffer buffer = new ValueBuffer(json.length());

            boolean readingKey = true, readingValue = false;

            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);

                switch (c) {
                    case '\n':

                        setTaskField(task, buffer);
                        buffer.reset();

                        readingKey = true;
                        readingValue = false;
                        break;

                    case '=':
                        if (readingKey) {
                            readingValue = true;
                            readingKey = false;
                            break;
                        }
                        // fall through if this = is not key terminator
                    default:
                        if (readingKey) buffer.appendKey(c);
                        if (readingValue) buffer.appendValue(c);
                        break;
                }

            }

            return task;
        }


        private class ValueBuffer {
            char[] keyBuffer = new char[256];
            char[] valBuffer;
            int keyIndex = 0, valIndex = 0;

            public ValueBuffer(int size) {
                valBuffer = new char[size];
            }

            public int intValue() {
                return intValue(0, valIndex);
            }

            public int intValue(int offset, int len) {
                int value = 0;

                for (int i = offset; i < len; i++) {
                    char c = valBuffer[i];
                    if (c <= '9' && c >= '0')
                        value = (value * 10) + (c - '0');
                }

                // respect negative numbers
                if ('-' == valBuffer[0]) value *= -1;
                return value;
            }

            public long longValue() {
                return longValue(0, valIndex);
            }

            public long longValue(int offset, int len) {
                long value = 0L;

                for (int i = offset; i < len; i++) {
                    char c = valBuffer[i];
                    if (c <= '9' && c >= '0')
                        value = (value * 10) + (c - '0');
                }

                if ('-' == valBuffer[0]) value *= -1L;
                return value;
            }

            public boolean boolValue() {
                return boolValue(0);
            }

            public boolean boolValue(int offset) {
                return 't' == valBuffer[offset] || 'T' == valBuffer[offset];
            }

            public String stringValue() {
                return new String(valBuffer, 0, valIndex);
            }

            public String stringValue(int offset, int len) {
                return new String(valBuffer, offset, len);
            }

            public int[] intArrayValue() {
                return intArrayValue(0, valIndex);
            }

            public int[] intArrayValue(int offset, int len) {
                int[] value = null;

                int j = 0;
                int valStart = 0;
                for (int i = offset; i < len; i++) {
                    if (':' == valBuffer[i]) {
                        value = new int[intValue(offset, i)];
                        valStart = i;
                    }

                    if (',' == valBuffer[i]) {
                        value[j++] = intValue(valStart + 1, i);
                        valStart = i;
                    }
                }

                return value;
            }

            public HttpHeaders headersValue() {
                return headersValue(0, valIndex);
            }

            public HttpHeaders headersValue(int offset, int len) {
                HttpHeaders headers = new HttpHeaders();
                int nameEnd = offset - 1;
                int valueEnd = offset - 1;

                for (int i = offset; i < len; i++) {
                    if (':' == valBuffer[i]) nameEnd = i;
                    if ('\t' == valBuffer[i]) {
                        headers.addHeader(
                                String.valueOf(valBuffer, valueEnd + 1, nameEnd - valueEnd - 1),
                                String.valueOf(valBuffer, nameEnd + 1, i - nameEnd - 1));
                        valueEnd = i;
                    }
                }

                return headers;
            }

            public List<HttpTaskResult> resultListValue() throws Exception {

                List<HttpTaskResult> results = new ArrayList<HttpTaskResult>();
                HttpTaskResult result = new HttpTaskResult();
                String field = "";
                boolean readingKey = true, readingVal = false;
                int keyEnd = -1, valEnd = -1;

                for (int i = 0; i < valIndex; i++) {
                    char c = valBuffer[i];
                    switch (c) {
                        case '\r':
                            if (readingVal) {
                                if (field.equals("status")) {
                                    result.setStatus(intValue(keyEnd + 1, i));
                                } else if (field.equals("headers")) {
                                    result.setHeaders(headersValue(keyEnd + 1, i));
                                } else if (field.equals("ts")) {
                                    result.setTimestamp(longValue(keyEnd + 1, i));
                                } else if (field.equals("content")) {
                                    result.setContent(decoder.decodeBuffer(stringValue(keyEnd + 1, i - keyEnd - 1)));
                                } else if (field.equals("cause")) {
                                    result.setCauseString(stringValue(keyEnd + 1, i - keyEnd - 1));
                                }
                            }
                            valEnd = i;
                            readingVal = false; readingKey = true;
                            break;
                        case ':':
                            if (readingKey) {
                                readingKey = false;
                                readingVal = true;
                                field = stringValue(valEnd + 1, i - valEnd - 1);
                                keyEnd = i;
                            }
                            break;
                        case '\f':
                            results.add(result);
                            result = new HttpTaskResult();
                            valEnd++;
                            break;
                        default:
                            break;

                    }
                }

                return results;
            }

            public String key() {
                return String.valueOf(keyBuffer, 0, keyIndex);
            }

            public void appendKey(char c) {
                keyBuffer[keyIndex++] = c;
            }

            public void appendValue(char c) {
                valBuffer[valIndex++] = c;
            }

            public void reset() {
                keyIndex = 0;
                valIndex = 0;
            }
        }


        private void setTaskField(HttpTask task, ValueBuffer buffer) throws Exception {

            if (buffer.key().equals("taskID")) task.taskID = buffer.intValue();
            if (buffer.key().equals("uri")) task.uri = new URI(buffer.stringValue());
            if (buffer.key().equals("connectTimeoutMillis")) task.connectTimeoutMillis = buffer.intValue();
            if (buffer.key().equals("data")) task.data = decoder.decodeBuffer(buffer.stringValue());
            if (buffer.key().equals("identity")) task.identity = buffer.stringValue();
            if (buffer.key().equals("maxRetries")) task.maxRetries = buffer.intValue();
            if (buffer.key().equals("minRetryInterval")) task.minRetryInterval = buffer.longValue();
            if (buffer.key().equals("requestTimeoutMillis")) task.requestTimeoutMillis = buffer.longValue();
            if (buffer.key().equals("retryCount")) task.retryCount = buffer.intValue();
            if (buffer.key().equals("sanitized")) task.sanitized = buffer.boolValue();
            if (buffer.key().equals("success")) task.success = buffer.boolValue();
            if (buffer.key().equals("ttl")) task.ttl = buffer.longValue();
            if (buffer.key().equals("verb")) task.verb = buffer.stringValue();

            if (buffer.key().equals("successResponseCodes")) task.successResponseCodes = buffer.intArrayValue();
            if (buffer.key().equals("headers")) task.headers = buffer.headersValue();
            if (buffer.key().equals("results")) task.results = buffer.resultListValue();
        }
    }
}
