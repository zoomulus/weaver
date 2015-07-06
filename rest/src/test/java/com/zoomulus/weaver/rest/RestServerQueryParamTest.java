package com.zoomulus.weaver.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.rest.testutils.GetRequestResult;

public class RestServerQueryParamTest extends RestServerTestBase
{
    @Test
    public void testSingleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/single?firstname=bob"), "bob");
    }
    
    @Test
    public void testMultipleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/multiple?firstname=bob&lastname=bobson"), "bob bobson");
    }
    
    @Test
    public void testMultipleQueryParamForSameKey() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/multsamekey?name=alice&name=bob&name=eve"), "alice,bob,eve");
    }
    
    @Test
    public void testNoObjectQueryParamSendsNullValue() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/single"), "null");
    }
    
    @Test
    public void testObjectQueryParamWithRequiredParamWorks() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/requiredsingle?firstname=bob"), "bob");
    }
    
    @Test
    public void testNoObjectQueryParamWithRequiredParamReturns400() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/queryparams/requiredsingle"));
    }
    
    @Test
    public void testNoNativeQueryParamReturns400() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/queryparams/int"));
    }
    
    @Test
    public void testNonmatchingQueryParamIsIgnored() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/single?firstname=tim&lastname=timson"), "tim");
    }
    
    @Test
    public void testNonmatchingQueryParamWithStrictParamsFails() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/strictparams?name=bob&catname=killer"));
    }
}
