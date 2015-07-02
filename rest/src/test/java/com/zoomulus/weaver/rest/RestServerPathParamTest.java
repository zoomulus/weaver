package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.rest.testutils.GetRequestResult;

public class RestServerPathParamTest extends RestServerTestBase
{
    @Test
    public void testGetId() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/id/id.12345.0"), "id:id.12345.0");
    }
    
    @Test
    public void testGetIdNoIdFails() throws ClientProtocolException, IOException
    {
        verifyNotFoundResult(new GetRequestResult("get/id"));
    }
    
    @Test
    public void testGetMatchingId() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/idmatch/12345"), "id:12345");
    }
    
    @Test
    public void testGetMultipleMatches() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/multiple/abc123/789xyz"), "second:789xyz,first:abc123");
    }
    
    @Test
    public void testGetRepeatedMatchesReturnsLast() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/multiple/first/123/second/456"), "id:456");
    }
    
    @Test
    public void testGetIntParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/int/4000000"), "4000000");
    }
    
    @Test
    public void testGetIntWithFloatValueFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/typematch/int/123.45"));
    }
    
    @Test
    public void testGetShortParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/short/20000"), "20000");
    }
    
    @Test
    public void testGetLongParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/long/8000000000"), "8000000000");        
    }
    
    @Test
    public void testGetFloatParam() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final GetRequestResult rr = new GetRequestResult(String.format("get/typematch/float/%f", floatVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleParam() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final GetRequestResult rr = new GetRequestResult(String.format("get/typematch/double/%f", doubleVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleWithIntValueConverts() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/double/13579"), "13579.0");
    }
    
    @Test
    public void testGetByteParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/byte/127"), "127");        
    }
    
    @Test
    public void testGetBooleanParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/boolean/true"), "true");        
        verifyOkResult(new GetRequestResult("get/typematch/boolean/false"), "false");        
        verifyOkResult(new GetRequestResult("get/typematch/boolean/True"), "true");        
        verifyOkResult(new GetRequestResult("get/typematch/boolean/FALSE"), "false");        
    }
    
    public void testGetStandardClassWithString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/Integer/5"), "5");
    }
    
    @Test
    public void testGetCustomClassWithStringConstructor() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/customwithstringctor/test"), "test");
    }
    
    @Test
    public void testGetCustomClassWithStringViaValueOf() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/customvalueofstring/tset"), "tset");
    }
    
    @Test
    public void testGetCustomClassWithoutStringConversionFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/typematch/custominvalid/test"));
    }
}
