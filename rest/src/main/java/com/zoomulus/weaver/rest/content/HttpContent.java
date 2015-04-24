package com.zoomulus.weaver.rest.content;

import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import lombok.Getter;

import com.google.common.base.Strings;
import com.zoomulus.weaver.core.content.Content;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.contenttype.ContentTypeResolverStrategy;
import com.zoomulus.weaver.rest.contenttype.IntelligentContentTypeResolverStrategy;

public class HttpContent implements Content
{
    @Getter
    private final ContentType contentType;
    @Getter
    private final String content;
    
    public static Optional<HttpContent> create(final String messageBody,
            final List<ContentType> messageContentTypes,
            final List<ContentType> acceptedContentTypes)
    {
        if (Strings.isNullOrEmpty(messageBody))
        {
            return Optional.empty();
        }
        
        final ContentTypeResolverStrategy resolver = new IntelligentContentTypeResolverStrategy();
        final Optional<ContentType> contentType = resolver.resolve(messageContentTypes, acceptedContentTypes);
        if (contentType.isPresent())
        {
            String decodedBody = messageBody;
            try
            {
                decodedBody = URLDecoder.decode(messageBody, contentType.get().getEncoding());
            }
            catch (UnsupportedEncodingException e)
            {
                try
                {
                    decodedBody = URLDecoder.decode(messageBody, CharsetUtil.UTF_8.name());
                }
                catch (UnsupportedEncodingException e2) { }
            }

            return Optional.of(new HttpContent(contentType.get(), decodedBody));
        }
        
        return Optional.empty();
    }
    
    private HttpContent(final ContentType contentType, final String content)
    {
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public byte[] getContentBytes()
    {
        return content.getBytes();
    }
}
