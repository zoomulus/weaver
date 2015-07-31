package com.zoomulus.weaver.rest.jaxrs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
    private final String hostname = "host.domain.tld";
    
    private HttpRequest mockRequest(final String path)
    {
        final HttpRequest request = mock(HttpRequest.class);
        final HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getUri()).thenReturn(path);
        when(request.headers()).thenReturn(headers);
        when(headers.get("Host")).thenReturn(hostname);
        return request;
    }
    
    private HttpRequest mockRequest(final String path, final String queryString)
    {
        return mockRequest(String.format("%s?%s", path, queryString));
    }
    
    private URI makeUri(final String path) throws URISyntaxException
    {
        return makeUri(path, null);
    }
    
    private URI makeUri(final String path, final String queryString) throws URISyntaxException
    {
        if (null != queryString)
        {
            return new URI(String.format("http://%s%s?%s", hostname, path, queryString));
        }
        return new URI(String.format("http://%s%s", hostname, path));
    }
    
    @Before
    public void setup()
    {
        queryParams = Maps.newHashMap();
        queryParams.put("p1", "v1");
        queryParams.put("p2", "v2");
        queryParams.put("p3", "v3");
    }
    
    private void verifyPathParameters(final WeaverUriInfo sut, Map<String, String> expectedValues)
    {
        final Map<String, List<String>> expectedValuesAsList = Maps.newHashMap();
        for (final Entry<String, String> e : expectedValues.entrySet())
        {
            expectedValuesAsList.put(e.getKey(), Lists.newArrayList(e.getValue()));
        }
        verifyPathParametersAsList(sut, expectedValuesAsList);
    }
    
    private void verifyPathParametersAsList(final WeaverUriInfo sut, Map<String, List<String>> expectedValues)
    {
        for (List<String> value : expectedValues.values())
        {
            assertTrue(sut.getPathParameterValues().contains(value));
        }
        for (String key : expectedValues.keySet())
        {
            assertTrue(sut.getPathParameterKeys().contains(key));
        }
        for (Entry<String, List<String>> e : expectedValues.entrySet())
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
        WeaverUriInfo.create(pattern, mockRequest(path));
    }
    
    @Test
    public void testConstructNullPatternThrowsException()
    {
        try
        {
            WeaverUriInfo.create(null, mockRequest(path));
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
        assertEquals(literalPattern, WeaverUriInfo.create(literalPattern, mockRequest(literalPattern)).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderEnd()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phEnd, mockRequest(literalPattern)).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderMid()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phEnd, mockRequest(literalPattern)).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderBegin()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phBegin, mockRequest(literalPattern)).get().getPath());
    }
    
    @Test
    public void testMatchPlaceholderMultiple()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(phMult, mockRequest(literalPattern)).get().getPath());
    }
    
    @Test
    public void testMatchDifferentSegmentNumbersFails()
    {
        assertFalse(WeaverUriInfo.create(phEnd, mockRequest(literalPattern4)).isPresent());
        assertFalse(WeaverUriInfo.create(phEnd, mockRequest(literalPattern6)).isPresent());
    }

    @Test
    public void testPlaceholderValueEnd()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phEnd, mockRequest(literalPattern)).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("end", "five");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testPlaceholderValueMid()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phMid, mockRequest(literalPattern)).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("mid", "three");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testPlaceholderValueBegin()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phBegin, mockRequest(literalPattern)).get();
        assertEquals(literalPattern, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("begin", "one");
        verifyPathParameters(sut, values);
    }
    
    @Test
    public void testPlaceholderValueMultiple()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(literalPattern)).get();
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
        WeaverUriInfo sut = WeaverUriInfo.create(rxEnd, mockRequest(rxEndMatch1)).get();
        assertEquals(rxEndMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("end", "five");
        verifyPathParameters(sut, values);
        
        sut = WeaverUriInfo.create(rxEnd, mockRequest(rxEndMatch2)).get();
        assertEquals(rxEndMatch2, sut.getPath());
        values.clear();
        values.put("end", "frederick");
        verifyPathParameters(sut, values);
        
        assertFalse(WeaverUriInfo.create(rxEnd, mockRequest(rxEndNonMatch1)).isPresent());
        assertFalse(WeaverUriInfo.create(rxEnd, mockRequest(literalPattern6)).isPresent());
    }
    
    @Test
    public void testRegexValueMid()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxMid, mockRequest(rxMidMatch1)).get();
        assertEquals(rxMidMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("mid", "three");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxMid, mockRequest(rxMidMatch2)).get();
        assertEquals(rxMidMatch2, sut.getPath());
        values.put("mid", "jubilee");
        verifyPathParameters(sut, values);

        assertFalse(WeaverUriInfo.create(rxMid, mockRequest(rxMidNonMatch1)).isPresent());
        assertFalse(WeaverUriInfo.create(rxMid, mockRequest(literalPattern6)).isPresent());
    }
    
    @Test
    public void testRegexValueBegin()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxBegin, mockRequest(rxBeginMatch1)).get();
        assertEquals(rxBeginMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("begin", "one");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxBegin, mockRequest(rxBeginMatch2)).get();
        assertEquals(rxBeginMatch2, sut.getPath());
        values.clear();
        values.put("begin", "ole");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxBegin, mockRequest(rxBeginMatch3)).get();
        assertEquals(rxBeginMatch3, sut.getPath());
        values.clear();
        values.put("begin", "oe");
        verifyPathParameters(sut, values);

        assertFalse(WeaverUriInfo.create(rxBegin, mockRequest(rxBeginNonMatch1)).isPresent());
        assertFalse(WeaverUriInfo.create(rxBegin, mockRequest(rxBeginNonMatch2)).isPresent());
        assertFalse(WeaverUriInfo.create(rxBegin, mockRequest(rxBeginNonMatch3)).isPresent());
    }
    
    @Test
    public void testRegexValueMultiple()
    {
        WeaverUriInfo sut = WeaverUriInfo.create(rxMult, mockRequest(rxMultMatch1)).get();
        assertEquals(rxMultMatch1, sut.getPath());
        
        Map<String, String> values = Maps.newHashMap();
        values.put("first", "alice");
        values.put("second", "bob");
        values.put("third", "eve");
        verifyPathParameters(sut, values);

        sut = WeaverUriInfo.create(rxMult, mockRequest(rxMultMatch2)).get();
        assertEquals(rxMultMatch2, sut.getPath());
        values.clear();
        values.put("first", "ace");
        values.put("second", "robert");
        values.put("third", "evening");
        verifyPathParameters(sut, values);

        assertFalse(WeaverUriInfo.create(rxMult, mockRequest(rxMultNonMatch1)).isPresent());
        assertFalse(WeaverUriInfo.create(rxMult, mockRequest(rxMultNonMatch2)).isPresent());
        assertFalse(WeaverUriInfo.create(rxMult, mockRequest(rxMultNonMatch3)).isPresent());
    }
    
    @Test
    public void testMatchesWithoutLeadingSlash()
    {
        assertEquals(literalPattern, WeaverUriInfo.create(literalPattern, mockRequest(literalPattern.substring(1, literalPattern.length()))).get().getPath());
    }
    
    @Test
    public void testMatchesWithMatrixParams()
    {
        assertEquals(mpBeginMatch, WeaverUriInfo.create(phBegin, mockRequest(mpBeginMatch)).get().getPath());
    }

    @Test
    public void testRegexMatchesEndParams()
    {
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("p", "5");
        for (final String pattern : Lists.newArrayList(phEnd, rxEnd))
        {
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mockRequest(mpEndMatch)).get();
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
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mockRequest(mpBeginMatch)).get();
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
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mockRequest(mpMidMatch)).get();
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
            final WeaverUriInfo sut = WeaverUriInfo.create(pattern, mockRequest(mpMultMatch)).get();
            assertEquals(mpMultMatch, sut.getPath());
            verifyMatrixParams(sut, expectedParams);
        }
    }
    
    @Test
    public void testMultipleParamsInStringGlobsAllToSingle()
    {
        final String path = "/one/two/three/four/five;a=1&b=2&c=3";
        final WeaverUriInfo sut = WeaverUriInfo.create(phEnd, mockRequest(path)).get();
        assertEquals(path, sut.getPath());
        Map<String, String> expectedParams = Maps.newHashMap();
        expectedParams.put("a", "1&b=2&c=3");
        verifyMatrixParams(sut, expectedParams);
    }
    
    @Test
    public void testPathSegment()
    {
        final String path = "/one/two/three/four/five;a=1";
        final WeaverUriInfo sut = WeaverUriInfo.create(phEnd, mockRequest(path)).get();
        assertEquals(path, sut.getPath());
        final Optional<PathSegment> ps = sut.getPathSegment("end");
        assertTrue(ps.isPresent());
        assertEquals("five", ps.get().getPath());
        assertEquals("1", ps.get().getMatrixParameters().getFirst("a"));
        
        assertEquals(5, sut.getPathSegments().size());
        assertEquals("five", sut.getPathSegments().get(4).getPath());
    }
    
    @Test
    public void testMultiplePathSegments()
    {
        final String path = "/one/two;a=1/three;b=2;c=3;d=4/four/five;a=5;a=6";
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(path)).get();
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
        
        assertEquals(5, sut.getPathSegments().size());
        Set<PathSegment> foundPathSegments = Sets.newHashSet();
        for (final PathSegment pathSegment : sut.getPathSegments())
        {
            if ("two".equals(pathSegment.getPath()) ||
                    "three".equals(pathSegment.getPath()) ||
                    "five".equals(pathSegment.getPath()))
            {
                foundPathSegments.add(pathSegment);
            }
        }
        assertEquals(3, foundPathSegments.size());
    }

    
    // UriInfo
    
    @Test
    public void testGetPath()
    {
        assertEquals(path, WeaverUriInfo.create(pattern, mockRequest(path)).get().getPath());
    }
    
    @Test
    public void testGetPathDecodeTrue() throws UnsupportedEncodingException
    {
        final String path = UriBuilder.fromPath("/{1}/").build("a#b").getRawPath();
        assertEquals("/a#b/", WeaverUriInfo.create("/{one}/", mockRequest(path)).get().getPath(true));
    }
    
    @Test
    public void testGetPathDecodeFalse()
    {
        final String path = UriBuilder.fromPath("/{1}/").build("a#b").getRawPath();
        assertEquals("/a%23b/", WeaverUriInfo.create("/{one}/", mockRequest(path)).get().getPath(false));
    }
    
    @Test
    public void testGetPathSegmentsDecodeTrue()
    {
        final String path = UriBuilder.fromPath("/one/{1}/{2}/four/{3}").build("al!ce;ap=2","b*b;bp=3","e#e;ep=5").getRawPath();
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(path)).get();
        final List<PathSegment> pathSegments = sut.getPathSegments();
        assertEquals(5, pathSegments.size());
        final Set<String> foundPathSegments = Sets.newHashSet();
        final Set<String> foundMatrixParams = Sets.newHashSet();
        for (final PathSegment ps : pathSegments)
        {
            final String psPath = ps.getPath();
            if ("al!ce".equals(psPath) || "b*b".equals(psPath) || "e#e".equals(psPath))
            {
                foundPathSegments.add(psPath);
                final String key = String.format("%sp", psPath.substring(0, 1));
                foundMatrixParams.add(ps.getMatrixParameters().getFirst(key));
            }
            else if ("one".equals(psPath) || "four".equals(psPath))
            {
                foundPathSegments.add(psPath);
            }
        }
        assertEquals(5, foundPathSegments.size());
        assertEquals(3, foundMatrixParams.size());
        assertTrue(foundMatrixParams.contains("2"));
        assertTrue(foundMatrixParams.contains("3"));
        assertTrue(foundMatrixParams.contains("5"));
    }
    
    @Test
    public void testGetPathSegmentsDecodeFalse()
    {
        final String path = UriBuilder.fromPath("/one/{1}/{2}/four/{3}").build("al!ce;ap=2","b*b;bp=3","e#e;ep=5").getRawPath();
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(path)).get();
        final List<PathSegment> pathSegments = sut.getPathSegments(false);
        assertEquals(5, pathSegments.size());
        final Set<String> foundPathSegments = Sets.newHashSet();
        final Set<String> foundMatrixParams = Sets.newHashSet();
        for (final PathSegment ps : pathSegments)
        {
            final String psPath = ps.getPath();
            if ("al!ce".equals(psPath) || "b*b".equals(psPath) || "e%23e".equals(psPath))
            {
                foundPathSegments.add(psPath);
                final String key = String.format("%sp", psPath.substring(0, 1));
                foundMatrixParams.add(ps.getMatrixParameters().getFirst(key));
            }
            else if ("one".equals(psPath) || "four".equals(psPath))
            {
                foundPathSegments.add(psPath);
            }
        }
        assertEquals(5, foundPathSegments.size());
        assertEquals(3, foundMatrixParams.size());
        assertTrue(foundMatrixParams.contains("2"));
        assertTrue(foundMatrixParams.contains("3"));
        assertTrue(foundMatrixParams.contains("5"));
    }
    
    @Test
    public void testGetRequestUri() throws URISyntaxException
    {
        final String queryString = "g=6&h=7";
        assertEquals(makeUri(mpMultMatch, queryString),
                WeaverUriInfo.create(phMult, mockRequest(mpMultMatch, queryString)).get().getRequestUri());
    }
    
    @Test
    public void testGetRequestUriBuilder() throws IllegalArgumentException, UriBuilderException, URISyntaxException
    {
        final String queryString = "g=6&h=7";
        assertEquals(makeUri(mpMultMatch, queryString),
                WeaverUriInfo.create(phMult, mockRequest(mpMultMatch, queryString)).get().getRequestUriBuilder().build());
    }
    
    @Test
    public void testGetAbsolutePath() throws URISyntaxException
    {
        assertEquals(makeUri(mpMultMatch),
                WeaverUriInfo.create(phMult, mockRequest(mpMultMatch, "g=6&h=7")).get().getAbsolutePath());
    }
    
    @Test
    public void testGetAbsolutePathBuilder() throws URISyntaxException
    {
        assertEquals(makeUri(mpMultMatch),
                WeaverUriInfo.create(phMult, mockRequest(mpMultMatch, "g=6&h=7")).get().getAbsolutePathBuilder().build());
    }
    
    @Test
    public void testGetBaseUri() throws URISyntaxException
    {
        assertEquals(makeUri("/"), WeaverUriInfo.create(literalPattern, mockRequest(literalPattern)).get().getBaseUri());
    }
    
    @Test
    public void testGetBaseUriBuilder() throws IllegalArgumentException, UriBuilderException, URISyntaxException
    {
        assertEquals(makeUri("/"), WeaverUriInfo.create(literalPattern, mockRequest(literalPattern)).get().getBaseUriBuilder().build());
    }
    
    @Test
    public void testGetPathParameters()
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(literalPattern)).get();
        final MultivaluedMap<String, String> pathParameters = sut.getPathParameters();
        assertEquals(5, pathParameters.size());
        final List<String> expectedParameterNames = Lists.newArrayList("one", "first", "second", "four", "third");
        final List<String> expectedParameterValues = Lists.newArrayList("one", "two", "three", "four", "five");
        for (int i=0; i<5; i++)
        {
            final String expectedName = expectedParameterNames.get(i);
            final String expectedValue = expectedParameterValues.get(i);
            final List<String> paramValue = pathParameters.get(expectedName);
            assertEquals(expectedValue, paramValue.get(0));
            assertEquals(1, paramValue.size());
        }
    }
    
    @Test
    public void testGetPathParametersDecodeTrue()
    {
        final String path = UriBuilder.fromPath("/one/{1}/{2}/four/{3}").build("al!ce;ap=2","b*b;bp=3","e#e;ep=5").getRawPath();
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(path)).get();
        final MultivaluedMap<String, String> pathParameters = sut.getPathParameters(true);
        assertEquals(5, pathParameters.size());
        final List<String> expectedParameterNames = Lists.newArrayList("one", "first", "second", "four", "third");
        final List<String> expectedParameterValues = Lists.newArrayList("one", "al!ce", "b*b", "four", "e#e");
        for (int i=0; i<5; i++)
        {
            final String expectedName = expectedParameterNames.get(i);
            final String expectedValue = expectedParameterValues.get(i);
            final List<String> paramValue = pathParameters.get(expectedName);
            assertEquals(expectedValue, paramValue.get(0));
            assertEquals(1, paramValue.size());
        }
    }

    @Test
    public void testGetPathParametersDecodeFalse()
    {
        final String path = UriBuilder.fromPath("/one/{1}/{2}/four/{3}").build("al!ce;ap=2","b*b;bp=3","e#e;ep=5").getRawPath();
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(path)).get();
        final MultivaluedMap<String, String> pathParameters = sut.getPathParameters(false);
        assertEquals(5, pathParameters.size());
        final List<String> expectedParameterNames = Lists.newArrayList("one", "first", "second", "four", "third");
        final List<String> expectedParameterValues = Lists.newArrayList("one", "al!ce", "b*b", "four", "e%23e");
        for (int i=0; i<5; i++)
        {
            final String expectedName = expectedParameterNames.get(i);
            final String expectedValue = expectedParameterValues.get(i);
            final List<String> paramValue = pathParameters.get(expectedName);
            assertEquals(expectedValue, paramValue.get(0));
            assertEquals(1, paramValue.size());
        }
    }
    
    @Test
    public void testGetQueryParameters()
    {
        final String queryString = "f=6&g=7";
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(literalPattern, queryString)).get();
        final MultivaluedMap<String, String> queryParameters = sut.getQueryParameters();
        assertEquals(2, queryParameters.size());
        final List<String> expectedParameterNames = Lists.newArrayList("f", "g");
        final List<String> expectedParameterValues = Lists.newArrayList("6", "7");
        for (int i=0; i<2; i++)
        {
            final String expectedName = expectedParameterNames.get(i);
            final String expectedValue = expectedParameterValues.get(i);
            final List<String> paramValue = queryParameters.get(expectedName);
            assertEquals(expectedValue, paramValue.get(0));
            assertEquals(1, paramValue.size());
        }
    }
    
    @Test
    public void testGetQueryParametersDecodeTrue()
    {
        final String queryString = "f=al!ce&g=e%23e";
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(literalPattern, queryString)).get();
        final MultivaluedMap<String, String> queryParameters = sut.getQueryParameters(true);
        assertEquals(2, queryParameters.size());
        final List<String> expectedParameterNames = Lists.newArrayList("f", "g");
        final List<String> expectedParameterValues = Lists.newArrayList("al!ce", "e#e");
        for (int i=0; i<2; i++)
        {
            final String expectedName = expectedParameterNames.get(i);
            final String expectedValue = expectedParameterValues.get(i);
            final List<String> paramValue = queryParameters.get(expectedName);
            assertEquals(expectedValue, paramValue.get(0));
            assertEquals(1, paramValue.size());
        }
    }
    
    @Test
    public void testGetQueryParametersDecodeFalse()
    {
        final String queryString = "f=al!ce&g=e%23e";
        final WeaverUriInfo sut = WeaverUriInfo.create(phMult, mockRequest(literalPattern, queryString)).get();
        final MultivaluedMap<String, String> queryParameters = sut.getQueryParameters(false);
        assertEquals(2, queryParameters.size());
        final List<String> expectedParameterNames = Lists.newArrayList("f", "g");
        final List<String> expectedParameterValues = Lists.newArrayList("al!ce", "e%23e");
        for (int i=0; i<2; i++)
        {
            final String expectedName = expectedParameterNames.get(i);
            final String expectedValue = expectedParameterValues.get(i);
            final List<String> paramValue = queryParameters.get(expectedName);
            assertEquals(expectedValue, paramValue.get(0));
            assertEquals(1, paramValue.size());
        }
    }
    
    @Test
    @Ignore
    public void testGetMatchedUris()
    {
        fail();
    }
    
    @Test
    @Ignore
    public void testGetMatchedUrisDecodeTrue()
    {
        fail();
    }
    
    @Test
    @Ignore
    public void testGetMatchedUrisDecodeFalse()
    {
        fail();
    }
    
    @Test
    @Ignore
    public void testGetMatchedResources()
    {
        fail();
    }
    
    @Test
    public void testResolve() throws URISyntaxException
    {
        final WeaverUriInfo sut = WeaverUriInfo.create("/", mockRequest("/")).get();
        final URI resolved = sut.resolve(makeUri(literalPattern));
        assertEquals(literalPattern, resolved.getPath());
        final WeaverUriInfo sut2 = WeaverUriInfo.create("/one/", mockRequest("/one/")).get();
        final URI resolved2 = sut2.resolve(makeUri(literalPattern));
        assertEquals(literalPattern, resolved2.getPath());
    }
    
    @Test
    public void testRelativize() throws URISyntaxException
    {
        final WeaverUriInfo sut = WeaverUriInfo.create(literalPattern, mockRequest(literalPattern)).get();
        final URI relativized = sut.relativize(makeUri("/one/two/three"));
        assertEquals("one/two/three", relativized.getPath());
    }
}
