package com.zoomulus.weaver.rest.testutils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import com.zoomulus.weaver.core.content.ContentType;

public class PutRequestResult extends RequestResult
{
    public PutRequestResult(final String uri) throws ClientProtocolException, IOException
    {
        super(uri);
    }
    
    public PutRequestResult(final String uri, final String body, final ContentType contentType) throws ClientProtocolException, IOException
    {
        super(uri, body, contentType);
    }
    
    protected Request getRequest(final String uri)
    {
        return Request.Put(host + uri);
    }
}
