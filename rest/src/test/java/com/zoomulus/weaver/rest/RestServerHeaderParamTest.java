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
    
    private static Map<String, String> getIdHeader(final String value)
    {
        final Map<String, String> headers = Maps.newHashMap();
        headers.put(headerKey, value);
        return headers;
    }
    @Test
    public void testGetIdHeader() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/header/id", getIdHeader("123")), "123");
    }
}
