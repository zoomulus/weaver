package com.zoomulus.weaver.rest.contenttype;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

public class IntelligentContentTypeResolverStrategy implements
        ContentTypeResolverStrategy
{
    @Override
    public Optional<MediaType> resolve(final List<MediaType> providedContentTypes,
            final List<MediaType> expectedContentTypes, final String message)
    {
        return getAgreedContentType(providedContentTypes, expectedContentTypes);
    }
    
    private Optional<MediaType> getAgreedContentType(final List<MediaType> requestContentTypes, final List<MediaType> acceptedContentTypes)
    {
        for (final MediaType rct : requestContentTypes)
        {
            for (final MediaType act : acceptedContentTypes)
            {
                if (rct.toString().split(";")[0].equalsIgnoreCase(act.toString().split(";")[0]))
                {
                    return Optional.of(rct);
                }
            }
        }
        return Optional.empty();
    }
}
