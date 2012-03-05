package com.wixpress.aqueduct.httptestserver;

import org.jboss.netty.bootstrap.ServerBootstrap;
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

    ServerBootstrap bootstrap;
    private int localPort = 0;
    private TestRequestListener requestArrivedListener = new TestRequestListener();
    private CountDownLatch requestArrivedSignal;
    private String localUrl;


    public void start() {

        try {
            ServerSocket socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            socket.close();

            bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newSingleThreadExecutor(),
                            Executors.newSingleThreadExecutor()));

            bootstrap.setPipelineFactory(new HttpTestServerPipelineFactory(requestArrivedListener));

            bootstrap.bind(socket.getLocalSocketAddress());

            localPort = socket.getLocalPort();
            localUrl = String.format("http://localhost:%d/", localPort);

            System.out.printf("Test server started on port %d\n", localPort);
        } catch (IOException e) {
            System.out.printf("IO Error: %s", e.toString());
        }
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getLocalUrl(){
        return localUrl;
    }

    public void stop(){
        bootstrap.releaseExternalResources();
    }

    public void registerForRequestInterception() {
        registerForRequestInterception(1);
    }

    public void registerForRequestInterception(int nRequests) {
        requestArrivedSignal = new CountDownLatch(nRequests);
    }

    public HttpRequest waitUntilRequestArrives() {
        return waitUntilRequestArrives(1, TimeUnit.SECONDS);
    }

    public HttpRequest waitUntilRequestArrives(long timeout, TimeUnit timeUnit) {

        if (requestArrivedSignal == null) {
            throw new IllegalStateException("Register for request interception first\n");
        }

        try {
            requestArrivedSignal.await(timeout, timeUnit);
            requestArrivedSignal = null;
            return requestArrivedListener.getLastHttpRequest();

        } catch (InterruptedException e) {
            System.out.print("Interrupted signal\n");
        }

        return null;
    }

    private class TestRequestListener implements HttpRequestArrivedListener {
        HttpRequest httpRequest;

        public void requestArrived(HttpRequest request) {
            this.httpRequest = request;

            System.out.printf("%s Request arrived for %s", request.getMethod().getName(), request.getUri());
            if (requestArrivedSignal != null) {
                requestArrivedSignal.countDown();
            }
        }

        public HttpRequest getLastHttpRequest() {
            return httpRequest;
        }
    }
}
