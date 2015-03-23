package com.zoomulus.weaver.core.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import io.netty.util.CharsetUtil;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

public class ContentTypeTest
{
    @Test
    public void testConstruct()
    {
        final ContentType ct = new ContentType(ContentType.APPLICATION_JSON, CharsetUtil.UTF_16.name());
        assertEquals(ContentType.APPLICATION_JSON, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_16.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructWithoutEncoding()
    {
        final ContentType ct = new ContentType(ContentType.APPLICATION_JSON);
        assertEquals(ContentType.APPLICATION_JSON, ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructWithNullStringThrowsException()
    {
        try
        {
            new ContentType(null);
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
        catch (IllegalArgumentException e) { }
    }
    
    @Test
    public void testConstructWithThreePartMediaType()
    {
        final ContentType ct = new ContentType("a/b/c");
        assertEquals("a/b/c", ct.getMediaType());
    }
    
    @Test
    public void testConstructCustom()
    {
        final ContentType ct = new ContentType("application/fake");
        assertEquals("application/fake", ct.getMediaType());
        assertEquals(CharsetUtil.UTF_8.name(), ct.getEncoding());
    }
    
    @Test
    public void testConstructNullEncodingThrowsException()
    {
        try
        {
            new ContentType(ContentType.APPLICATION_JSON, null);
            fail();
        }
        catch (NullPointerException e) { }
    }
    
    @Test
    public void testToString()
    {
        assertEquals("application/json; UTF-8", ContentType.APPLICATION_JSON_TYPE.toString());
    }
    
    @Test
    public void testValueOf()
    {
        assertEquals(ContentType.TEXT_PLAIN_TYPE, ContentType.valueOf(ContentType.TEXT_PLAIN));
        assertEquals(ContentType.TEXT_PLAIN_TYPE, ContentType.valueOf(ContentType.TEXT_PLAIN_TYPE.getMediaType()));
        assertEquals(ContentType.TEXT_PLAIN_TYPE, ContentType.valueOf(ContentType.TEXT_PLAIN_TYPE.toString()));
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
        
        final Map<ContentType, String> m2 = Maps.newHashMap();
        m2.put(ContentType.APPLICATION_ATOM_XML_TYPE, ContentType.APPLICATION_ATOM_XML);
        m2.put(ContentType.APPLICATION_FORM_URLENCODED_TYPE, ContentType.APPLICATION_FORM_URLENCODED);
        m2.put(ContentType.APPLICATION_JSON_TYPE, ContentType.APPLICATION_JSON);
        m2.put(ContentType.APPLICATION_OCTET_STREAM_TYPE, ContentType.APPLICATION_OCTET_STREAM);
        m2.put(ContentType.APPLICATION_SVG_XML_TYPE, ContentType.APPLICATION_SVG_XML);
        m2.put(ContentType.APPLICATION_XHTML_XML_TYPE, ContentType.APPLICATION_XHTML_XML);
        m2.put(ContentType.APPLICATION_XML_TYPE, ContentType.APPLICATION_XML);
        m2.put(ContentType.MULTIPART_FORM_DATA_TYPE, ContentType.MULTIPART_FORM_DATA);
        m2.put(ContentType.TEXT_HTML_TYPE, ContentType.TEXT_HTML);
        m2.put(ContentType.TEXT_PLAIN_TYPE, ContentType.TEXT_PLAIN);
        m2.put(ContentType.TEXT_XML_TYPE, ContentType.TEXT_XML);
        m2.put(ContentType.WILDCARD_TYPE, ContentType.WILDCARD);
        
        for (final ContentType ct : m2.keySet())
        {
            assertEquals(ct.getMediaType(), m2.get(ct));
        }
    }
    
    @Test
    public void testEquals()
    {
        assertEquals(ContentType.APPLICATION_JSON_TYPE, ContentType.APPLICATION_JSON_TYPE);
        
        final ContentType ct1 = new ContentType(ContentType.APPLICATION_JSON);
        final ContentType ct2 = new ContentType(ContentType.APPLICATION_JSON);
        assertEquals(ct1, ct2);
        assertEquals(ContentType.APPLICATION_JSON_TYPE, ct1);
        
        final ContentType ct3 = new ContentType(ContentType.APPLICATION_ATOM_XML);
        assertNotEquals(ct2, ct3);
        final ContentType ct4 = new ContentType(ContentType.APPLICATION_ATOM_XML, CharsetUtil.UTF_16.name());
        assertNotEquals(ct3, ct4);
        final ContentType ct5 = new ContentType(ContentType.APPLICATION_ATOM_XML, CharsetUtil.UTF_16.name());
        assertEquals(ct4, ct5);
        
        final ContentType ct6 = new ContentType(ContentType.APPLICATION_JSON, CharsetUtil.UTF_16.name());
        assertNotEquals(ct5, ct6);
        
        final ContentType ct7 = new ContentType(new String("application/json"));
        assertEquals(ct1, ct7);
    }
    
    @Test
    public void testIsCompatible()
    {
        assertTrue(ContentType.APPLICATION_JSON_TYPE.isCompatibleWith(ContentType.APPLICATION_JSON_TYPE));
        assertFalse(ContentType.APPLICATION_JSON_TYPE.isCompatibleWith(ContentType.APPLICATION_XML_TYPE));
        
        final ContentType ct1 = new ContentType(ContentType.APPLICATION_JSON);
        assertTrue(ct1.isCompatibleWith(ContentType.APPLICATION_JSON_TYPE));
        assertTrue(ContentType.APPLICATION_JSON_TYPE.isCompatibleWith(ct1));
        assertFalse(ct1.isCompatibleWith(ContentType.APPLICATION_ATOM_XML_TYPE));
        assertFalse(ContentType.APPLICATION_ATOM_XML_TYPE.isCompatibleWith(ct1));
        
        final ContentType ct2 = new ContentType(ContentType.APPLICATION_JSON);
        assertTrue(ct1.isCompatibleWith(ct2));
        assertTrue(ct2.isCompatibleWith(ct1));
        
        final ContentType ct3 = new ContentType(ContentType.APPLICATION_ATOM_XML);
        assertFalse(ct1.isCompatibleWith(ct3));
        assertFalse(ct3.isCompatibleWith(ct1));
        
        final ContentType ct4 = new ContentType(ContentType.APPLICATION_JSON, CharsetUtil.UTF_16.name());
        assertTrue(ct1.isCompatibleWith(ct4));
        assertTrue(ct4.isCompatibleWith(ct1));
    }
}
