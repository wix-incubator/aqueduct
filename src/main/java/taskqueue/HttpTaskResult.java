package taskqueue;

import java.util.List;
import java.util.Map;

/**
 * Created by evg.
 * Date: 10/12/11
 * Time: 22:58
 */
public class HttpTaskResult {
    private int status = 0;
    private List<Map.Entry<String, String>> headers;
    private byte [] content;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Map.Entry<String, String>> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Map.Entry<String, String>> headers) {
        this.headers = headers;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
