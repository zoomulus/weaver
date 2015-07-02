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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.content.HttpContent;
import com.zoomulus.weaver.rest.contenttype.ContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.contenttype.IntelligentContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.exceptions.StrictParamsMismatchException;
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
        
    private Object invokeEndpoint(final String messageBody,
            final ResourcePath resourcePath,
            final Optional<HttpHeaders> headers,
            final Map<String, List<String>> queryParams)
                    throws JsonParseException, JsonMappingException, InstantiationException, IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException, ResourceArgsBuilderException,
                    IOException, NoSuchMethodException, SecurityException, StrictParamsMismatchException
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
        Object resourceObj = referencedClass.getConstructor((Class<?>[])null).newInstance((Object[])null);
        return referencedMethod.invoke(resourceObj, args);
    }
    
    public Response invoke(final String messageBody,
            final ResourcePath resourcePath,
            final Optional<HttpHeaders> headers,
            final Map<String, List<String>> queryParams)
    {
        try
        {
            Object response = invokeEndpoint(messageBody, resourcePath, headers, queryParams);
            
            if (null == response) 
            {
                return Response.status(Status.NO_CONTENT).build();
            }
            
            final List<ContentType> acceptContentTypes = getAcceptContentTypes(headers);
            final List<ContentType> producesContentTypes = getProducesContentTypes(response);
            
            return new ResponseFactory().generate(response, acceptContentTypes, producesContentTypes);            
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        catch (StrictParamsMismatchException e)
        {
            e.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
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
