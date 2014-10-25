package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.util.PathJoiner;

public class DefaultResourceScannerStrategy implements ResourceScannerStrategy
{
    @Override
    public Map<ResourceIdentifier, Resource> scan(Set<Class<?>> resourceClasses)
    {
        final Map<ResourceIdentifier, Resource> results = Maps.newHashMap();
        
        Path classPath = null;
        for (final Class<?> resourceClass : resourceClasses)
        {
            classPath = resourceClass.getAnnotation(Path.class);
            if (null == classPath) continue;

            for (final Method method : resourceClass.getMethods())
            {
                final Optional<HttpMethod> httpMethod = getHttpMethodForMethod(method);
                
                if (httpMethod.isPresent())
                {                
                    Path methodPath = method.getAnnotation(Path.class);
                    String absPath = null == methodPath ?
                            new PathJoiner().with(classPath.value()).join() :
                            new PathJoiner().with(classPath.value()).with(methodPath.value()).join();
                            
                    final Resource rsrc = Resource.builder()
                            .referencedClass(resourceClass)
                            .referencedMethod(method)
                            .path(absPath)
                            .httpMethod(httpMethod.get())
                            .build();
                    
                    results.put(new ResourceIdentifier(absPath, httpMethod.get()), rsrc);
                }
            }
        }
        
        return results;
    }
    
    private Optional<HttpMethod> getHttpMethodForMethod(final Method method)
    {
        return Optional.ofNullable(
                null != method.getAnnotation(GET.class) ? HttpMethod.GET :
                    (null != method.getAnnotation(POST.class) ? HttpMethod.POST :
                        (null != method.getAnnotation(PUT.class) ? HttpMethod.PUT :
                            (null != method.getAnnotation(DELETE.class) ? HttpMethod.DELETE :
                                (null != method.getAnnotation(HEAD.class) ? HttpMethod.HEAD :
                                    (null != method.getAnnotation(OPTIONS.class) ? HttpMethod.OPTIONS :
                                        null))))));
    }
}
