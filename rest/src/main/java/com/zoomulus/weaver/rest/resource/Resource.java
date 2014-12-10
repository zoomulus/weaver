package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.Value;
import lombok.experimental.Builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Value
@Builder
public class Resource
{
    Class<?> referencedClass;
    Method referencedMethod;
    String path;
    HttpMethod httpMethod;
    
    Map<String, String> pathParams = Maps.newHashMap();
    
    // TODO:
    // @Consumes / @Produces
    // Single unnamed parameter - body (input stream, byte array, or String - depending on content type?)
    // Support List<PathSegment> (maybe)
    // Handle javax.ws.rs.WebApplicationException (chap 7)
    // Support providing annotations on an interface, not implementation
    // Should work on subclasses also, but subclass must still have @Path annotation
    // Support all injected parameter types:
    //  - QueryParam
    //  - FormParam
    //  - HeaderParam
    //  - CookieParam
    //  - BeanParam
    //  - Context
    //  - DefaultValue
    //  - Encoded
    // Support ParamConverter<T>
    // Ensure most optimal match works
    
    private Object getParameterOfMatchingType(final Class<?> parameterType, final String s_arg)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object arg = null;
        if (parameterType.isPrimitive())
        {
            if (parameterType == boolean.class) { arg = Boolean.valueOf(s_arg); }
            else if (parameterType == byte.class) { arg = Byte.valueOf(s_arg); }
            else if (parameterType == short.class) { arg = Short.valueOf(s_arg); }
            else if (parameterType == int.class) { arg = Integer.valueOf(s_arg); }
            else if (parameterType == long.class) { arg = Long.valueOf(s_arg); }
            else if (parameterType == float.class) { arg = Float.valueOf(s_arg); }
            else if (parameterType == double.class) { arg = Double.valueOf(s_arg); }
        }
        else
        {
            Optional<Constructor<?>> stringConstructor = getStringConstructor(parameterType);
            if (stringConstructor.isPresent())
            {
                arg = stringConstructor.get().newInstance(s_arg);
            }
            else
            {
                Optional<Method> valueOfStringMethod = getValueOfStringMethod(parameterType);
                if (valueOfStringMethod.isPresent())
                {
                    arg = valueOfStringMethod.get().invoke(null, s_arg);
                }
                else
                {
                    arg = s_arg;
                }
            }
        }
        return arg;
    }
    
    private Object[] populateArgs(final String messageBody,
            final ResourcePath resourcePath,
            final Map<String, List<String>> queryParams,
            final Map<String, List<String>> formParams)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        final List<Object> args = Lists.newArrayList();
        
        Class<?>[] parameterTypes = referencedMethod.getParameterTypes();
        
        int idx = 0;
        for (final Annotation[] paramAnnotations : referencedMethod.getParameterAnnotations())
        {
            if (0 == paramAnnotations.length)
            {
                args.add(messageBody);
            }
            else
            {
                for (final Annotation annotation : paramAnnotations)
                {
                    Class<?> parameterType = parameterTypes[idx];
                    String s_arg = null;
                    if (annotation instanceof PathParam)
                    {
                        final String paramValue = ((PathParam) annotation).value();
                        
                        if (PathSegment.class.isAssignableFrom(parameterType))
                        {
                            Optional<PathSegment> ps = resourcePath.getPathSegment(paramValue);
                            if (ps.isPresent()) args.add(ps.get());
                        }
                        else
                        {
                            s_arg = resourcePath.get(paramValue);
                        }
                    }
                    else if (annotation instanceof MatrixParam)
                    {
                        s_arg = resourcePath.matrixParamGet((((MatrixParam) annotation).value()));                        
                    }
                    else if (annotation instanceof QueryParam || annotation instanceof FormParam)
                    {
                        final List<String> params = (annotation instanceof QueryParam) ?
                                queryParams.get(((QueryParam) annotation).value()) :
                                formParams.get(((FormParam) annotation).value());
                        if (List.class.isAssignableFrom(parameterType))
                        {
                            args.add(params);
                        }
                        else if (null != params && ! params.isEmpty())
                        {
                            s_arg = params.get(0);
                        }
                    }
                    
                    if (null != s_arg)
                    {
                        Object arg = getParameterOfMatchingType(parameterType, s_arg);
                        if (null != arg)
                        {
                            args.add(arg);
                        }
                    }
                }
            }
            ++idx;
        }
        
        return args.toArray();
    }
    
    public Response invoke(final String messageBody, final ResourcePath resourcePath, final Map<String, List<String>> queryParams)
    {
        Object response = null;
        try
        {
            String decodedBody = null;
            try
            {
                decodedBody = URLDecoder.decode(messageBody, CharsetUtil.UTF_8.name());
            }
            catch (UnsupportedEncodingException e)
            {
                decodedBody = messageBody;
            }
            
            Map<String, List<String>> formParams = decodedBody != null ? parseFormData(decodedBody) : Maps.newHashMap();
            
            Object[] args = populateArgs(messageBody, resourcePath, queryParams, formParams);
            if (referencedMethod.getParameters().length != args.length)
            {
                return Response.status(Status.NOT_FOUND).build();
            }
            Object resourceObj = referencedClass.getConstructor((Class<?>[])null).newInstance((Object[])null);
            response = referencedMethod.invoke(resourceObj, args);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
        {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
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
        
        // If this class has a toString delcared directly on it, prefer that method
        else if (hasDeclaredToString(response.getClass()))
        {
            return Response.status(Status.OK).entity(response.toString()).build();
        }
        // Otherwise do a JSON conversion if possible
        else
        {
            try
            {
                return Response.status(Status.OK).entity(new ObjectMapper().writeValueAsString(response)).build();
            }
            catch (JsonProcessingException e) { }
        }
        
        // As a last resort use whatever toString gives us
        return Response.status(Status.OK).entity(response.toString()).build();
    }
    
    public Optional<String> getPathParam(final String name)
    {
        return Optional.ofNullable(pathParams.get(name));
    }
    
    
    private Optional<Constructor<?>> getStringConstructor(final Class<?> klass)
    {
        for (final Constructor<?> ctor : klass.getConstructors())
        {
            final Class<?>[] params = ctor.getParameterTypes();
            if (params.length != 1)
            {
                continue;
            }
            if (params[0] == String.class)
            {
                return Optional.of(ctor);
            }
        }
        return Optional.empty();
    }
    
    private Optional<Method> getValueOfStringMethod(final Class<?> klass)
    {
        try
        {
            return Optional.of(klass.getDeclaredMethod("valueOf", String.class));            
        }
        catch (NoSuchMethodException e) { }
        return Optional.empty();
    }
    
    private boolean hasDeclaredToString(final Class<?> klass)
    {
        for (final Method m : klass.getDeclaredMethods())
        {
            if (m.getName().equals("toString") &&
                    m.getReturnType() == String.class &&
                    m.getParameterCount() == 0)
                return true;
        }
        return false;
    }
    
    private Map<String, List<String>> parseFormData(final String body)
    {
        Map<String, List<String>> formParams = Maps.newHashMap();
        
        if (HttpMethod.POST == httpMethod ||
                HttpMethod.PUT == httpMethod)
        {
            Annotation consumesAnnotation = referencedMethod.getAnnotation(Consumes.class);
            if (null == consumesAnnotation) consumesAnnotation = referencedClass.getAnnotation(Consumes.class);
            
            if (null != consumesAnnotation)
            {
                List<String> contentTypes = Lists.newArrayList(((Consumes)consumesAnnotation).value());
                if (contentTypes.contains(MediaType.APPLICATION_FORM_URLENCODED))
                {
                    // Now we know this method expects a form, so decode the body
                    formParams = new QueryStringDecoder(body, false).parameters();
                }
            }
        }
        
        return formParams;
    }
}
