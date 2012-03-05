package com.wixpress.aqueduct.httptestserver;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import static org.jboss.netty.channel.Channels.*;

/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 12:11
 */
public class HttpTestServerPipelineFactory implements ChannelPipelineFactory {

    private HttpRequestArrivedListener requestArrivedListener;

    public HttpTestServerPipelineFactory(HttpRequestArrivedListener requestArrivedListener){
        this.requestArrivedListener = requestArrivedListener;
    }

    public ChannelPipeline getPipeline() throws Exception {

        // Create a default pipeline implementation.
         ChannelPipeline pipeline = pipeline();

         // Uncomment the following line if you want HTTPS
         //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
         //engine.setUseClientMode(false);
         //pipeline.addLast("ssl", new SslHandler(engine));

         pipeline.addLast("decoder", new HttpRequestDecoder());
         pipeline.addLast("encoder", new HttpResponseEncoder());

         pipeline.addLast("handler", new HttpTestServerResponseHandler(requestArrivedListener));
         return pipeline;
    }
}
