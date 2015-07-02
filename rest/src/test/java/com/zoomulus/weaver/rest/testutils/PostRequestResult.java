package com.zoomulus.weaver.rest.testutils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import com.zoomulus.weaver.core.content.ContentType;

public class PostRequestResult extends RequestResult
{
    public PostRequestResult(final String uri) throws ClientProtocolException, IOException
    {
        super(uri);
    }
    
    public PostRequestResult(final String uri, final String body, final ContentType contentType) throws ClientProtocolException, IOException
    {
        super(uri, body, contentType);
    }
    
    protected Request getRequest(final String uri)
    {
        return Request.Post(host + uri);
    }
}
