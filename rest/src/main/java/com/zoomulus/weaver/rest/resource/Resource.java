package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.Value;
import lombok.experimental.Builder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.annotations.StrictParams;
import com.zoomulus.weaver.rest.content.HttpContent;
import com.zoomulus.weaver.rest.contenttype.ContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.contenttype.IntelligentContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.resource.ResourceArgs.ResourceArgsBuilder.ResourceArgsBuilderException;
import com.zoomulus.weaver.rest.response.ResponseFactory;

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
    
    static ObjectMapper jsonMapper = new ObjectMapper();
    static XmlMapper xmlMapper = new XmlMapper();
    
    ContentTypeResolverStrategy inboundContentTypeResolverStrategy =
            new IntelligentContentTypeResolverStrategy();
    
    // TODO:
    // Handle javax.ws.rs.WebApplicationException (chap 7)
    // Support all injected parameter types:
    //  - HeaderParam
    //  - CookieParam
    //  - BeanParam
    //  - Context
    //  - Encoded
    // Support ParamConverter<T>
    // Ensure most optimal match works
    
//    private Optional<Constructor<?>> getStringConstructor(final Class<?> klass)
//    {
//        for (final Constructor<?> ctor : klass.getConstructors())
//        {
//            final Class<?>[] params = ctor.getParameterTypes();
//            if (params.length != 1)
//            {
//                continue;
//            }
//            if (params[0] == String.class)
//            {
//                return Optional.of(ctor);
//            }
//        }
//        return Optional.empty();
//    }
//    
//    private Optional<Method> getValueOfStringMethod(final Class<?> klass)
//    {
//        try
//        {
//            return Optional.of(klass.getDeclaredMethod("valueOf", String.class));            
//        }
//        catch (NoSuchMethodException e) { }
//        return Optional.empty();
//    }
    
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
                    acceptedContentTypes.add(ContentType.valueOf(cts));
                }
                catch (IllegalArgumentException e) { }
            }
        }
        return acceptedContentTypes;
    }
    
    private List<ContentType> getRequestContentTypes(final Optional<HttpHeaders> headers)
    {
        return getContentTypesForHeader(headers, HttpHeaders.Names.CONTENT_TYPE);
    }
    
    private List<ContentType> getAcceptContentTypes(final Optional<HttpHeaders> headers)
    {
        return getContentTypesForHeader(headers, HttpHeaders.Names.ACCEPT);
    }
    
    private List<ContentType> getContentTypesForHeader(final Optional<HttpHeaders> headers, final String headerName)
    {
        final List<ContentType> requestContentTypes = Lists.newArrayList();
        if (headers.isPresent())
        {
            for (final String cts : headers.get().getAll(headerName))
            {
                try
                {
                    requestContentTypes.add(ContentType.valueOf(cts));
                }
                catch (IllegalArgumentException e) { }
            }
        }
        return requestContentTypes;
    }
    
    private ContentType getAgreedContentType(final List<ContentType> requestContentTypes,
            final List<ContentType> acceptedContentTypes)
    {
        for (final ContentType rct : requestContentTypes)
        {
            for (final ContentType act : acceptedContentTypes)
            {
                if (rct.isCompatibleWith(act))
                {
                    return rct;
                }
            }
        }
        return null;
    }
    
    private List<ContentType> getProducesContentTypes(final Object response)
    {
        final List<ContentType> contentTypes = Lists.newArrayList();
        if (response instanceof Response && ((Response)response).getMediaType() != null)
        {
            contentTypes.add(new ContentType(((Response)response).getMediaType().toString()));
        }
        else
        {
            Annotation producesAnnotation = referencedMethod.getAnnotation(Produces.class);
            if (null == producesAnnotation) producesAnnotation = referencedClass.getAnnotation(Produces.class);
            if (null != producesAnnotation)
            {
                for (final String cts : ((Produces)producesAnnotation).value())
                {
                    try
                    {
                        contentTypes.add(ContentType.valueOf(cts));
                    }
                    catch (IllegalArgumentException e)
                    {
                        try
                        {
                            contentTypes.add(new ContentType(cts));
                        }
                        catch (IllegalArgumentException e2) { }
                    }
                }
            }
        }
        return contentTypes;
    }
        
//    private String getDecodedBody(final String messageBody) // , final ContentType contentType)
//    {
//        try
//        {
//            return URLDecoder.decode(messageBody, CharsetUtil.UTF_8.name());
//        }
//        catch (UnsupportedEncodingException e) { }
//        
//        return messageBody;
//    }
    
//    private Map<String, List<String>> parseFormData(final String body, final Optional<ContentType> contentType)
//    {
//        Map<String, List<String>> formParams = Maps.newHashMap();
//        
//        if (null == body || ! contentType.isPresent()) return formParams;
//        
//        if (! contentType.get().isCompatibleWith(ContentType.APPLICATION_FORM_URLENCODED_TYPE))
//            return formParams;
//        
//        if (HttpMethod.POST == httpMethod ||
//                HttpMethod.PUT == httpMethod)
//        {
//            formParams = new QueryStringDecoder(body, false).parameters();
//        }
//        
//        return formParams;
//    }    
    
    private boolean passesStrictParamsCheck(int nArgs, int nQueryParams, int nFormParams)
    {
        boolean hasStrictParams = false;
        for (final Annotation annotation : referencedMethod.getAnnotations())
        {
            if (annotation instanceof StrictParams)
            {
                hasStrictParams = true;
                break;
            }
        }
        if (hasStrictParams && nArgs != (nQueryParams + nFormParams))
        {
            return false;
        }
        return true;
    }
    
//    private boolean expectsMessageBody()
//    {
//        for (final Annotation[] paramAnnotations : referencedMethod.getParameterAnnotations())
//        {
//            if (0 == paramAnnotations.length) return true;
//        }
//        return false;
//    }
    
//    private Object getParameterOfMatchingType(final Class<?> parameterType, final String s_arg)
//            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
//    {
//        Object arg = null;
//        if (parameterType.isPrimitive())
//        {
//            if (parameterType == boolean.class) { arg = Boolean.valueOf(s_arg); }
//            else if (parameterType == byte.class) { arg = Byte.valueOf(s_arg); }
//            else if (parameterType == short.class) { arg = Short.valueOf(s_arg); }
//            else if (parameterType == int.class) { arg = Integer.valueOf(s_arg); }
//            else if (parameterType == long.class) { arg = Long.valueOf(s_arg); }
//            else if (parameterType == float.class) { arg = Float.valueOf(s_arg); }
//            else if (parameterType == double.class) { arg = Double.valueOf(s_arg); }
//        }
//        else
//        {
//            Optional<Constructor<?>> stringConstructor = getStringConstructor(parameterType);
//            if (stringConstructor.isPresent())
//            {
//                arg = stringConstructor.get().newInstance(s_arg);
//            }
//            else
//            {
//                Optional<Method> valueOfStringMethod = getValueOfStringMethod(parameterType);
//                if (valueOfStringMethod.isPresent())
//                {
//                    arg = valueOfStringMethod.get().invoke(null, s_arg);
//                }
//                else
//                {
//                    arg = s_arg;
//                }
//            }
//        }
//        return arg;
//    }
    
//    private Object[] populateArgs(final String messageBody,
//            final ResourcePath resourcePath,
//            final Map<String, List<String>> queryParams,
//            final Map<String, List<String>> formParams,
//            final Optional<ContentType> inboundContentType)
//            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JsonParseException, JsonMappingException, IOException
//    {
//        final List<Object> args = Lists.newArrayList();
//        
//        Class<?>[] parameterTypes = referencedMethod.getParameterTypes();
//        
//        int idx = 0;
//        for (final Annotation[] paramAnnotations : referencedMethod.getParameterAnnotations())
//        {
//            if (0 == paramAnnotations.length)
//            {
//                boolean added = false;
//                if (inboundContentType.isPresent() && parameterTypes[idx] != String.class && ! parameterTypes[idx].getName().equals("[B"))
//                {
//                    if (inboundContentType.get().isCompatibleWith(ContentType.APPLICATION_JSON_TYPE))
//                    {
//                        args.add(jsonMapper.readValue(messageBody, parameterTypes[idx]));
//                        added = true;
//                    }
//                    else if (inboundContentType.get().isCompatibleWith(ContentType.APPLICATION_XML_TYPE))
//                    {
//                        args.add(xmlMapper.readValue(messageBody, parameterTypes[idx]));
//                        added = true;
//                    }
//                    else if (inboundContentType.get().isCompatibleWith(ContentType.TEXT_PLAIN_TYPE))
//                    {
//                        if (parameterTypes[idx].isPrimitive())
//                        {
//                            final Class<?> parameterType = parameterTypes[idx]; 
//                            if (parameterType == boolean.class) { args.add(Boolean.valueOf(messageBody)); added=true; }
//                            else if (parameterType == byte.class) { args.add(Byte.valueOf(messageBody)); added=true; }
//                            else if (parameterType == short.class) { args.add(Short.valueOf(messageBody)); added=true; }
//                            else if (parameterType == int.class) { args.add(Integer.valueOf(messageBody)); added=true; }
//                            else if (parameterType == long.class) { args.add(Long.valueOf(messageBody)); added=true; }
//                            else if (parameterType == float.class) { args.add(Float.valueOf(messageBody)); added=true; }
//                            else if (parameterType == double.class) { args.add(Double.valueOf(messageBody)); added=true; }                            
//                        }
//                        else
//                        {
//                            Optional<Constructor<?>> ctor = getStringConstructor(parameterTypes[idx]);
//                            if (ctor.isPresent())
//                            {
//                                args.add(ctor.get().newInstance(messageBody));
//                                added = true;
//                            }
//                            else
//                            {
//                                Optional<Method> valueOf = getValueOfStringMethod(parameterTypes[idx]);
//                                if (valueOf.isPresent())
//                                {
//                                    args.add(valueOf.get().invoke(null, messageBody));
//                                    added = true;
//                                }
//                            }
//                        }
//                    }
//                }
//                
//                if (! added)
//                {
//                    if (parameterTypes[idx].getName().equals("[B"))
//                    {
//                        args.add(messageBody.getBytes());
//                    }
//                    else
//                    {
//                        args.add(messageBody);
//                    }
//                }
//            }
//            else
//            {
//                Annotation paramTypeAnnotation = null;
//                Annotation defaultValueAnnotation = null;
//                Annotation requiredParamAnnotation = null;
//                boolean allowNullArg = false;
//                for (final Annotation annotation : paramAnnotations)
//                {
//                    if (annotation instanceof PathParam ||
//                            annotation instanceof MatrixParam ||
//                            annotation instanceof QueryParam ||
//                            annotation instanceof FormParam)
//                    {
//                        paramTypeAnnotation = annotation;
//                    }
//                    else if (annotation instanceof DefaultValue)
//                    {
//                        defaultValueAnnotation = annotation;
//                    }
//                    else if (annotation instanceof RequiredParam)
//                    {
//                        requiredParamAnnotation = annotation;
//                    }
//                }
//                
//                Class<?> parameterType = parameterTypes[idx];
//                String s_arg = null;
//
//                if (null != paramTypeAnnotation)
//                {
//                    if (paramTypeAnnotation instanceof PathParam)
//                    {
//                        final String paramValue = ((PathParam) paramTypeAnnotation).value();
//                        
//                        if (PathSegment.class.isAssignableFrom(parameterType))
//                        {
//                            Optional<PathSegment> ps = resourcePath.getPathSegment(paramValue);
//                            if (ps.isPresent()) args.add(ps.get());
//                        }
//                        else
//                        {
//                            s_arg = resourcePath.get(paramValue);
//                        }
//                    }
//                    else if (paramTypeAnnotation instanceof MatrixParam)
//                    {
//                        s_arg = resourcePath.matrixParamGet(((MatrixParam) paramTypeAnnotation).value());
//                    }
//                    else if (paramTypeAnnotation instanceof QueryParam ||
//                            paramTypeAnnotation instanceof FormParam)
//                    {
//                        final List<String> params = (paramTypeAnnotation instanceof QueryParam) ?
//                                queryParams.get(((QueryParam) paramTypeAnnotation).value()) :
//                                formParams.get(((FormParam) paramTypeAnnotation).value());
//                        if (null != params && ! params.isEmpty())
//                        {
//                            if (List.class.isAssignableFrom(parameterType))
//                            {
//                                args.add(params);
//                            }
//                            else
//                            {
//                                s_arg = params.get(0);
//                            }
//                        }
//                        else if (null != defaultValueAnnotation)
//                        {
//                            s_arg = ((DefaultValue) defaultValueAnnotation).value();
//                        }
//                        else if (! parameterType.isPrimitive())
//                        {
//                            allowNullArg = (null == requiredParamAnnotation);
//                        }
//                    }
//                }
//                
//                if (null != s_arg)
//                {
//                    Object arg = getParameterOfMatchingType(parameterType, s_arg);
//                    if (null != arg)
//                    {
//                        args.add(arg);
//                    }
//                }
//                else if (allowNullArg)
//                {
//                    args.add(null);
//                }
//            }
//            ++idx;
//        }
//        
//        return args.toArray();
//    }
    
    private Object invokeEndpoint(final String messageBody,
            final ResourcePath resourcePath,
            final Optional<HttpHeaders> headers,
            final Map<String, List<String>> queryParams)
                    throws JsonParseException, JsonMappingException, InstantiationException, IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException, ResourceArgsBuilderException,
                    IOException, NoSuchMethodException, SecurityException
    {
        boolean methodRequiresPayload = (HttpMethod.PUT == httpMethod
                || HttpMethod.POST == httpMethod
                || HttpMethod.DELETE == httpMethod
                || HttpMethod.PATCH == httpMethod);
        boolean methodLacksPayload = ! methodRequiresPayload;

        final List<ContentType> acceptedInboundContentTypes = getAcceptedContentTypes();
        
        if (! acceptedInboundContentTypes.isEmpty() && methodLacksPayload)
        {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        
        final Optional<HttpContent> content = HttpContent.create(messageBody,
                getRequestContentTypes(headers),
                acceptedInboundContentTypes);
        
        if (content.isPresent())
        {
            if (methodLacksPayload)
            {
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        else if (! Strings.isNullOrEmpty(messageBody))
        {
            return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        
        final ResourceArgs resourceArgs = ResourceArgs.builder()
                .content(content)
                .resourcePath(resourcePath)
                .httpMethod(httpMethod)
                .referencedMethod(referencedMethod)
                .queryParams(queryParams)
                .build();
        
        final Object[] args = resourceArgs.getArgs();
        
        if (referencedMethod.getParameters().length != args.length)
        {
            return Response.status(Status.BAD_REQUEST).build();
        }
        else if (! passesStrictParamsCheck(args.length, queryParams.size(), resourceArgs.getFormParams().size()))
        {
            return Response.status(Status.BAD_REQUEST).build();
        }
        Object resourceObj = referencedClass.getConstructor((Class<?>[])null).newInstance((Object[])null);
        return referencedMethod.invoke(resourceObj, args);
    }
    
    public Response invoke(final String messageBody,
            final ResourcePath resourcePath,
            final Optional<HttpHeaders> headers,
            final Map<String, List<String>> queryParams)
    {
        // TODO:  This method - probably the whole class - needs a complete refactor.
        try
        {
            Object response = invokeEndpoint(messageBody, resourcePath, headers, queryParams);
            
            if (null == response) 
            {
                return Response.status(Status.NO_CONTENT).build();
            }
            
            final List<ContentType> acceptContentTypes = getAcceptContentTypes(headers);
            final List<ContentType> producesContentTypes = getProducesContentTypes(response);
            
            //return new ResponseFactory().generate(response, acceptContentTypes, producesContentTypes);
            
            Optional<ContentType> producesContentType =
                    new ResponseFactory().generate(response, acceptContentTypes, producesContentTypes);
//                    acceptContentTypes.isEmpty() ?
//                            (producesContentTypes.size() == 1 ?
//                                    Optional.of(producesContentTypes.get(0)) : Optional.empty()) :
//                            Optional.ofNullable(getAgreedContentType(acceptContentTypes, producesContentTypes));
                                    
            Optional<String> stringRep = Optional.empty();
            boolean wantsJson = acceptContentTypes.size() == 1 &&
                    acceptContentTypes.get(0).isCompatibleWith(ContentType.APPLICATION_JSON_TYPE);
            boolean wantsXml = acceptContentTypes.size() == 1 &&
                    acceptContentTypes.get(0).isCompatibleWith(ContentType.APPLICATION_XML_TYPE);
            boolean triedJsonConversion = false;
            
            if (response instanceof Response)
            {
                if (! acceptContentTypes.isEmpty() && ! producesContentType.isPresent())
                {
                    return Response.status(Status.NOT_ACCEPTABLE).build();
                }
                return (Response) response;
            }
            
            if (producesContentType.isPresent())
            {
                try
                {
                    if (producesContentType.get().isCompatibleWith(ContentType.APPLICATION_JSON_TYPE))
                    {
                        triedJsonConversion = true;
                        stringRep = Optional.ofNullable(jsonMapper.writeValueAsString(response));
                    }
                    else if (producesContentType.get().isCompatibleWith(ContentType.APPLICATION_XML_TYPE))
                    {
                        stringRep = Optional.ofNullable(xmlMapper.writeValueAsString(response));
                    }
                }
                catch (JsonProcessingException e)
                {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
                }
            }
            else if (! acceptContentTypes.isEmpty() && ! producesContentTypes.isEmpty())
            {
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
            
            if (! stringRep.isPresent())
            {
                if (response instanceof String && ! wantsJson && ! wantsXml)
                {
                    if (! producesContentType.isPresent())
                    {
                        producesContentType = Optional.of(ContentType.TEXT_PLAIN_TYPE);
                    }
                    stringRep = Optional.of((String) response);
                }
                else if (hasDeclaredToString(response.getClass()) && ! wantsJson && ! wantsXml)
                {
                    if (! producesContentType.isPresent())
                    {
                        producesContentType = Optional.of(ContentType.TEXT_PLAIN_TYPE);
                    }
                    stringRep = Optional.of(response.toString());
                }
                // Otherwise do a JSON conversion if possible
                else if (! triedJsonConversion)
                {
                    try
                    {
                        if (wantsXml)
                        {
                            stringRep = Optional.ofNullable(xmlMapper.writeValueAsString(response));
                            if (! producesContentType.isPresent())
                            {
                                producesContentType = Optional.of(ContentType.APPLICATION_XML_TYPE);
                            }
                        }
                        else
                        {
                            stringRep = Optional.ofNullable(jsonMapper.writeValueAsString(response));
                            if (! producesContentType.isPresent())
                            {
                                producesContentType = Optional.of(ContentType.APPLICATION_JSON_TYPE);
                            }
                        }
                    }
                    catch (JsonProcessingException e) { }
                }
                
                // As a last resort use whatever toString gives us
                if (! stringRep.isPresent())
                {
                    if (! producesContentType.isPresent())
                    {
                        producesContentType = Optional.of(ContentType.TEXT_PLAIN_TYPE);
                    }
                    stringRep = Optional.of(response.toString());
                }
            }
            
            if (! producesContentType.isPresent() || (! acceptContentTypes.isEmpty()))
            {
                final ContentType pct = producesContentType.get();
                boolean found = false;
                for (final ContentType act : acceptContentTypes)
                {
                    if (pct.isCompatibleWith(act))
                    {
                        found = true;
                        break;
                    }
                }
                if (! found)
                {
                    return Response.status(Status.NOT_ACCEPTABLE).build();
                }
            }
            
            if (stringRep.isPresent())
            {
                return Response
                        .status(Status.OK)
                        .entity(stringRep.get())
                        .type(producesContentType.isPresent() ? producesContentType.get().toString() : ContentType.TEXT_PLAIN)
                        .build();
            }
            else
            {
                return Response
                        .status(Status.NO_CONTENT)
                        .entity(null)
                        .build();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ResourceArgsBuilderException e)
        {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
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
}
