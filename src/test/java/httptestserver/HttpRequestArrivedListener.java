package httptestserver;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 15:00
 */
public interface HttpRequestArrivedListener {

    public void requestArrived(HttpRequest request);
}
