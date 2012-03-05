package com.wixpress.aqueduct.httpclient;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
  import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
  import org.jboss.netty.handler.codec.http.HttpContentDecompressor;


/**
   * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
   * @author Andy Taylor (andy.taylor@jboss.org)
   * @author <a href="http://gleamynode.net/">Trustin Lee</a>
   *
   * @version $Rev: 2226 $, $Date: 2010-03-31 11:26:51 +0900 (Wed, 31 Mar 2010) $
   */

  class HttpClientPipelineFactory implements ChannelPipelineFactory {
      private HttpResponseCompletedListener responseCompletedListener;

      public HttpClientPipelineFactory(HttpResponseCompletedListener responseCompletedListener) {
          this.responseCompletedListener = responseCompletedListener;
      }

     public ChannelPipeline getPipeline() throws Exception {
         // Create a default pipeline implementation.
         ChannelPipeline pipeline = pipeline();

         pipeline.addLast("codec", new HttpClientCodec());

         // Remove the following line if you don't want automatic content decompression.
         pipeline.addLast("inflater", new HttpContentDecompressor());

         // Uncomment the following line if you don't want to handle HttpChunks.
         pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));

         pipeline.addLast("handler", new HttpResponseHandler(responseCompletedListener));
         return pipeline;
     }
 }