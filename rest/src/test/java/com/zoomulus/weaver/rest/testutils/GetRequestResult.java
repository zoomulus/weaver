package com.zoomulus.weaver.rest.testutils;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import com.zoomulus.weaver.core.content.ContentType;

public class GetRequestResult extends RequestResult
{
    public GetRequestResult(final String uri) throws ClientProtocolException, IOException
    {
        super(uri);
    }
    
    public GetRequestResult(final String uri, final Map<String, String> headers) throws ClientProtocolException, IOException
    {
        super(uri, null, ContentType.TEXT_PLAIN_TYPE, headers);
    }
    
    protected Request getRequest(final String uri)
    {
        return Request.Get(host + uri);
    }
}
