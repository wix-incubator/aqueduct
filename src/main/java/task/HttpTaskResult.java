package task;

import java.util.*;

/**
 * Created by evg.
 * Date: 10/12/11
 * Time: 22:58
 */
public class HttpTaskResult {
    private int status = 0;
    private HttpHeaders headers;
    private byte [] content;
    private Date date;

    private String errorCause;

    public HttpTaskResult(){
        date = new Date();
        headers = new HttpHeaders();
    }

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
        return errorCause;
    }

    public void setErrorCause(Throwable errorCause) {
        if(null != errorCause)
            this.errorCause = errorCause.toString();
    }

    public Date getDate() {
        return date;
    }
}
