package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Value;

import com.zoomulus.weaver.core.util.PathJoiner;

@Value
public class ResourceIdentifier
{
    final String path;
    final HttpMethod method;
    
    public ResourceIdentifier(final String path, final HttpMethod method)
    {
        this.path = new PathJoiner().with(path).join();
        this.method = method;
    }
    
    public ResourceIdentifier(final String basePath, final String resourcePath, final HttpMethod method)
    {
        this(new PathJoiner().with(basePath).with(resourcePath).join(), method);
    }
}
