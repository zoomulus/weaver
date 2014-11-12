package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.Value;
import lombok.experimental.Builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Value
@Builder
public class Resource
{
    Class<?> referencedClass;
    Method referencedMethod;
    //SegmentedString path;
    String path;
    HttpMethod httpMethod;
    
    Map<String, String> pathParams = Maps.newConcurrentMap();
    
    // @Consumes / @Produces
    // Single unnamed parameter - body (input stream, byte array, or String - depending on content type?)
    // @PathParam w/ @Path e.g. @Path("{id"}), @PathParam("id") int id
    //  regex matching also, e.g. @Path("{id : \\d+}")
    //  Also handle matrix params, e.g. @Path("{/e55/{year}") with uri path /e55;color=black/2006
    //  would match year as 2006
    // Handle javax.ws.rs.WebApplicationException (chap 7)
    // Return type - javax.ws.rs.core.StreamingOutput
    //  Support other types (OutputStream, byte array, String, or JSON-serializable object) automatically
    //  sent to StreamingOutput
    // Support providing annotations on an interface, not implementation
    // Should work on subclasses also, but subclass must still have @Path annotation
    // Support all injected parameter types:
    //  - PathParam
    //  - MatrixParam
    //  - QueryParam
    //  - FormParam
    //  - HeaderParam
    //  - CookieParam
    //  - BeanParam
    //  - Context
    //  - DefaultValue
    //  - Encoded
    // Support string conversion to native types, objects with single String constructor param,
    //  objects with valueOf(String), containers of objects matching the above, or Optional?
    // Support ParamConverter<T>
    
    private Object[] populateArgs(final String messageBody, final ResourcePath resourcePath)
    {
        final List<Object> args = Lists.newArrayList();
        for (final Parameter param : referencedMethod.getParameters())
        {
            if (param.getAnnotations().length < 0)
            {
                Annotation pathParam = param.getAnnotation(PathParam.class);
                if (null != pathParam)
                {
                    args.add(resourcePath.get(pathParam.annotationType().getName()));
                }
            }
            else
            {
                // This is the body
                args.add(messageBody);
            }
        }
        return args.toArray();
    }
    
    public Response invoke(final String messageBody, final ResourcePath resourcePath)
    {
        Object response = null;
        try
        {
            Object resourceObj = referencedClass.newInstance();
            response = referencedMethod.invoke(resourceObj, populateArgs(messageBody, resourcePath));
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (null == response) 
        {
            return Response.status(Status.NO_CONTENT).build();
        }
        if (response instanceof Response)
        {
            return (Response) response;
        }
        else if (response instanceof String)
        {
            return Response.status(Status.OK).entity((String) response).build();
        }
        else
        {
            // Try to convert object to JSON
            // Return string of JSON if successful
        }
        return Response.status(Status.OK).entity("from Resource class").build();
    }
    
    public Optional<String> getPathParam(final String name)
    {
        return Optional.ofNullable(pathParams.get(name));
    }
}
