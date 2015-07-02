package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.rest.testutils.GetRequestResult;

public class RestServerMatrixParamTest extends RestServerTestBase
{
    @Test
    public void testGetMatrixParamSingle() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/single/12345;name=bob"), "id:12345,name:bob");
    }
    
    @Test
    public void testGetMatrixParamMultiple() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/multiple/first;1=one/second;two=2"), "p1:first,n:one;p2:second,n:2");
    }
    
    @Test
    public void testGetMatrixParamRepeatedMatchesReturnsLast() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/multiple/rep/rep1;var=alice/rep2;var=bob"), "var:bob");
    }
    
    @Test
    public void testGetMatrixParamDoesntParseMultipleParamsInSingleStatement() throws ClientProtocolException, IOException
    {
        // This is how MatrixParam should behave; if you want to split the whole string
        // you have to use PathSegment and do it yourself, you big baby
        verifyOkResult(new GetRequestResult("get/matrix/single/12345;name=bob&age=30&home=Nowhere"), "id:12345,name:bob&age=30&home=Nowhere");
    }
    
    @Test
    public void testGetIntMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/int/x;var=4000000"), "4000000");
    }
    
    @Test
    public void testGetIntMatrixParamWithFloatValueFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/matrix/typematch/int/x;var=123.45"));
    }
    
    @Test
    public void testGetShortMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/short/x;var=20000"), "20000");
    }
    
    @Test
    public void testGetLongMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/long/x;var=8000000000"), "8000000000");        
    }
    
    @Test
    public void testGetFloatMatrixParam() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final GetRequestResult rr = new GetRequestResult(String.format("get/matrix/typematch/float/x;var=%f", floatVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleMatrixParam() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final GetRequestResult rr = new GetRequestResult(String.format("get/matrix/typematch/double/x;var=%f", doubleVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleMatrixParamWithIntValueConverts() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/double/x;var=13579"), "13579.0");
    }
    
    @Test
    public void testGetByteMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/byte/x;var=127"), "127");        
    }
    
    @Test
    public void testGetBooleanMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=true"), "true");        
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=false"), "false");        
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=True"), "true");        
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=FALSE"), "false");        
    }
    
    public void testGetStandardClassMatrixParamWithString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/Integer/x;var=5"), "5");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithStringConstructor() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/customwithstringctor/x;var=test"), "test");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithStringViaValueOf() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/customvalueofstring/x;var=tset"), "tset");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithoutStringConversionFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/matrix/typematch/custominvalid/x;var=test"));
    }
}
