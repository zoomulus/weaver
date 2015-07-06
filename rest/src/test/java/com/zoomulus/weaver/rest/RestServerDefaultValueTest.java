package com.zoomulus.weaver.rest;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;

public class RestServerDefaultValueTest extends RestServerTestBase
{
    @Test
    public void testDefaultValueNativeQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/int"), "111");
    }
    
    @Test
    public void testDefaultValueObjectQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/string"), "tim");
    }
    
    @Test
    public void testDefaultValueMultipleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/multiple"), "tim,111");
    }
    
    @Test
    public void testSomeDefaultValuesMultipleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/multiple?name=bob"), "bob,111");
    }
    
    @Test
    public void testProvidedQueryParamOverridesDefaultValue() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/string?name=bob"), "bob");
    }
    
    @Test
    public void testObjectQueryParamWithRequiredParamAndDefaultValueWorks() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/requiredanddefaultsingle?firstname=bob"), "bob");
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/requiredanddefaultsingle"), "tim");
    }    
    
    @Test
    public void testDefaultValueNativeFormParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/int", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "111");
    }
    
    @Test
    public void testDefaultValueObjectFormParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/string", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim");
    }
    
    @Test
    public void testDefaultValueMultipleFormParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/multiple", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim,111");
    }
    
    @Test
    public void testSomeDefaultValuesMultipleFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("age=222", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/multiple", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim,222");
    }
    
    @Test
    public void testProvidedFormParamOverridesDefaultValue() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("name=bob", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/string", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "bob");
    }
    
    @Test
    public void testObjectFormParamWithRequiredParamAndDefaultValueWorks() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("name=bob", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/requiredanddefaultsingle", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "bob");
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/requiredanddefaultsingle", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim");
    }    
    
    @Test
    public void testDefaultValueQueryAndFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("firstname=tim", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/queryandform?gender=male", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "tim timson,male,111");
    }
}
