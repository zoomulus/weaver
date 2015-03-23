package com.zoomulus.weaver.rest.contenttype;

import java.util.List;
import java.util.Optional;

import com.zoomulus.weaver.core.content.ContentType;

public class IntelligentContentTypeResolverStrategy implements
        ContentTypeResolverStrategy
{
    @Override
    public Optional<ContentType> resolve(final List<ContentType> providedContentTypes,
            final List<ContentType> expectedContentTypes, final String message)
    {
        Optional<ContentType> contentType = getAgreedContentType(providedContentTypes, expectedContentTypes);
        
        if (! contentType.isPresent())
        {
            if (! providedContentTypes.isEmpty() && expectedContentTypes.isEmpty())
            {
                contentType = Optional.of(providedContentTypes.get(0));
            }
            else
            {
                boolean pctIsText = false;
                for (final ContentType pct : providedContentTypes)
                {
                    if (pct.isCompatibleWith(ContentType.TEXT_PLAIN_TYPE))
                    {
                        pctIsText = true;
                        break;
                    }
                }
                if (pctIsText)
                {
                    boolean hasJson = false;
                    boolean hasXml = false;
                    for (final ContentType ct : expectedContentTypes)
                    {
                        if (ct.isCompatibleWith(ContentType.APPLICATION_JSON_TYPE))
                        {
                            hasJson = true;
                            break;
                        }
                        else if (ct.isCompatibleWith(ContentType.APPLICATION_XML_TYPE))
                        {
                            hasXml = true; // don't break; we prefer json
                        }
                    }
                    if (hasJson)
                    {
                        contentType = Optional.of(ContentType.APPLICATION_JSON_TYPE);
                    }
                    else if (hasXml)
                    {
                        contentType = Optional.of(ContentType.APPLICATION_XML_TYPE);
                    }
                }
            }
        }
        
        return contentType;
    }
    
    private Optional<ContentType> getAgreedContentType(final List<ContentType> requestContentTypes,
            final List<ContentType> acceptedContentTypes)
    {
        for (final ContentType rct : requestContentTypes)
        {
            for (final ContentType act : acceptedContentTypes)
            {
                if (rct.isCompatibleWith(act))
                {
                    return Optional.of(rct);
                }
            }
        }
        return Optional.empty();
    }
}
