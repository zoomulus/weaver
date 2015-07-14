package com.zoomulus.weaver.rest.jaxrs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.core.PathSegment;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.rest.jaxrs.WeaverUriInfo;
import com.zoomulus.weaver.rest.resource.ResourcePath;
import com.zoomulus.weaver.rest.resource.ResourcePath.ResourcePathParser;

public class WeaverUriInfoTest
{
    private String pattern = "/test/path";
    private String path = "/test/path";
    
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

    private Map<String, String> queryParams;
    private WeaverUriInfo sut;
    
    @Before
    public void setup()
    {
        queryParams = Maps.newHashMap();
        queryParams.put("p1", "v1");
        queryParams.put("p2", "v2");
        queryParams.put("p3", "v3");
        sut = WeaverUriInfo.create(pattern, path).get();
    }
    
    private void verifyPathParameters(final WeaverUriInfo sut, Map<String, String> expectedValues)
    {
        for (String value : expectedValues.values())
        {
            assertTrue(sut.getPathParameterValues().contains(value));
        }
        for (String key : expectedValues.keySet())
        {
            assertTrue(sut.getPathParameterKeys().contains(key));
        }
        for (Entry<String, String> e : expectedValues.entrySet())
        {
            assertTrue(sut.getPathParameterEntries().contains(e));
            assertEquals(e.getValue(), sut.getPathParameter(e.getKey()).get());
        }
    }    
    
    private void verifyMatrixParams(final WeaverUriInfo sut, final Map<String, String> expectedParams)
    {
        assertTrue(sut.hasMatrixParameters());
        
        Set<String> mpKeys = sut.getMatrixParameterKeys();
        assertEquals(expectedParams.keySet().size(), mpKeys.size());
        for (final String expectedKey : expectedParams.keySet())
        {
            assertTrue(mpKeys.contains(expectedKey));
        }
        for (final String key : mpKeys)
        {
            assertEquals(expectedParams.get(key), sut.getMatrixParameter(key).get());
        }
    }
    
    @Test
    public void testConstruct()
    {
        WeaverUriInfo.create(pattern, path);
    }
    
    @Test
    public void testConstructNullPatternThrowsException()
    {
        try
        {
            WeaverUriInfo.create(null, path);
            fail();
        }
        catch (NullPointerException e) { }
    }
    
    @Test
    public void testConstructNullPathThrowsException()
    {
        assertFalse(WeaverUriInfo.create(pattern, null).isPresent());
    }
    
    @Test
    public void testMatchLiteral()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(literalPattern, literalPattern).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderEnd()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phEnd, literalPattern).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderMid()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phEnd, literalPattern).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderBegin()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phBegin, literalPattern).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderMultiple()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phMult, literalPattern).get().getPath());
    }
    
    @Test
    public void testMatchDifferentSegmentNumbersFails()
    {
        assertFalse(WeaverUriInfo.create(phEnd, literalPattern4).isPresent());
        assertFalse(WeaverUriInfo.create(phEnd, literalPattern6).isPresent());
    }

    @Test
    public void testPlaceholderValueEnd()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phEnd, literalPattern).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("end", "five");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testPlaceholderValueMid()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phMid, literalPattern).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("mid", "three");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testPlaceholderValueBegin()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phBegin, literalPattern).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("begin", "one");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testPlaceholderValueMultiple()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, literalPattern).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("first", "two");
        values.put("second", "three");
        values.put("third", "five");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testRegexValueEnd()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxEnd, rxEndMatch1).get();
        assertEquals(rxEndMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("end", "five");
        verifyPathParameters(sut, values);
        
        sut = WeaverUriInfo.create(rxEnd, rxEndMatch2).get();
        assertEquals(rxEndMatch2, sut.getPath());
        values.clear();
        values.put("end", "frederick");
        verifyPathParameters(sut, values);
        
        assertFalse(WeaverUriInfo.create(rxEnd, rxEndNonMatch1).isPresent());
        assertFalse(WeaverUriInfo.create(rxEnd, literalPattern6).isPresent());
    }
    
    @Test
    public void testRegexValueMid()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxMid, rxMidMatch1).get();
        assertEquals(rxMidMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("mid", "three");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxMid, rxMidMatch2).get();
        assertEquals(rxMidMatch2, sut.getPath());
        values.put("mid", "jubilee");
        verifyPathParameters(sut, values);

        assertFalse(WeaverUriInfo.create(rxMid, rxMidNonMatch1).isPresent());
        assertFalse(WeaverUriInfo.create(rxMid, literalPattern6).isPresent());
    }
    
    @Test
    public void testRegexValueBegin()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxBegin, rxBeginMatch1).get();
        assertEquals(rxBeginMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("begin", "one");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxBegin, rxBeginMatch2).get();
        assertEquals(rxBeginMatch2, sut.getPath());
        values.clear();
        values.put("begin", "ole");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxBegin, rxBeginMatch3).get();
        assertEquals(rxBeginMatch3, sut.getPath());
        values.clear();
        values.put("begin", "oe");
        verifyPathParameters(sut, values);

        assertFalse(WeaverUriInfo.create(rxBegin, rxBeginNonMatch1).isPresent());
        assertFalse(WeaverUriInfo.create(rxBegin, rxBeginNonMatch2).isPresent());
        assertFalse(WeaverUriInfo.create(rxBegin, rxBeginNonMatch3).isPresent());
    }
    
    @Test
    public void testRegexValueMultiple()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxMult, rxMultMatch1).get();
        assertEquals(rxMultMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("first", "alice");
        values.put("second", "bob");
        values.put("third", "eve");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxMult, rxMultMatch2).get();
        assertEquals(rxMultMatch2, sut.getPath());
        values.clear();
        values.put("first", "ace");
        values.put("second", "robert");
        values.put("third", "evening");
        verifyPathParameters(sut, values);

        assertFalse(WeaverUriInfo.create(rxMult, rxMultNonMatch1).isPresent());
        assertFalse(WeaverUriInfo.create(rxMult, rxMultNonMatch2).isPresent());
        assertFalse(WeaverUriInfo.create(rxMult, rxMultNonMatch3).isPresent());
    }
    
    @Test
    public void testMatchesWithoutLeadingSlash()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(literalPattern, literalPattern.substring(1, literalPattern.length())).get().getPath());
    }
    
    @Test
    public void testMatchesWithMatrixParams()
    {
        assertEquals(mpBeginMatch, WeaverUriInfo.create(phBegin, mpBeginMatch).get().getPath());
    }

    @Test
    public void testRegexMatchesEndParams()
    {
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("p", "5");
        for (final String pattern : Lists.newArrayList(phEnd, rxEnd))
        {
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mpEndMatch).get();
            assertEquals(mpEndMatch, sut.getPath());
            verifyMatrixParams(sut, expectedParams);
        }
    }
    
    @Test
    public void testRegexMatchesBeginParams()
    {
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("p", "1");
        for (final String pattern : Lists.newArrayList(phBegin, rxBegin))
        {
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mpBeginMatch).get();
            assertEquals(mpBeginMatch, sut.getPath());
            verifyMatrixParams(sut, expectedParams);
        }
    }
    
    @Test
    public void testRegexMatchesMidParams()
    {
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("p", "3");
        for (final String pattern : Lists.newArrayList(phMid, rxMid))
        {
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mpMidMatch).get();
            assertEquals(mpMidMatch, sut.getPath());
            verifyMatrixParams(sut, expectedParams);
        }
    }
    
    @Test
    public void testMatchesWithMultipleMatrixParams()
    {
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("ap", "2");
        expectedParams.put("bp", "3");
        expectedParams.put("ep", "5");
        for (final String pattern : Lists.newArrayList(phMult, rxMult))
        {
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mpMultMatch).get();
            assertEquals(mpMultMatch, sut.getPath());
            verifyMatrixParams(sut, expectedParams);
        }
    }
    
    @Test
    public void testMultipleParamsInStringGlobsAllToSingle()
    {
        final String path = "/one/two/three/four/five;a=1&b=2&c=3";
        final WeaverUriInfo sut = WeaverUriInfo.create(phEnd, path).get();
        assertEquals(path, sut.getPath());
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("a", "1&b=2&c=3");
        verifyMatrixParams(sut, expectedParams);
    }
    
    @Test
    public void testPathSegment()
    {
        final String path = "/one/two/three/four/five;a=1";
        final WeaverUriInfo sut = WeaverUriInfo.create(phEnd, path).get();
        assertEquals(path, sut.getPath());
        final Optional<PathSegment> ps = sut.getPathSegment("end");
        assertTrue(ps.isPresent());
        assertEquals("five", ps.get().getPath());
        assertEquals("1", ps.get().getMatrixParameters().getFirst("a"));
    }
    
    @Test
    public void testMultiplePathSegments()
    {
        final String path = "/one/two;a=1/three;b=2;c=3;d=4/four/five;a=5;a=6";
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, path).get();
        assertEquals(path, sut.getPath());
        
        final Optional<PathSegment> ps1 = sut.getPathSegment("first");
        final Optional<PathSegment> ps2 = sut.getPathSegment("second");
        final Optional<PathSegment> ps3 = sut.getPathSegment("third");
        assertTrue(ps1.isPresent());
        assertTrue(ps2.isPresent());
        assertTrue(ps3.isPresent());
        assertEquals("two", ps1.get().getPath());
        assertEquals("1", ps1.get().getMatrixParameters().getFirst("a"));
        assertEquals("three", ps2.get().getPath());
        assertEquals("2", ps2.get().getMatrixParameters().getFirst("b"));
        assertEquals("3", ps2.get().getMatrixParameters().getFirst("c"));
        assertEquals("4", ps2.get().getMatrixParameters().getFirst("d"));
        assertEquals("five", ps3.get().getPath());
        assertEquals("5", ps3.get().getMatrixParameters().get("a").get(0));
        assertEquals("6", ps3.get().getMatrixParameters().get("a").get(1));
    }

    
    // UriInfo
    
    @Test
    public void testGetPath()
    {
        assertEquals(path, WeaverUriInfo.create(pattern, path).get().getPath());
    }
    
    @Test
    @Ignore
    public void testGetPathDecode() throws UnsupportedEncodingException
    {
        fail();
    }
    
    @Test
    public void testGetPathDecodeFalse()
    {
        assertEquals(sut.getPath(), sut.getPath(false));
    }
    
    @Test
    @Ignore
    public void testGetPathSegments()
    {
        fail();
    }
    
    @Test
    @Ignore
    public void testGetPathSegmentsDecodeTrue()
    {
        fail();
    }
    
    @Test
    @Ignore
    public void testGetPathSegmentsDecodeFalse()
    {
        fail();
    }
    
    @Test
    @Ignore
    public void testGetRequestUri()
    {
        fail();
    }
}
