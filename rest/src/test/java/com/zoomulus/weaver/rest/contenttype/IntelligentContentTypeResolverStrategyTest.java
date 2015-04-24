package com.zoomulus.weaver.rest.contenttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zoomulus.weaver.core.content.ContentType;

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
        final Optional<ContentType> ct = sut.resolve(Lists.newArrayList(ContentType.APPLICATION_JSON_TYPE, ContentType.TEXT_HTML_TYPE),
                    Lists.newArrayList(ContentType.APPLICATION_JSON_TYPE, ContentType.TEXT_PLAIN_TYPE));
        assertTrue(ct.isPresent());
        assertEquals(ContentType.APPLICATION_JSON_TYPE, ct.get());
    }
    
    @Test
    public void testNoMatchingContentType()
    {
        final Optional<ContentType> ct = sut.resolve(Lists.newArrayList(ContentType.APPLICATION_JSON_TYPE, ContentType.APPLICATION_XML_TYPE),
                    Lists.newArrayList(ContentType.TEXT_HTML_TYPE, ContentType.TEXT_PLAIN_TYPE));
        assertFalse(ct.isPresent());
    }
    
    @Test
    public void testSingleOptionMatches()
    {
        final Optional<ContentType> ct = sut.resolve(Lists.newArrayList(ContentType.TEXT_PLAIN_TYPE),
                Lists.newArrayList(ContentType.TEXT_PLAIN_TYPE));
        assertTrue(ct.isPresent());
        assertEquals(ContentType.TEXT_PLAIN_TYPE, ct.get());
    }
    
    @Test
    public void testNoOptionsReturnsNoMatch()
    {
        final Optional<ContentType> ct = sut.resolve(Lists.newArrayList(), Lists.newArrayList());
        assertFalse(ct.isPresent());
    }
    
    @Test
    public void testMultipleMatchesReturnsFirst()
    {
        final Optional<ContentType> ct = sut.resolve(Lists.newArrayList(ContentType.TEXT_PLAIN_TYPE, ContentType.TEXT_HTML_TYPE),
                Lists.newArrayList(ContentType.TEXT_HTML_TYPE, ContentType.TEXT_PLAIN_TYPE));
        assertTrue(ct.isPresent());
        assertEquals(ContentType.TEXT_PLAIN_TYPE, ct.get());
    }
    
    @Test
    public void testNoExpectedCTReturnsFirstProvidedCT()
    {
        final Optional<ContentType> ct = sut.resolve(Lists.newArrayList(ContentType.TEXT_PLAIN_TYPE),
                Lists.newArrayList());
        assertTrue(ct.isPresent());
        assertEquals(ContentType.TEXT_PLAIN_TYPE, ct.get());
    }
}
