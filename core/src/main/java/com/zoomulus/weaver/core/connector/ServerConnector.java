package com.zoomulus.weaver.core.connector;

import io.netty.channel.ChannelInitializer;

public interface ServerConnector
{
    int getPort();
    int getDefaultPort();
    ChannelInitializer<?> getChannelInitializer();
}
