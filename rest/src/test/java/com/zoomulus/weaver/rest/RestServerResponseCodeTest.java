package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;
import com.zoomulus.weaver.rest.testutils.RequestResult;

public class RestServerResponseCodeTest extends RestServerTestBase
{
    @Test
    public void testGetHandlerReturns200() throws ClientProtocolException, IOException
    {
        final RequestResult rr = new GetRequestResult("get/return/normal");
        verifyOkResult(rr, "normal");
    }
    
    @Test
    public void testHandlerReturnsNullSends204() throws ClientProtocolException, IOException
    {
        final GetRequestResult rr = new GetRequestResult("get/return/null");
        assertEquals(Status.NO_CONTENT.getStatusCode(), rr.status());
        assertEquals(Status.NO_CONTENT.getReasonPhrase(), rr.reason());
    }
    
    @Test
    public void testHandlerThrowsExceptionSends500() throws ClientProtocolException, IOException
    {
        final GetRequestResult rr = new GetRequestResult("get/return/throws");
        verifyInternalServerErrorResult(rr);
    }
    
    @Test
    public void testNonmatchingHandlerSends404() throws ClientProtocolException, IOException
    {
        final GetRequestResult result = new GetRequestResult("get/return/missing");
        verifyNotFoundResult(result);
    }
    
    @Test
    public void testPostToGetResourceSends405() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("get/return/normal");
        verifyMethodNotAllowedResult(result);
    }
    
    @Test
    public void testGetWithNonmatchingAcceptSends406() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/return/applicationxml", getAcceptHeader(ContentType.APPLICATION_JSON));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testPostWithNonmatchingContentTypeSends415() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("post/formparam/single",
                "{\"type\":\"json\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testEndpointCanReturn201() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("post/return/created");
        assertEquals(Status.CREATED.getStatusCode(), result.status());
        assertEquals(Status.CREATED.getReasonPhrase(), result.reason());
    }
    
    @Test
    public void testEndpointCanReturn202() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("post/return/accepted");
        assertEquals(Status.ACCEPTED.getStatusCode(), result.status());
        assertEquals(Status.ACCEPTED.getReasonPhrase(), result.reason());
    }
    
    @Test
    public void testEndpointCanReturnAnyValidHttpStatus() throws ClientProtocolException, IOException
    {
        // No 100-level requests here, those have to do with continuation
        RequestResult result = new GetRequestResult("get/return/custom?status=205");
        assertEquals(205, result.status());
        result = new GetRequestResult("get/return/custom?status=302");
        assertEquals(302, result.status());
        result = new GetRequestResult("get/return/custom?status=409");
        assertEquals(409, result.status());
        result = new GetRequestResult("get/return/custom?status=503");
        assertEquals(503, result.status());
        result = new GetRequestResult("get/return/custom?status=600");
        assertEquals(600, result.status());
    }
}
