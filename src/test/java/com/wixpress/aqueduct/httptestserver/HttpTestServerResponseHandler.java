package com.wixpress.aqueduct.httptestserver;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 12:14
 */
public class HttpTestServerResponseHandler extends SimpleChannelUpstreamHandler {

    private HttpRequestArrivedListener requestArrivedListener;

    public HttpTestServerResponseHandler(HttpRequestArrivedListener requestArrivedListener){
        this.requestArrivedListener = requestArrivedListener;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpRequest request = (HttpRequest) e.getMessage();

        // Call testing callback
        if(requestArrivedListener != null) requestArrivedListener.requestArrived(request);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_0, getTestStatus(request));

        response.setContent(ChannelBuffers.copiedBuffer("ok".getBytes()));
        setContentLength(response, 2);

        Channel ch = e.getChannel();

        // Write the content.
        ChannelFuture writeFuture = ch.write(response);

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }


    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponseStatus getTestStatus(HttpRequest request){
        if(request.containsHeader("Test-Status")){
            return HttpResponseStatus.valueOf(Integer.parseInt(request.getHeader("Test-Status")));
        }

        return OK;
    }

}
