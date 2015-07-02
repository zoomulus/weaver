package com.zoomulus.weaver.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;

import com.zoomulus.weaver.rest.testutils.GetRequestResult;

public class RestServerPathSegmentTest extends RestServerTestBase
{
    @Test
    public void testGetPathSegment() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/pathsegment/ps1;k=v;j=x"), "pp:ps1;kval:v,jval:x");
    }
    
    @Test
    public void testGetPathSegmentNoMatrixParams() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/pathsegment/ps1"), "pp:ps1;kval:null,jval:null");
    }
    
    @Test
    public void testGetPathSegmentNoPathParams() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/pathsegment/;k=v;j=x"), "pp:;kval:v,jval:x");
    }
    
    // This is a part of the spec but to me it seems an unnecessary complication.
    // It seems more reasonable to me to not convolute the processing of the path
    // for that special case, and instead expect users to figure out how to work around it.
    // I'm going to wait until someone makes me support List<PathSegment> before
    // I add it.
    // Of course if someone else wants to add support for it, whatever. -MR
    @Ignore
    @Test
    public void testGetPathSegmentList()
    {
        
    }
}
