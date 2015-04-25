package com.zoomulus.weaver.rest.response;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.contenttype.ContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.contenttype.IntelligentContentTypeResolverStrategy;

public class ResponseFactory
{
    private final ContentTypeResolverStrategy contentTypeResolver;
    
    public ResponseFactory()
    {
        contentTypeResolver = new IntelligentContentTypeResolverStrategy();
    }
    
    //public Response generate(
    public Optional<ContentType> generate(
            @NonNull final Object emittedObject,
            final List<ContentType> expectedContentTypes,
            final List<ContentType> providedContentTypes)
    {
        final Optional<ContentType> responseContentType = contentTypeResolver.resolve(providedContentTypes, expectedContentTypes);
        return responseContentType;
    }
}
