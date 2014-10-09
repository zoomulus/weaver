package com.zoomulus.weaver.rest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;

import com.google.common.collect.Lists;
import com.zoomulus.weaver.core.connector.ServerConnector;

public class RestServer
{
    private final List<ServerConnector> connectors;
    private List<ChannelFuture> channels = Lists.newArrayList();
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    
    public RestServer(final ServerConnector connector)
    {
        this(Lists.newArrayList(connector));
    }
    
    public RestServer(final List<ServerConnector> connectors)
    {
        this.connectors = connectors;
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }
    
    public void start()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() { shutdown(); }
        });
        
        try
        {
            // for each connector, build a bootstrap, start and save the ChannelFuture
            for (final ServerConnector connector : connectors)
            {
                final ServerBootstrap bootstrap =
                        new ServerBootstrap()
                            .group(masterGroup, slaveGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(connector.getChannelInitializer())
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
                channels.add(bootstrap.bind(connector.getPort()).sync());
            }
        }
        catch (final InterruptedException e) { }
    }
    
    public void shutdown()
    {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();
        
        // Perform the magic foo
        for (final ChannelFuture channel : channels)
        {
            try
            {
                channel.channel().closeFuture().sync();
            }
            catch (InterruptedException e) { }
        }
    }
}
