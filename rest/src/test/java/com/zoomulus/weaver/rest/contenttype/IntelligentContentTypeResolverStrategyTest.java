package com.zoomulus.weaver.rest.contenttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IntelligentContentTypeResolverStrategyTest
{
    private ContentTypeResolverStrategy sut;
    
    @Before
    public void before()
    {
        sut = new IntelligentContentTypeResolverStrategy();
    }
    
    @After
    public void after()
    {
        sut = null;
    }
    
    @Test
    public void testChooseSingleContentType()
    {
        final Optional<MediaType> ct = sut.resolve(Lists.newArrayList(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE),
                    Lists.newArrayList(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE), null);
        assertTrue(ct.isPresent());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, ct.get());
    }
    
    @Test
    public void testNoMatchingContentType()
    {
        final Optional<MediaType> ct = sut.resolve(Lists.newArrayList(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE),
                    Lists.newArrayList(MediaType.TEXT_HTML_TYPE, MediaType.TEXT_PLAIN_TYPE), null);
        assertFalse(ct.isPresent());
    }
    
    @Test
    public void testSingleOptionMatches()
    {
        final Optional<MediaType> ct = sut.resolve(Lists.newArrayList(MediaType.TEXT_PLAIN_TYPE),
                Lists.newArrayList(MediaType.TEXT_PLAIN_TYPE), null);
        assertTrue(ct.isPresent());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, ct.get());
    }
    
    @Test
    public void testNoOptionsReturnsNoMatch()
    {
        final Optional<MediaType> ct = sut.resolve(Lists.newArrayList(), Lists.newArrayList(), null);
        assertFalse(ct.isPresent());
    }
    
    @Test
    public void testMultipleMatchesReturnsFirst()
    {
        final Optional<MediaType> ct = sut.resolve(Lists.newArrayList(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE),
                Lists.newArrayList(MediaType.TEXT_HTML_TYPE, MediaType.TEXT_PLAIN_TYPE), null);
        assertTrue(ct.isPresent());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, ct.get());
    }
}
