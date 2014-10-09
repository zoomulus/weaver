package com.zoomulus.weaver.core.connector;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.Path;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;

@Slf4j
public class ServerConnector
{
    @Getter
    final int port;
    
    final ChannelHandler connectionHandler;
    
    private ServerConnector(int port, final ChannelHandler connectionHandler)
    {
        this.port = port;
        this.connectionHandler = connectionHandler;
    }
    
    public ChannelInitializer<?> getChannelInitializer()
    {
        return new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception
            {
                ch.pipeline().addLast(connectionHandler);
            }            
        };
    }

    public static ServerConnectorBuilder builder()
    {
        return new ServerConnectorBuilder();
    }
    
    public static class ServerConnectorBuilder
    {
        private int port = 8080;
        private Optional<ChannelHandler> connectionHandler = Optional.empty();
        private Set<Class<?>> resources = Sets.newConcurrentHashSet();
        
        public ServerConnectorBuilder withPort(int port)
        {
            this.port = port;
            return this;
        }
        
        public ServerConnectorBuilder withConnectionHandler(final ChannelHandler connectionHandler)
        {
            this.connectionHandler = Optional.of(connectionHandler);
            return this;
        }
        
        public ServerConnectorBuilder withResource(final Class<?> resourceClass)
        {
            addResourceIfJaxRS(resourceClass);
            return this;
        }
        
        public ServerConnectorBuilder withResource(final String resourceClassName)
        {
            try
            {
                log.debug("Searching for resource class {} by name", resourceClassName);
                Class<?> resourceClass = Class.forName(resourceClassName);
                return withResource(resourceClass);
            }
            catch (ClassNotFoundException e)
            {
                log.warn("Could not locate resource class " + resourceClassName, e);
            }
            return this;
        }
        
        public ServerConnectorBuilder withResources(final String packageName) 
        {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try
            {
                log.debug("Scanning package {} for JAX-RS resources", packageName);
                for (final ClassPath.ClassInfo info : ClassPath.from(classLoader).getTopLevelClasses(packageName))
                {
                    addResourceIfJaxRS(info.load());
                }
            }
            catch (IOException e)
            {
                log.warn("Couldn't load classes for package name " + packageName, e);
            }
            return this;
        }
        
        public ServerConnector build()
        {
            if (connectionHandler.isPresent())
            {
                return new ServerConnector(port, connectionHandler.get());
            }
            else if (resources.isEmpty())
            {
                throw new RuntimeException("Could not find any valid resource classes");
            }
            return new ServerConnector(port, new RestHandler(resources));
        }
        
        private void addResourceIfJaxRS(final Class<?> resourceClass)
        {
            log.debug("Checking whether {} is a JAX-RS resource", resourceClass.getName());
            if (null != resourceClass.getAnnotation(Path.class))
            {
                log.debug("Found JAX-RS resource {}", resourceClass.getName());
                resources.add(resourceClass);
            }
        }
    }
}
