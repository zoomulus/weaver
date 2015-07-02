package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URLEncoder;

import javax.ws.rs.core.Response.Status;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;

public class RestServerFormParamTest extends RestServerTestBase
{
    @Test
    public void testSingleFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/single", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "v1");
    }
    
    @Test
    public void testMultipleFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p3=v3&p2=v2&p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/multiple", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "v1,v2,v3");
    }
    
    @Test
    public void testQueryAndForm() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("fp2=fv2&fp3=fv3&fp1=fv1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/queryandform?qp1=qv1&qp3=qv3&qp2=qv2",
                formdata,
                ContentType.APPLICATION_FORM_URLENCODED_TYPE),
            "qp1=qv1,qp2=qv2,qp3=qv3,fp1=fv1,fp2=fv2,fp3=fv3");
    }
    
    @Test
    public void testNoObjectFormParamSendsNullValue() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p2=v2", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/single", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "null");
    }
    
    @Test
    public void testObjectFormParamWithRequiredParamWorks() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/requiredsingle", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "v1");
    }

    @Test
    public void testNoObjectFormParamWithRequiredParamReturns400() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p2=v2", CharsetUtil.UTF_8.name());
        verify400Result(new PostRequestResult("post/formparam/requiredsingle", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
    
    @Test
    public void testNoNativeFormParamReturns400() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("q=w", CharsetUtil.UTF_8.name());
        verify400Result(new PostRequestResult("post/formparam/typematch/int", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
    
    @Test
    public void testNonmatchingFormParamIsIgnored() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1&p2=v2", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/single", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "v1");
    }
    
    @Test
    public void testEmptyFormWithObjectParamSendsNull() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/formparam/single", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "null");
    }
    
    @Test
    public void testEmptyFormWithRequiredObjectParamReturns400() throws ClientProtocolException, IOException
    {
        verify400Result(new PostRequestResult("post/formparam/requiredsingle", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
    
    @Test
    public void testEmptyFormWithNativeParamReturns400() throws ClientProtocolException, IOException
    {
        verify400Result(new PostRequestResult("post/formparam/typematch/int", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
    
    @Test
    public void testFormPostBoolean() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=true", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/boolean", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "true");
    }
    
    @Test
    public void testFormPostByte() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=127", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/byte", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "127");
    }
    
    @Test
    public void testFormPostShort() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=20000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/short", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "20000");
    }
    
    @Test
    public void testFormPostInt() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=4000000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/int", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "4000000");
    }
    
    @Test
    public void testFormPostIntWithFloatValueFails() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=123.45", CharsetUtil.UTF_8.name());
        verifyInternalServerErrorResult(new PostRequestResult("post/formparam/typematch/int",
                formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
    
    @Test
    public void testFormPostFloat() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final String formdata = URLEncoder.encode(String.format("p=%f", floatVal), CharsetUtil.UTF_8.name());
        final PostRequestResult rr = new PostRequestResult("post/formparam/typematch/float", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE);
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testFormPostDouble() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final String formdata = URLEncoder.encode(String.format("p=%f", doubleVal), CharsetUtil.UTF_8.name());
        final PostRequestResult rr = new PostRequestResult("post/formparam/typematch/double", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE);
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testFormPostLong() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=8000000000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/long", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "8000000000");
    }
    
    @Test
    public void testFormPostInteger() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=4000000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/Integer", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "4000000");
    }
    
    @Test
    public void testFormPostCustomWithStringCtor() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=bob", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/customwithstringctor", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "bob");
    }
    
    @Test
    public void testFormPostCustomValueOfString() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=bill", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/customvalueofstring", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "bill");
    }
    
    @Test
    public void testFormPostCustomInvalid() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=ben", CharsetUtil.UTF_8.name());
        verifyInternalServerErrorResult(new PostRequestResult("post/formparam/typematch/custominvalid",
                formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
}
