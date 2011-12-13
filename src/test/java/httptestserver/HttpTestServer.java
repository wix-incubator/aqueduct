package httptestserver;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 12:05
 */
public class HttpTestServer {

    private int localPort = 0;
    private TestRequestListener requestArrivedListener = new TestRequestListener();
    private CountDownLatch requestArrivedSignal;


    public void start() {

        try {
            ServerSocket socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            socket.close();

            ServerBootstrap bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newSingleThreadExecutor(),
                            Executors.newSingleThreadExecutor()));

            bootstrap.setPipelineFactory(new HttpTestServerPipelineFactory(requestArrivedListener));

            bootstrap.bind(socket.getLocalSocketAddress());

            localPort = socket.getLocalPort();

            System.out.printf("Test server started on port %d", localPort);
        } catch (IOException e) {

        }
    }

    public int getLocalPort() {
        return localPort;
    }

    public void registerForRequestInterception() {
        requestArrivedSignal = new CountDownLatch(1);
    }

    public HttpRequest waitUntilRequestArrives() {

        if (requestArrivedSignal == null) {
            throw new IllegalStateException("Register for request interception first");
        }

        try {
            requestArrivedSignal.await(56, TimeUnit.SECONDS);
            requestArrivedSignal = null;
            return requestArrivedListener.getLastHttpRequest();

        } catch (InterruptedException e) {
            System.out.print("Interrupted signal");
        }

        return null;
    }

    private class TestRequestListener implements HttpRequestArrivedListener {
        HttpRequest httpRequest;

        public void requestArrived(HttpRequest request) {
            this.httpRequest = request;

            if (requestArrivedSignal != null) {
                requestArrivedSignal.countDown();
            }
        }

        public HttpRequest getLastHttpRequest() {
            return httpRequest;
        }
    }
}
