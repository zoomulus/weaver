package com.zoomulus.weaver.rest.contenttype;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

public interface ContentTypeResolverStrategy
{
    Optional<MediaType> resolve(final List<MediaType> providedContentTypes, final List<MediaType> expectedContentTypes, final String message);
}
