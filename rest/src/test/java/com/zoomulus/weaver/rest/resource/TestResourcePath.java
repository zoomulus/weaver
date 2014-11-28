package com.zoomulus.weaver.rest.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.rest.resource.ResourcePath.ResourcePathParser;

public class TestResourcePath
{
    private static String literalPattern = "/one/two/three/four/five";
    private static String literalPattern4 = "/one/two/three/four";
    private static String literalPattern6 = "/one/two/three/four/five/six";
    private static String phEnd = "/one/two/three/four/{end}";
    private static String phBegin = "/{begin}/two/three/four/five";
    private static String phMid = "/one/two/{mid}/four/five";
    private static String phMult = "/one/{first}/{second}/four/{third}";
    
    private static String rxEnd = "/one/two/three/four/{end : f.+}";
    private static String rxEndMatch1 = "/one/two/three/four/five";
    private static String rxEndMatch2 = "/one/two/three/four/frederick";
    private static String rxEndNonMatch1 = "/one/two/three/four/six";

    private static String rxMid = "/one/two/{mid: .+ee}/four/five";
    private static String rxMidMatch1 = "/one/two/three/four/five";
    private static String rxMidMatch2 = "/one/two/jubilee/four/five";
    private static String rxMidNonMatch1 = "/one/two/four/four/five";
    
    private static String rxBegin = "/{begin: ^o.?e$}/two/three/four/five";
    private static String rxBeginMatch1 = "/one/two/three/four/five";
    private static String rxBeginMatch2 = "/ole/two/three/four/five";
    private static String rxBeginMatch3 = "/oe/two/three/four/five";
    private static String rxBeginNonMatch1 = "/ones/two/three/four/five";
    private static String rxBeginNonMatch2 = "/bone/two/three/four/five";
    private static String rxBeginNonMatch3 = "/oblique/two/three/four/five";
    
    private static String rxMult = "/one/{first: a.*c.*e}/{second: [br]ob(ert)?}/four/{third: eve.*}";
    private static String rxMultMatch1 = "/one/alice/bob/four/eve";
    private static String rxMultMatch2 = "/one/ace/robert/four/evening";
    private static String rxMultNonMatch1 = "/one/aliced/bob/four/eve";
    private static String rxMultNonMatch2 = "/one/alice/bobby/four/eve";
    private static String rxMultNonMatch3 = "/one/alice/bob/four/steve";

    private static String mpBeginMatch = "/one;p=1/two/three/four/five";
    private static String mpMidMatch = "/one/two/three;p=3/four/five";
    private static String mpEndMatch = "/one/two/three/four/five;p=5";
    private static String mpMultMatch = "/one/alice;ap=2/bob;bp=3/four/eve;ep=5";
    
    @Test
    public void testConstruct()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern("").parse("");
        assertNotNull(rp);
    }
    
    @Test
    public void testConstructParser()
    {
        ResourcePathParser parser = ResourcePath.withPattern("");
        assertNotNull(parser);
    }
    
    @Test
    public void testConstructNullPatternThrowsException()
    {
        try
        {
            ResourcePath.withPattern(null);
            fail("Expected exception but none thrown");
        }
        catch (NullPointerException e) { }
    }
    
    @Test
    public void testParseNullPath()
    {
        assertFalse(ResourcePath.withPattern("").parse(null).isPresent());
    }
    
    @Test
    public void testToString()
    {
        
    }
    
    @Test
    public void testMatchLiteral()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(literalPattern).parse(literalPattern);
        assertEquals(literalPattern, rp.get().toString());
    }
    
    @Test
    public void testMatchPlaceholderEnd()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phEnd).parse(literalPattern);
        assertTrue(rp.isPresent());
    }
    
    @Test
    public void testMatchPlaceholderMid()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phMid).parse(literalPattern);
        assertTrue(rp.isPresent());
    }
    
    @Test
    public void testMatchPlaceholderBegin()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phBegin).parse(literalPattern);
        assertTrue(rp.isPresent());
    }
    
    @Test
    public void testMatchPlaceholderMultiple()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phMult).parse(literalPattern);
        assertTrue(rp.isPresent());
    }
    
    @Test
    public void testMatchDifferentSegmentNumbersFails()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phEnd).parse(literalPattern4);
        assertFalse(rp.isPresent());
        rp = ResourcePath.withPattern(phEnd).parse(literalPattern6);
        assertFalse(rp.isPresent());
    }
    
    private void verifyValues(final ResourcePath rp, Map<String, String> expectedValues)
    {
        for (String value : expectedValues.values())
        {
            assertTrue(rp.values().contains(value));
        }
        for (String key : expectedValues.keySet())
        {
            assertTrue(rp.keySet().contains(key));
        }
        for (Entry<String, String> e : expectedValues.entrySet())
        {
            assertTrue(rp.entrySet().contains(e));
            assertEquals(e.getValue(), rp.get(e.getKey()));
        }
    }
    
    @Test
    public void testPlaceholderValueEnd()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phEnd).parse(literalPattern);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("end", "five");
        verifyValues(rp.get(), values);
    }
    
    @Test
    public void testPlaceholderValueMid()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phMid).parse(literalPattern);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("mid", "three");
        verifyValues(rp.get(), values);
    }
    
    @Test
    public void testPlaceholderValueBegin()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phBegin).parse(literalPattern);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("begin", "one");
        verifyValues(rp.get(), values);
    }
    
    @Test
    public void testPlaceholderValueMultiple()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phMult).parse(literalPattern);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("first", "two");
        values.put("second", "three");
        values.put("third", "five");
        verifyValues(rp.get(), values);
    }
    
    @Test
    public void testRegexValueEnd()
    {
        final ResourcePathParser parser = ResourcePath.withPattern(rxEnd);
        Optional<ResourcePath> rp = parser.parse(rxEndMatch1);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("end", "five");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxEndMatch2);
        assertTrue(rp.isPresent());
        values.clear();
        values.put("end", "frederick");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxEndNonMatch1);
        assertFalse(rp.isPresent());
        
        rp = parser.parse(literalPattern6);
        assertFalse(rp.isPresent());
    }
    
    @Test
    public void testRegexValueMid()
    {
        final ResourcePathParser parser = ResourcePath.withPattern(rxMid);
        Optional<ResourcePath> rp = parser.parse(rxMidMatch1);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("mid", "three");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxMidMatch2);
        assertTrue(rp.isPresent());
        values.clear();
        values.put("mid", "jubilee");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxMidNonMatch1);
        assertFalse(rp.isPresent());
        
        rp = parser.parse(literalPattern6);
        assertFalse(rp.isPresent());
    }
    
    @Test
    public void testRegexValueBegin()
    {
        final ResourcePathParser parser = ResourcePath.withPattern(rxBegin);
        Optional<ResourcePath> rp = parser.parse(rxBeginMatch1);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("begin", "one");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxBeginMatch2);
        assertTrue(rp.isPresent());
        values.clear();
        values.put("begin", "ole");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxBeginMatch3);
        assertTrue(rp.isPresent());
        values.clear();
        values.put("begin", "oe");
        verifyValues(rp.get(), values);

        rp = parser.parse(rxBeginNonMatch1);
        assertFalse(rp.isPresent());
        
        rp = parser.parse(rxBeginNonMatch2);
        assertFalse(rp.isPresent());

        rp = parser.parse(rxBeginNonMatch3);
        assertFalse(rp.isPresent());

        rp = parser.parse(literalPattern6);
        assertFalse(rp.isPresent());        
    }
    
    @Test
    public void testRegexValueMultiple()
    {
        final ResourcePathParser parser = ResourcePath.withPattern(rxMult);
        Optional<ResourcePath> rp = parser.parse(rxMultMatch1);
        assertTrue(rp.isPresent());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("first", "alice");
        values.put("second", "bob");
        values.put("third", "eve");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxMultMatch2);
        values.clear();
        values.put("first", "ace");
        values.put("second", "robert");
        values.put("third", "evening");
        verifyValues(rp.get(), values);
        
        rp = parser.parse(rxMultNonMatch1);
        assertFalse(rp.isPresent());
        
        rp = parser.parse(rxMultNonMatch2);
        assertFalse(rp.isPresent());
        
        rp = parser.parse(rxMultNonMatch3);
        assertFalse(rp.isPresent());
        
    }
    
    @Test
    public void testMatchesWithoutLeadingSlash()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(literalPattern).
                parse(literalPattern.substring(1, literalPattern.length()));
        assertTrue(rp.isPresent());
    }
    
    @Test
    public void testMatchesWithMatrixParams()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phBegin)
                .parse(mpBeginMatch);
        assertTrue(rp.isPresent());
    }
    
    private void verifyMatrixParams(final ResourcePath rp, final Map<String, String> expectedParams)
    {
        assertTrue(rp.hasMatrixParams());
        
        Set<String> mpKeys = rp.matrixParamKeySet();
        assertEquals(expectedParams.keySet().size(), mpKeys.size());
        for (final String expectedKey : expectedParams.keySet())
        {
            assertTrue(mpKeys.contains(expectedKey));
        }
        for (final String key : mpKeys)
        {
            assertEquals(expectedParams.get(key), rp.matrixParamGet(key));
        }
    }
    
    @Test
    public void testRegexMatchesEndParams()
    {
        for (final String pattern : Lists.newArrayList(phEnd, rxEnd))
        {
            Optional<ResourcePath> rp = ResourcePath.withPattern(pattern)
                    .parse(mpEndMatch);
            assertTrue(rp.isPresent());
            
            Map<String, String> expectedParams = Maps.newHashMap();
            expectedParams.put("p", "5");
            verifyMatrixParams(rp.get(), expectedParams);
        }
    }
    
    @Test
    public void testRegexMatchesBeginParams()
    {
        for (final String pattern : Lists.newArrayList(phBegin, rxBegin))
        {
            Optional<ResourcePath> rp = ResourcePath.withPattern(pattern)
                    .parse(mpBeginMatch);
            assertTrue(rp.isPresent());
            
            Map<String, String> expectedParams = Maps.newHashMap();
            expectedParams.put("p", "1");
            verifyMatrixParams(rp.get(), expectedParams);
        }
    }
    
    @Test
    public void testRegexMatchesMidParams()
    {
        for (final String pattern : Lists.newArrayList(phMid, rxMid))
        {
            Optional<ResourcePath> rp = ResourcePath.withPattern(pattern)
                    .parse(mpMidMatch);
            assertTrue(rp.isPresent());
            
            Map<String, String> expectedParams = Maps.newHashMap();
            expectedParams.put("p", "3");
            verifyMatrixParams(rp.get(), expectedParams);
        }
    }
    
    @Test
    public void testMatchesWithMultipleMatrixParams()
    {
        for (final String pattern : Lists.newArrayList(phMult, rxMult))
        {
            Optional<ResourcePath> rp = ResourcePath.withPattern(pattern)
                    .parse(mpMultMatch);
            assertTrue(rp.isPresent());
            
            Map<String, String> expectedParams = Maps.newHashMap();
            expectedParams.put("ap", "2");
            expectedParams.put("bp", "3");
            expectedParams.put("ep", "5");
            verifyMatrixParams(rp.get(), expectedParams);
        }
    }
    
    @Test
    public void testMultipleParamsInStringGlobsAllToSingle()
    {
        Optional<ResourcePath> rp = ResourcePath.withPattern(phEnd).parse("/one/two/three/four/five;a=1&b=2&c=3");
        assertTrue(rp.isPresent());
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("a", "1&b=2&c=3");
        verifyMatrixParams(rp.get(), expectedParams);
    }
}
