package com.zoomulus.weaver.core.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import io.netty.util.CharsetUtil;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ContentTypeTest
{
    @Test
    public void testConstruct()
    {
        final ContentType ct = new ContentType(MediaType.APPLICATION_JSON_TYPE, CharsetUtil.UTF_16.name());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_16.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructWithoutEncoding()
    {
        final ContentType ct = new ContentType(MediaType.APPLICATION_JSON_TYPE);
        assertEquals(MediaType.APPLICATION_JSON_TYPE, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructWithString()
    {
        final ContentType ct = new ContentType(MediaType.APPLICATION_JSON);
        assertEquals(MediaType.APPLICATION_JSON_TYPE, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructWithStringAndEncoding()
    {
        final ContentType ct = new ContentType(MediaType.APPLICATION_JSON, CharsetUtil.UTF_8.name());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructWithNullStringThrowsException()
    {
        try
        {
            new ContentType((String)null);
            fail();
        }
        catch (NullPointerException e) { }
    }
    
    @Test
    public void testConstructWithInvalidMediaTypeStringThrowsException()
    {
        try
        {
            new ContentType("invalid");
            fail();
        }
        catch (RuntimeException e) { }
    }
    
    @Test
    public void testConstructWithThreePartMediaType()
    {
        final ContentType ct = new ContentType("a/b/c");
        assertEquals("a", ct.getMediaType().getType());
        assertEquals("b/c", ct.getMediaType().getSubtype());
    }
    
    @Test
    public void testConstructCustom()
    {
        final MediaType fakeMediaType = new MediaType("application", "fake");
        final ContentType ct = new ContentType(fakeMediaType);
        assertEquals(fakeMediaType, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructNullMediaTypeThrowsException()
    {
        try
        {
            new ContentType((MediaType)null);
            fail();
        }
        catch (NullPointerException e) { }
    }
    
    @Test
    public void testConstructNullEncodingThrowsException()
    {
        try
        {
            new ContentType(MediaType.APPLICATION_JSON_TYPE, null);
            fail();
        }
        catch (NullPointerException e) { }
    }
    
    @Test
    public void testConstants()
    {
        final Map<ContentType, String> m = Maps.newHashMap();
        m.put(ContentType.APPLICATION_ATOM_XML_TYPE, ContentType.APPLICATION_ATOM_XML);
        m.put(ContentType.APPLICATION_FORM_URLENCODED_TYPE, ContentType.APPLICATION_FORM_URLENCODED);
        m.put(ContentType.APPLICATION_JSON_TYPE, ContentType.APPLICATION_JSON);
        m.put(ContentType.APPLICATION_OCTET_STREAM_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        m.put(ContentType.APPLICATION_SVG_XML_TYPE, ContentType.APPLICATION_SVG_XML);
        m.put(ContentType.APPLICATION_XHTML_XML_TYPE, ContentType.APPLICATION_XHTML_XML);
        m.put(ContentType.APPLICATION_XML_TYPE, ContentType.APPLICATION_XML);
        m.put(ContentType.MULTIPART_FORM_DATA_TYPE, ContentType.MULTIPART_FORM_DATA);
        m.put(ContentType.TEXT_HTML_TYPE, ContentType.TEXT_HTML);
        m.put(ContentType.TEXT_PLAIN_TYPE, ContentType.TEXT_PLAIN);
        m.put(ContentType.TEXT_XML_TYPE, ContentType.TEXT_XML);
        m.put(ContentType.WILDCARD_TYPE, ContentType.WILDCARD);
        
        for (final ContentType ct : m.keySet())
        {
            assertEquals(ct, ContentType.valueOf(m.get(ct)));
        }
        
        final Map<ContentType, MediaType> m2 = Maps.newHashMap();
        m2.put(ContentType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_ATOM_XML_TYPE);
        m2.put(ContentType.APPLICATION_FORM_URLENCODED_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        m2.put(ContentType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
        m2.put(ContentType.APPLICATION_OCTET_STREAM_TYPE, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        m2.put(ContentType.APPLICATION_SVG_XML_TYPE, MediaType.APPLICATION_SVG_XML_TYPE);
        m2.put(ContentType.APPLICATION_XHTML_XML_TYPE, MediaType.APPLICATION_XHTML_XML_TYPE);
        m2.put(ContentType.APPLICATION_XML_TYPE, MediaType.APPLICATION_XML_TYPE);
        m2.put(ContentType.MULTIPART_FORM_DATA_TYPE, MediaType.MULTIPART_FORM_DATA_TYPE);
        m2.put(ContentType.TEXT_HTML_TYPE, MediaType.TEXT_HTML_TYPE);
        m2.put(ContentType.TEXT_PLAIN_TYPE, MediaType.TEXT_PLAIN_TYPE);
        m2.put(ContentType.TEXT_XML_TYPE, MediaType.TEXT_XML_TYPE);
        m2.put(ContentType.WILDCARD_TYPE, MediaType.WILDCARD_TYPE);
        
        for (final ContentType ct : m2.keySet())
        {
            assertEquals(ct.getMediaType(), m2.get(ct));
        }
    }
    
    @Test
    public void testApplicationJsonType()
    {
        final ContentType ct = ContentType.APPLICATION_JSON_TYPE;
        assertEquals(MediaType.APPLICATION_JSON_TYPE, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
}
