package com.zoomulus.weaver.core.content;

import io.netty.util.CharsetUtil;

import java.util.Collections;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import com.google.common.collect.Maps;

@Value
@EqualsAndHashCode
public class ContentType
{
    @NonNull String mediaType;
    @NonNull String encoding;
    
    public ContentType(@NonNull final String mediaType, @NonNull final String encoding)
    {
        if (! mediaType.contains("/"))
        {
            throw new IllegalArgumentException("Invalid media type " + mediaType);
        }
        this.mediaType = mediaType;
        this.encoding = encoding;
    }
    
    public ContentType(@NonNull final String mediaType)
    {
        this(mediaType, CharsetUtil.UTF_8.name());
    }
    
    public String toString()
    {
        return mediaType + "; " + encoding;
    }
    
    public boolean isCompatibleWith(final ContentType rhs)
    {
        return mediaType.equals(rhs.mediaType);
    }
    
    public static ContentType valueOf(@NonNull final String cts)
    {
        final String key = cts.split(";")[0];
        if (! valueMap.containsKey(key))
        {
            throw new IllegalArgumentException(cts);
        }
        final ContentType ct = valueMap.get(key);
        return ct;
    }
    
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    public static final ContentType APPLICATION_ATOM_XML_TYPE = new ContentType(ContentType.APPLICATION_ATOM_XML);
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final ContentType APPLICATION_FORM_URLENCODED_TYPE = new ContentType(ContentType.APPLICATION_FORM_URLENCODED);
    public static final String APPLICATION_JSON = "application/json";
    public static final ContentType APPLICATION_JSON_TYPE = new ContentType(ContentType.APPLICATION_JSON);
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final ContentType APPLICATION_OCTET_STREAM_TYPE = new ContentType(ContentType.APPLICATION_OCTET_STREAM);
    public static final String APPLICATION_SVG_XML = "application/svg+xml";
    public static final ContentType APPLICATION_SVG_XML_TYPE = new ContentType(ContentType.APPLICATION_SVG_XML);
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    public static final ContentType APPLICATION_XHTML_XML_TYPE = new ContentType(ContentType.APPLICATION_XHTML_XML);
    public static final String APPLICATION_XML = "application/xml";
    public static final ContentType APPLICATION_XML_TYPE = new ContentType(ContentType.APPLICATION_XML);
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final ContentType MULTIPART_FORM_DATA_TYPE = new ContentType(ContentType.MULTIPART_FORM_DATA);
    public static final String TEXT_HTML = "text/html";
    public static final ContentType TEXT_HTML_TYPE = new ContentType(ContentType.TEXT_HTML);
    public static final String TEXT_PLAIN = "text/plain";
    public static final ContentType TEXT_PLAIN_TYPE = new ContentType(ContentType.TEXT_PLAIN);
    public static final String TEXT_XML = "text/xml";
    public static final ContentType TEXT_XML_TYPE = new ContentType(ContentType.TEXT_XML);
    public static final String WILDCARD = "*/*";    
    public static final ContentType WILDCARD_TYPE = new ContentType(ContentType.WILDCARD);
    
    private static final Map<String, ContentType> valueMap;
    static {
        final Map<String, ContentType> m = Maps.newHashMap();
        m.put(APPLICATION_ATOM_XML, APPLICATION_ATOM_XML_TYPE);
        m.put(APPLICATION_FORM_URLENCODED, APPLICATION_FORM_URLENCODED_TYPE);
        m.put(APPLICATION_JSON, APPLICATION_JSON_TYPE);
        m.put(APPLICATION_OCTET_STREAM, APPLICATION_OCTET_STREAM_TYPE);
        m.put(APPLICATION_SVG_XML, APPLICATION_SVG_XML_TYPE);
        m.put(APPLICATION_XHTML_XML, APPLICATION_XHTML_XML_TYPE);
        m.put(APPLICATION_XML, APPLICATION_XML_TYPE);
        m.put(MULTIPART_FORM_DATA, MULTIPART_FORM_DATA_TYPE);
        m.put(TEXT_HTML, TEXT_HTML_TYPE);
        m.put(TEXT_PLAIN, TEXT_PLAIN_TYPE);
        m.put(TEXT_XML, TEXT_XML_TYPE);
        m.put(WILDCARD, WILDCARD_TYPE);
        valueMap = Collections.unmodifiableMap(m);
    }
}
