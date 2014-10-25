package com.zoomulus.weaver.rest.connector;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.Path;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.zoomulus.weaver.core.connector.ServerConnector;
import com.zoomulus.weaver.rest.RestHandler;

@Slf4j
public class RestServerConnector implements ServerConnector
{
    @Getter
    final int port;
    final Set<Class<?>> resources;
    
    private RestServerConnector(final Optional<Integer> port, final Set<Class<?>> resources)
    {
        this.port = port.isPresent() ? port.get() : getDefaultPort();
        this.resources = resources;
    }
    
    @Override
    public int getDefaultPort()
    {
        return 8080;
    }
    
    @Override
    public ChannelInitializer<?> getChannelInitializer()
    {
        return new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception
            {
                ch.pipeline().addLast(new HttpRequestDecoder());
                ch.pipeline().addLast(new HttpResponseEncoder());
                ch.pipeline().addLast(new RestHandler(resources));
            }            
        };
    }
    
    public static RestServerConnectorBuilder builder()
    {
        return new RestServerConnectorBuilder();
    }

    public static class RestServerConnectorBuilder
    {
        private Optional<Integer> port = Optional.empty();
        private Set<Class<?>> resources = Sets.newConcurrentHashSet();
        
        public RestServerConnectorBuilder withPort(int port)
        {
            this.port = Optional.of(port);
            return this;
        }
        
        public RestServerConnectorBuilder withResource(final Class<?> resourceClass)
        {
            addResourceIfJaxRS(resourceClass);
            return this;
        }
        
        public RestServerConnectorBuilder withResource(final String resourceClassName)
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
        
        public RestServerConnectorBuilder withResources(final String packageName) 
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
        
        public RestServerConnector build()
        {
            if (resources.isEmpty())
            {
                throw new RuntimeException("Could not find any valid resource classes");
            }
            return new RestServerConnector(port, resources);
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
