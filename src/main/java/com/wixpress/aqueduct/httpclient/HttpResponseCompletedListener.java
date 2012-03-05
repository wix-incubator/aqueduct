package com.wixpress.aqueduct.httpclient;

import org.jboss.netty.channel.Channel;
import com.wixpress.aqueduct.task.HttpTask;

/**
 * Created by evg.
 * Date: 12/12/11
 * Time: 01:29
 */
public interface HttpResponseCompletedListener {

    public void responseCompleted(HttpTask task, Channel channel);
}
