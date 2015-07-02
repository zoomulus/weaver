package com.zoomulus.weaver.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.rest.testutils.DeleteRequestResult;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.HeadRequestResult;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;
import com.zoomulus.weaver.rest.testutils.PutRequestResult;

public class RestServerHttpMethodsTest extends RestServerTestBase
{
    @Test
    public void testGet() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get"), "get");
    }
    
    @Test
    public void testPost() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post"), "post");
    }
    
    @Test
    public void testPut() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PutRequestResult("put"), "put");
    }
    
    @Test
    public void testDelete() throws ClientProtocolException, IOException
    {
        verifyOkResult(new DeleteRequestResult("delete"), "delete");
    }
    
    @Test
    public void testHead() throws ClientProtocolException, IOException
    {
        verifyOkResult(new HeadRequestResult("head"), null);
    }
}
