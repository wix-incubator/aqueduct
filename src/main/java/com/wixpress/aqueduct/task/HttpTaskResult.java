package com.wixpress.aqueduct.task;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

/**
 * Created by evg.
 * Date: 10/12/11
 * Time: 22:58
 */
public class HttpTaskResult {
    private int status = 0;
    private HttpHeaders headers = new HttpHeaders();
    private byte [] content;
    private long timestamp = System.currentTimeMillis();
    private String errorCauseString = "";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getErrorCause() {
        return errorCauseString;
    }

    @JsonProperty("errorCause")
    public void setCauseString(String errorCause) {
        if(null != errorCause)
            this.errorCauseString = errorCause;
    }

    public void setCause(Throwable errorCause) {
        if(null != errorCause)
            this.errorCauseString = errorCause.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpTaskResult)) return false;

        HttpTaskResult result = (HttpTaskResult) o;

        if (status != result.status) return false;
        if (timestamp != result.timestamp) return false;
        if (!Arrays.equals(content, result.content)) return false;
        if (errorCauseString != null ? !errorCauseString.equals(result.errorCauseString) : result.errorCauseString != null) return false;
        if (headers != null ? !headers.equals(result.headers) : result.headers != null) return false;

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("HttpTaskResult");
        sb.append("{status=").append(status);
        sb.append(", headers=").append(headers);
        sb.append(", content=size:").append(content == null ? "null" : content.length);
        sb.append(", ts=").append(timestamp);
        sb.append(", errorCauseString='").append(errorCauseString).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
