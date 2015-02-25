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
        Optional<MediaType> contentType = getAgreedContentType(providedContentTypes, expectedContentTypes);
        
        if (! contentType.isPresent())
        {
            if (! providedContentTypes.isEmpty() && expectedContentTypes.isEmpty())
            {
                contentType = Optional.of(providedContentTypes.get(0));
            }
            else
            {
                boolean pctIsText = false;
                for (final MediaType pct : providedContentTypes)
                {
                    if (pct.toString().split(";")[0].equals(MediaType.TEXT_PLAIN))
                    {
                        pctIsText = true;
                        break;
                    }
                }
                if (pctIsText)
                {
                    boolean hasJson = false;
                    boolean hasXml = false;
                    for (final MediaType mt : expectedContentTypes)
                    {
                        if (mt.toString().split(";")[0].equals(MediaType.APPLICATION_JSON))
                        {
                            hasJson = true;
                            break;
                        }
                        else if (mt.toString().split(";")[0].equals(MediaType.APPLICATION_XML))
                        {
                            hasXml = true; // don't break; we prefer json
                        }
                    }
                    if (hasJson)
                    {
                        contentType = Optional.of(MediaType.APPLICATION_JSON_TYPE);
                    }
                    else if (hasXml)
                    {
                        contentType = Optional.of(MediaType.APPLICATION_XML_TYPE);
                    }
                }
            }
        }
        
        return contentType;
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
