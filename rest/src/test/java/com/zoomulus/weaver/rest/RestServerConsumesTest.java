package com.zoomulus.weaver.rest;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.HeadRequestResult;
import com.zoomulus.weaver.rest.testutils.OptionsRequestResult;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;

public class RestServerConsumesTest extends RestServerTestBase
{
    private final String json = "{ \"property\" : \"value\", \"array\" : [1, 2, 3], \"embedded\" : { \"ep\" : \"ev\" } }";
    private final String xml = "<xml><mynode myattr=\"myval\">text</mynode></xml>";
    private final String text = "abc123";

    @Test
    public void testFormPostWithClassLevelConsumes() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("form/post/single", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "p1=v1");
        verifyUnsupportedMediaTypeResult(new PostRequestResult("form/post/single", "p1=v1", ContentType.TEXT_PLAIN_TYPE));
    }
    
    @Test
    public void testPostDifferentContentTypes() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/xml", xml, ContentType.APPLICATION_XML_TYPE), xml);
        verifyOkResult(new PostRequestResult("post/json", json, ContentType.APPLICATION_JSON_TYPE), json);
        verifyOkResult(new PostRequestResult("post/text", text, ContentType.TEXT_PLAIN_TYPE), text);
    }
    
    @Test
    public void testConsumesOnGetReturns500() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("/get/consumes"));
    }
    
    @Test
    public void testConsumesOnHeadReturns500() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new HeadRequestResult("/head/consumes"));
    }
    
    @Test
    public void testConsumesOnOptionsReturns500() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new OptionsRequestResult("/options/consumes"));
    }
        
    
    // We are not going to support this for now.
    // The spec allows for an endpoint to accept and deliver multiple content types, but
    // also that a single resource can disambiguate between requests based on the content type
    // provided.  In our implementation that means the value for @Consumes would have to be
    // used to identify a resource in the ResourceIdentifier class.  This ruins the quicker
    // lookup Weaver is trying to achieve - and for no real purpose, because it is a minor matter
    // for the implementer to simply look at the @HeaderParam("Content-Type") and make a decision
    // based on that instead of writing two endpoints.
    @Ignore
    @Test
    public void testPostDisambiguatesOnContentType() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/bycontenttype", xml, ContentType.APPLICATION_XML_TYPE), "xml");
        verifyOkResult(new PostRequestResult("post/bycontenttype", json, ContentType.APPLICATION_JSON_TYPE), "json");
        verifyOkResult(new PostRequestResult("post/bycontenttype", text, ContentType.TEXT_PLAIN_TYPE), "text");
    }
}
