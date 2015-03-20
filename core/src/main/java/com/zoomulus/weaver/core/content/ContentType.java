package com.zoomulus.weaver.core.content;

import io.netty.util.CharsetUtil;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import com.google.common.collect.Maps;

@Value
@AllArgsConstructor
@EqualsAndHashCode(exclude="encoding")
public class ContentType
{
    @Getter
    @NonNull MediaType mediaType;
    @Getter
    @NonNull String encoding;
    
    public ContentType(final MediaType mediaType)
    {
        this(mediaType, CharsetUtil.UTF_8.name());
    }
    
    public ContentType(@NonNull final String mediaType, @NonNull final String encoding)
    {
        final String[] parts = mediaType.split("/", 2);
        if (2 > parts.length) throw new RuntimeException("Unsupported media type " + mediaType);
        this.mediaType = new MediaType(parts[0], parts[1]);
        this.encoding = encoding;
    }
    
    public ContentType(@NonNull final String mediaType)
    {
        this(mediaType, CharsetUtil.UTF_8.name());
    }
    
    public static ContentType valueOf(@NonNull final String cts)
    {
        return valueMap.containsKey(cts) ? valueMap.get(cts) : null;
    }
    
    public static final String APPLICATION_ATOM_XML = MediaType.APPLICATION_ATOM_XML;
    public static final ContentType APPLICATION_ATOM_XML_TYPE = new ContentType(MediaType.APPLICATION_ATOM_XML_TYPE);
    public static final String APPLICATION_FORM_URLENCODED = MediaType.APPLICATION_FORM_URLENCODED;
    public static final ContentType APPLICATION_FORM_URLENCODED_TYPE = new ContentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    public static final String APPLICATION_JSON = MediaType.APPLICATION_JSON;
    public static final ContentType APPLICATION_JSON_TYPE = new ContentType(MediaType.APPLICATION_JSON_TYPE);
    public static final String APPLICATION_OCTET_STREAM = MediaType.APPLICATION_OCTET_STREAM;
    public static final ContentType APPLICATION_OCTET_STREAM_TYPE = new ContentType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    public static final String APPLICATION_SVG_XML = MediaType.APPLICATION_SVG_XML;
    public static final ContentType APPLICATION_SVG_XML_TYPE = new ContentType(MediaType.APPLICATION_SVG_XML_TYPE);
    public static final String APPLICATION_XHTML_XML = MediaType.APPLICATION_XHTML_XML;
    public static final ContentType APPLICATION_XHTML_XML_TYPE = new ContentType(MediaType.APPLICATION_XHTML_XML_TYPE);
    public static final String APPLICATION_XML = MediaType.APPLICATION_XML;
    public static final ContentType APPLICATION_XML_TYPE = new ContentType(MediaType.APPLICATION_XML_TYPE);
    public static final String MULTIPART_FORM_DATA = MediaType.MULTIPART_FORM_DATA;
    public static final ContentType MULTIPART_FORM_DATA_TYPE = new ContentType(MediaType.MULTIPART_FORM_DATA_TYPE);
    public static final String TEXT_HTML = MediaType.TEXT_HTML;
    public static final ContentType TEXT_HTML_TYPE = new ContentType(MediaType.TEXT_HTML_TYPE);
    public static final String TEXT_PLAIN = MediaType.TEXT_PLAIN;
    public static final ContentType TEXT_PLAIN_TYPE = new ContentType(MediaType.TEXT_PLAIN_TYPE);
    public static final String TEXT_XML = MediaType.TEXT_XML;
    public static final ContentType TEXT_XML_TYPE = new ContentType(MediaType.TEXT_XML_TYPE);
    public static final String WILDCARD = MediaType.WILDCARD;    
    public static final ContentType WILDCARD_TYPE = new ContentType(MediaType.WILDCARD_TYPE);
    
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
