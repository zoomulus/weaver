package com.zoomulus.weaver.rest;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;

public class RestServerHeaderParamTest extends RestServerTestBase
{
    private static final String headerKey = "X-Weaver-Test-ID";
    
    private static Map<String, String> headers(final String key, final String value, final Map<String, String> headers)
    {
        headers.put(key, value);
        return headers;
    }
    
    private static Map<String, String> headers(final String key, final String value)
    {
        final Map<String, String> headers = Maps.newHashMap();
        headers.put(key, value);
        return headers;
    }
    
    private static Map<String, String> headersWithId(final String value)
    {
        return headers(headerKey, value);
    }
    
    @Test
    public void testGetIdHeader() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/header/id", headersWithId("123")), "123");
    }
    
    @Test
    public void testGetIdHeaderMissing() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/header/id", headers("X-Weaver-Test-Other", "other")));
    }
    
    @Test
    public void testGetIdHeaderNoHeaders() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/header/id"));
    }
    
    @Test
    public void testGetIdHeaderDefaultValue() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/header/id/default"), "111");
    }
    
    @Test
    public void testGetIdHeaderProvidedIgnoresDefaultValue() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/header/id/default", headersWithId("123")), "123");
    }
    
    @Test
    public void testGetMultipleHeaders() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/header/multiple", headers("Header1", "1", headers("Header2", "2", headers("Header3", "3")))), "123");
    }
    
    @Test
    public void testGetMultipleHeadersWithDefault() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/header/multiple/default", headers("Header1", "1", headers("Header2", "2", headers("Header3", "7")))), "1274");        
    }
    
    @Test
    public void testGetMultipleHeadersOneMissing() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/header/multiple", headers("Header1", "1", headers("Header2", "2"))));
    }
}
