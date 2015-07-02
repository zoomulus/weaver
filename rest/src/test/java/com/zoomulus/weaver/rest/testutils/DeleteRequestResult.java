package com.zoomulus.weaver.rest.testutils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

public class DeleteRequestResult extends RequestResult
{
    public DeleteRequestResult(final String uri) throws ClientProtocolException, IOException
    {
        super(uri);
    }
    
    protected Request getRequest(final String uri)
    {
        return Request.Delete(host + uri);
    }
}
