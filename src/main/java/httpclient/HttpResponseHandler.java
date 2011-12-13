package httpclient;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import taskqueue.HttpTask;
import taskqueue.HttpTaskResult;

import java.util.*;

/**
 * @author evg
 */

public class HttpResponseHandler extends SimpleChannelUpstreamHandler {

    private HttpResponseCompletedListener responseCompletedListener;


    public HttpResponseHandler(HttpResponseCompletedListener responseCompletedListener) {
        this.responseCompletedListener = responseCompletedListener;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpResponse response = (HttpResponse) e.getMessage();
        HttpTask task = (HttpTask) ctx.getAttachment();

        HttpTaskResult result = new HttpTaskResult();

        int responseStatus = response.getStatus().getCode();
        result.setStatus(responseStatus);

        for (int successCode : task.getSuccessResponseCodes()){
            if(successCode == responseStatus) task.setSuccess(true);
        }

        List<Map.Entry<String, String>> headersDeepCopy = new ArrayList<Map.Entry<String, String>>(response.getHeaders());
        Collections.copy(headersDeepCopy, response.getHeaders());
        result.setHeaders(headersDeepCopy);

        ChannelBuffer content = response.getContent();
        if (content.readable()) {
            byte [] resultContent = new byte[content.capacity()];
            content.getBytes(0, resultContent);
            result.setContent(resultContent);
        }

        task.addResult(result);
        responseCompletedListener.responseCompleted(task, ctx.getChannel());
    }
}

