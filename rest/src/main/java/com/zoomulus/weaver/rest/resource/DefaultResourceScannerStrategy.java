package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Lists;
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
                            .consumesContentTypes(getConsumesContentTypes(httpMethod.get(), method, resourceClass))
                            .producesContentTypes(getProducesContentTypes(httpMethod.get(), method, resourceClass))
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
    
    private List<String> getConsumesContentTypes(final HttpMethod httpMethod, final Method method, final Class<?> resourceClass)
    {
        final List<String> contentTypes = Lists.newArrayList();
        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.DELETE)
        {
            Annotation consumesAnnotation = method.getAnnotation(Consumes.class);
            if (null != consumesAnnotation)
            {
                contentTypes.addAll(Lists.newArrayList(((Consumes)consumesAnnotation).value()));
            }
            consumesAnnotation = resourceClass.getAnnotation(Consumes.class);
            if (null != consumesAnnotation)
            {
                contentTypes.addAll(Lists.newArrayList(((Consumes)consumesAnnotation).value()));
            }
            
            if (contentTypes.isEmpty())
                return Lists.newArrayList(MediaType.TEXT_PLAIN);
        }
        return contentTypes;
    }
    
    private List<String> getProducesContentTypes(final HttpMethod httpMethod, final Method method, final Class<?> resourceClass)
    {
        final List<String> contentTypes = Lists.newArrayList();
        Annotation producesAnnotation = method.getAnnotation(Produces.class);
        if (null != producesAnnotation)
        {
            contentTypes.addAll(Lists.newArrayList(((Produces)producesAnnotation).value()));
        }
        producesAnnotation = resourceClass.getAnnotation(Produces.class);
        if (null != producesAnnotation)
        {
            contentTypes.addAll(Lists.newArrayList(((Produces)producesAnnotation).value()));
        }
        return contentTypes.isEmpty() ? Lists.newArrayList(MediaType.TEXT_PLAIN) : contentTypes;
    }    
}
