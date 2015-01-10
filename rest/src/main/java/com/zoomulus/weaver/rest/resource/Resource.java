package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.Value;
import lombok.experimental.Builder;

import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.rest.annotations.RequiredParam;

@Value
@Builder
public class Resource
{
    Class<?> referencedClass;
    Method referencedMethod;
    String path;
    HttpMethod httpMethod;
    List<String> consumesContentTypes;
    List<String> producesContentTypes;
    
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
                Annotation paramTypeAnnotation = null;
                Annotation defaultValueAnnotation = null;
                Annotation requiredParamAnnotation = null;
                boolean allowNullArg = false;
                for (final Annotation annotation : paramAnnotations)
                {
                    if (annotation instanceof PathParam ||
                            annotation instanceof MatrixParam ||
                            annotation instanceof QueryParam ||
                            annotation instanceof FormParam)
                    {
                        paramTypeAnnotation = annotation;
                    }
                    else if (annotation instanceof DefaultValue)
                    {
                        defaultValueAnnotation = annotation;
                    }
                    else if (annotation instanceof RequiredParam)
                    {
                        requiredParamAnnotation = annotation;
                    }
                }
                
                Class<?> parameterType = parameterTypes[idx];
                String s_arg = null;

                if (null != paramTypeAnnotation)
                {
                    if (paramTypeAnnotation instanceof PathParam)
                    {
                        final String paramValue = ((PathParam) paramTypeAnnotation).value();
                        
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
                    else if (paramTypeAnnotation instanceof MatrixParam)
                    {
                        s_arg = resourcePath.matrixParamGet(((MatrixParam) paramTypeAnnotation).value());
                    }
                    else if (paramTypeAnnotation instanceof QueryParam ||
                            paramTypeAnnotation instanceof FormParam)
                    {
                        final List<String> params = (paramTypeAnnotation instanceof QueryParam) ?
                                queryParams.get(((QueryParam) paramTypeAnnotation).value()) :
                                formParams.get(((FormParam) paramTypeAnnotation).value());
                        if (null != params && ! params.isEmpty())
                        {
                            if (List.class.isAssignableFrom(parameterType))
                            {
                                args.add(params);
                            }
                            else
                            {
                                s_arg = params.get(0);
                            }
                        }
                        else if (! parameterType.isPrimitive())
                        {
                            if (null != defaultValueAnnotation)
                            {
                                s_arg = ((DefaultValue) defaultValueAnnotation).value();
                            }
                            else
                            {
                                allowNullArg = (null == requiredParamAnnotation);
                            }
                        }
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
                else if (allowNullArg)
                {
                    args.add(null);
                }
            }
            ++idx;
        }
        
        return args.toArray();
    }
    
    public Response invoke(final String messageBody,
            final ResourcePath resourcePath,
            final Optional<HttpHeaders> headers,
            final Map<String, List<String>> queryParams)
    {
        Object response = null;
        try
        {
            final List<ContentType> acceptedContentTypes = getAcceptedContentTypes();
            final List<ContentType> requestContentTypes = getRequestContentTypes(headers);
            final ContentType contentType = getAgreedContentType(requestContentTypes, acceptedContentTypes);
            if (null == contentType && ! acceptedContentTypes.isEmpty())
            {
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
            
            String decodedBody = getDecodedBody(messageBody, contentType);
            
            Map<String, List<String>> formParams = parseFormData(decodedBody, contentType);
            
            Object[] args = populateArgs(messageBody, resourcePath, queryParams, formParams);
            if (referencedMethod.getParameters().length != args.length)
            {
                return Response.status(Status.BAD_REQUEST).build();
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
    
    public boolean consumes(final String contentType)
    {
        return consumesContentTypes.contains(contentType);
    }
    
    public boolean produces(final String contentType)
    {
        return producesContentTypes.contains(contentType);
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
    
    private List<ContentType> getAcceptedContentTypes()
    {
        Annotation consumesAnnotation = referencedMethod.getAnnotation(Consumes.class);
        if (null == consumesAnnotation) consumesAnnotation = referencedClass.getAnnotation(Consumes.class);
        final List<ContentType> acceptedContentTypes = Lists.newArrayList();
        if (null != consumesAnnotation)
        {
            for (final String cts : ((Consumes)consumesAnnotation).value())
            {
                try
                {
                    final ContentType ct = ContentType.parse(cts);
                    acceptedContentTypes.add(ct);
                }
                catch (ParseException | UnsupportedCharsetException e) { }
            }
        }
        return acceptedContentTypes;
    }
    
    private List<ContentType> getRequestContentTypes(final Optional<HttpHeaders> headers)
    {
        final List<ContentType> requestContentTypes = Lists.newArrayList();
        if (headers.isPresent())
        {
            for (final String cts : headers.get().getAll(HttpHeaders.Names.CONTENT_TYPE))
            {
                try
                {
                    final ContentType ct = ContentType.parse(cts);
                    requestContentTypes.add(ct);
                }
                catch (ParseException | UnsupportedCharsetException e) { }
            }
        }
        return requestContentTypes;
    }
    
    private ContentType getAgreedContentType(final List<ContentType> requestContentTypes, final List<ContentType> acceptedContentTypes)
    {
        for (final ContentType rct : requestContentTypes)
        {
            for (final ContentType act : acceptedContentTypes)
            {
                if (rct.getMimeType().equalsIgnoreCase(act.getMimeType()))
                {
                    return rct;
                }
            }
        }
        return null;
    }
    
    private String getDecodedBody(final String messageBody, final ContentType contentType)
    {
        Charset charset = null != contentType ? contentType.getCharset() : CharsetUtil.UTF_8;
        if (null == charset) charset = CharsetUtil.UTF_8;
        try
        {
            return URLDecoder.decode(messageBody, charset.name());
        }
        catch (UnsupportedEncodingException e1)
        {
            if (charset != CharsetUtil.UTF_8)
            {
                try
                {
                    return URLDecoder.decode(messageBody, CharsetUtil.UTF_8.name());
                }
                catch (UnsupportedEncodingException e2) { }
            }
        }
        
        return messageBody;
    }
    
    private Map<String, List<String>> parseFormData(final String body, final ContentType contentType)
    {
        Map<String, List<String>> formParams = Maps.newHashMap();
        
        if (null == body || null == contentType) return formParams;
        
        if (! contentType.getMimeType().equalsIgnoreCase(ContentType.APPLICATION_FORM_URLENCODED.getMimeType()))
            return formParams;
        
        if (HttpMethod.POST == httpMethod ||
                HttpMethod.PUT == httpMethod)
        {
            formParams = new QueryStringDecoder(body, false).parameters();
        }
        
        return formParams;
    }
}
