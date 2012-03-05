package com.wixpress.aqueduct.httpclient;

import com.wixpress.aqueduct.task.HttpTask;
import com.wixpress.aqueduct.task.HttpTaskResult;
import com.wixpress.aqueduct.taskqueue.HttpTaskResultListener;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static java.lang.String.format;

/**
 * @author evg
 */

public class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    private ClientBootstrap bootstrap;
    private HttpTaskResultListener taskCompletedListener;
    private ChannelGroup activeChannels = new DefaultChannelGroup();

    public HttpClient(HttpTaskResultListener taskCompletedListener) {

        this.taskCompletedListener = taskCompletedListener;

        // Configure the client.
        //TODO: Make Channels ThreadPool configurable
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(Executors.newFixedThreadPool(4), Executors.newFixedThreadPool(4))
        );

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpClientPipelineFactory(new ResponseFinalizer()));
        bootstrap.setOption("tcpNoDelay", true);
    }

    public void shutdown() {

        activeChannels.close();
        bootstrap.releaseExternalResources();
    }


    public void send(HttpTask task) throws Exception {

        String host = task.getUri().getHost();
        int port = task.getUri().getPort();

        if (-1 == port) port = 80;

        LOGGER.debug(format("Start performing request to %s", task.getUri().toASCIIString()));


        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        Channel channel = future.getChannel();
        channel.getConfig().setConnectTimeoutMillis(task.getConnectTimeoutMillis());
        channel.getPipeline().getContext("handler").setAttachment(task);

        activeChannels.add(channel);
        future.addListener(new HttpTaskExecutor(task));
    }

    private class HttpTaskExecutor implements ChannelFutureListener {

        HttpTask task;

        public HttpTaskExecutor(HttpTask task) {
            this.task = task;
        }

        public void operationComplete(ChannelFuture future) throws Exception {

            // Wait until the connection attempt succeeds or fails.
            Channel channel = future.getChannel();

            try {
                String host = task.getUri().getHost();

                if (!future.isSuccess()) {
                    LOGGER.error(format("Failed to connect to %s", host));
                    setFailure(channel, future.getCause());
                    return;
                }

                LOGGER.debug(format("Connected to %s", host));

                // Prepare the HTTP request.
                HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.valueOf(task.getVerb()), task.getUri().toASCIIString());
                request.setHeader(HttpHeaders.Names.HOST, host);
                request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

                if (task.getHeaders() != null && (!task.getHeaders().isEmpty())) {
                    for (Map.Entry<String, List<String>> entry : task.getHeaders().entrySet()) {
                        for (String headerValue : entry.getValue()) {
                            request.addHeader(entry.getKey(), headerValue);
                        }
                    }
                }

                if (null != task.getData()) {
                    request.setContent(ChannelBuffers.wrappedBuffer(task.getData()));
                }

                // Send the HTTP request.
                channel.write(request);

                LOGGER.debug(format("HTTP Request sent, waiting for response from %s", host));
            } catch (Exception e) {
                LOGGER.error(format("Failed to complete task for %s", task.getUri().toASCIIString()), e);
                setFailure(channel, e);
            }
        }

        public void setFailure(Channel channel, Throwable e) throws Exception {

            if (null != task) {
                task.setSuccess(false);

                HttpTaskResult result = new HttpTaskResult();
                result.setCause(e);
                task.addResult(result);
            }

            taskFinished(task, channel);
        }
    }

    private void taskFinished(HttpTask task, Channel channel) {

        try {
            channel.close();
        } finally {
            if (null != taskCompletedListener) {
                taskCompletedListener.taskComplete(task);
            }
        }
    }

    private class ResponseFinalizer implements HttpResponseCompletedListener {

        public void responseCompleted(HttpTask task, Channel channel) {
            taskFinished(task, channel);
        }
    }
}