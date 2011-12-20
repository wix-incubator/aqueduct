package httpclient;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import taskqueue.HttpTask;
import taskqueue.HttpTaskQueue;
import taskqueue.HttpTaskResult;

/**
 * @author evg
 */

public class HttpClient {

    private Logger logger = Logger.getLogger("root");
    private ClientBootstrap bootstrap;
    private HttpTaskCompletedListener taskCompletedListener;
    private ConcurrentHashMap<Integer, Channel> activeChannels = new ConcurrentHashMap<Integer, Channel>();

    public HttpClient(HttpTaskCompletedListener taskCompletedListener) {

        this.taskCompletedListener = taskCompletedListener;

        // Configure the client.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(Executors.newFixedThreadPool(4), Executors.newFixedThreadPool(4))
        );

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpClientPipelineFactory(new ResponseFinalizer()));
    }

    public void shutdown() {

        for (Channel channel : activeChannels.values()) {
            channel.close();
        }
        activeChannels.clear();

        bootstrap.releaseExternalResources();
    }


    public void send(HttpTask task) throws Exception {

        String host = task.getUri().getHost();
        int port = task.getUri().getPort();

        if (-1 == port) port = 80;

        logger.info("Start performing request to " + task.getUri().toASCIIString());
        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        Channel channel = future.getChannel();
        channel.getPipeline().getContext("handler").setAttachment(task);

        activeChannels.put(channel.getId(), channel);
        future.addListener(new HttpTaskExecutor(task));
    }

    private class HttpTaskExecutor implements ChannelFutureListener {
        HttpTask task;

        public HttpTaskExecutor(HttpTask task) {
            this.task = task;
        }

        public void operationComplete(ChannelFuture future) throws Exception {

            String host = task.getUri().getHost();

            // Wait until the connection attempt succeeds or fails.
            Channel channel = future.getChannel();
            if (!future.isSuccess()) {

                logger.severe("Failed to connect to " + host);
                task.setSuccess(false);
                task.setLastError(future.getCause());

                HttpTaskResult result = new HttpTaskResult();
                result.setErrorCause(future.getCause());
                task.addResult(result);

                taskFinished(task, channel);
                return;
            }

            logger.info("Connected to " + host);

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_0, task.getMethod(), task.getUri().toASCIIString());
            request.setHeader(HttpHeaders.Names.HOST, host);
            request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

            if (task.getHeaders() != null && (!task.getHeaders().isEmpty())) {
                for (Map.Entry<String, List<String>> entry : task.getHeaders().entrySet()) {
                    for (String headerValue : entry.getValue()) {
                        request.setHeader(entry.getKey(), headerValue);
                    }
                }
            }

            if (null != task.getData()) {
                request.setContent(ChannelBuffers.wrappedBuffer(task.getData()));
            }

            // Send the HTTP request.
            channel.write(request);

            logger.info("HTTP Request sent, waiting for response from " + host);
        }
    }

    private void taskFinished(HttpTask task, Channel channel) {

        activeChannels.remove(channel.getId());
        channel.close();
        taskCompletedListener.taskCompleted(task);
    }

    private class ResponseFinalizer implements HttpResponseCompletedListener {

        public void responseCompleted(HttpTask task, Channel channel) {
            taskFinished(task, channel);
        }
    }
}