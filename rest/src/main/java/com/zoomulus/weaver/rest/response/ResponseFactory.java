package com.zoomulus.weaver.rest.response;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.contenttype.ContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.contenttype.IntelligentContentTypeResolverStrategy;

public class ResponseFactory
{
    private final ContentTypeResolverStrategy contentTypeResolver;
    
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper xmlMapper = new XmlMapper();
    
    public ResponseFactory()
    {
        contentTypeResolver = new IntelligentContentTypeResolverStrategy();
    }
    
    public Response generate(
            @NonNull final Object emittedObject,
            final List<ContentType> expectedContentTypes,
            final List<ContentType> providedContentTypes)
    {
        try
        {
            Optional<ContentType> responseContentType =
                    contentTypeResolver.resolve(providedContentTypes, expectedContentTypes);
            
            boolean wantsJson = expects(expectedContentTypes, ContentType.APPLICATION_JSON_TYPE);
            boolean wantsXml = expects(expectedContentTypes, ContentType.APPLICATION_XML_TYPE);
            
            if (emittedObject instanceof Response)
            {
                if (! providedContentTypes.isEmpty() && ! responseContentType.isPresent())
                {
                    return Response.status(Status.NOT_ACCEPTABLE).build();
                }
                return (Response) emittedObject;
            }
            else if (! responseContentType.isPresent() &&
                    ! expectedContentTypes.isEmpty() &&
                    ! providedContentTypes.isEmpty())
            {
                // Couldn't agree on a content type
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
            
            Optional<String> stringRep = responseIsJson(responseContentType) ? Optional.ofNullable(jsonMapper.writeValueAsString(emittedObject))
                    : (responseIsXml(responseContentType) ? Optional.ofNullable(xmlMapper.writeValueAsString(emittedObject)) :
                        Optional.empty());
            
            if (! stringRep.isPresent())
            {
                if (emittedObject instanceof String && ! wantsJson && ! wantsXml)
                {
                    if (! responseContentType.isPresent()) responseContentType = Optional.of(ContentType.TEXT_PLAIN_TYPE);
                    stringRep = Optional.of((String) emittedObject);
                }
                else if (hasDeclaredToString(emittedObject.getClass()) && ! wantsJson && ! wantsXml)
                {
                    if (! responseContentType.isPresent()) responseContentType = Optional.of(ContentType.TEXT_PLAIN_TYPE);
                    stringRep = Optional.of(emittedObject.toString());
                }
                // Otherwise do a JSON conversion if possible
                else
                {
                    try
                    {
                        if (wantsXml)
                        {
                            stringRep = Optional.ofNullable(xmlMapper.writeValueAsString(emittedObject));
                            if (! responseContentType.isPresent())
                            {
                                responseContentType = Optional.of(ContentType.APPLICATION_XML_TYPE);
                            }
                        }
                        else
                        {
                            stringRep = Optional.ofNullable(jsonMapper.writeValueAsString(emittedObject));
                            if (! responseContentType.isPresent())
                            {
                                responseContentType = Optional.of(ContentType.APPLICATION_JSON_TYPE);
                            }
                        }
                    }
                    catch (JsonProcessingException e) { }
                }
                
                // As a last resort use whatever toString gives us
                if (! stringRep.isPresent())
                {
                    if (! responseContentType.isPresent())
                    {
                        responseContentType = Optional.of(ContentType.TEXT_PLAIN_TYPE);
                    }
                    stringRep = Optional.of(emittedObject.toString());
                }
            }
            
            if (! responseContentType.isPresent())
            {
                // We should have a response content type by now, regardless of @Provides declarations.
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
            else if (! expectedContentTypes.isEmpty())
            {
                // Since we may have guessed at the response content type based on the content being
                // returned, we need to double-check that this content type doesn't conflict with
                // anything indicated in any Accept headers.
                if (! (responseContentType =
                        contentTypeResolver.resolve(Lists.newArrayList(responseContentType.get()), expectedContentTypes)).isPresent())
                {
                    return Response.status(Status.NOT_ACCEPTABLE).build();
                }
            }
            
            if (stringRep.isPresent())
            {
                return Response
                        .status(Status.OK)
                        .entity(stringRep.get())
                        .type(responseContentType.isPresent() ? responseContentType.get().toString() : ContentType.TEXT_PLAIN)
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
        catch (JsonProcessingException e)
        {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private boolean expects(final List<ContentType> expectedContentTypes, final ContentType contentType)
    {
        return expectedContentTypes.size() == 1 && expectedContentTypes.get(0).isCompatibleWith(contentType);        
    }
    
    private boolean responseIsJson(final Optional<ContentType> rct)
    {
        return rct.isPresent() && rct.get().isCompatibleWith(ContentType.APPLICATION_JSON_TYPE);
    }
    
    private boolean responseIsXml(final Optional<ContentType> rct)
    {
        return rct.isPresent() && rct.get().isCompatibleWith(ContentType.APPLICATION_XML_TYPE);
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
}
