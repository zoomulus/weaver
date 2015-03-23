package com.zoomulus.weaver.rest.contenttype;

import java.util.List;
import java.util.Optional;

import com.zoomulus.weaver.core.content.ContentType;

public interface ContentTypeResolverStrategy
{
    Optional<ContentType> resolve(final List<ContentType> providedContentTypes,
            final List<ContentType> expectedContentTypes,
            final String message);
}
