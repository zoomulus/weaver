package com.zoomulus.weaver.rest.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.content.ContentType;

@Getter
@Accessors(fluent=true)
public abstract class RequestResult
{
    private final int status;
    private final String reason;
    private final String content;
    private ContentType contentType;
    
    protected static final String host = "http://localhost:22002/";
    
    public RequestResult(final String uri) throws ClientProtocolException, IOException
    {
        this(uri, null, ContentType.TEXT_PLAIN_TYPE, Maps.newHashMap());
    }
    
    public RequestResult(final String uri, final String body, final ContentType contentType) throws ClientProtocolException, IOException
    {
        this(uri, body, contentType, Maps.newHashMap());
    }
    
    public RequestResult(final String uri,
            final String body,
            final ContentType contentType,
            final Map<String, String> headers) throws ClientProtocolException, IOException
    {
        final Request req = getRequest(uri);
        for (final Entry<String, String> header : headers.entrySet())
        {
            req.addHeader(header.getKey(), header.getValue());
        }
        if (null != body)
        {
            org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.parse(contentType.toString());
            req.bodyString(body, ct);
        }
        final Response rsp = req.execute();
        final HttpResponse httpRsp = rsp.returnResponse();
        status = httpRsp.getStatusLine().getStatusCode();
        reason = httpRsp.getStatusLine().getReasonPhrase();
        HttpEntity entity = httpRsp.getEntity();
        if (null == entity)
        {
            this.contentType = null;
            content = null;
        }
        else
        {
            org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.get(entity);
            try
            {
                this.contentType = ContentType.valueOf(ct.getMimeType());
            }
            catch (IllegalArgumentException e)
            {
                this.contentType = new ContentType(ct.getMimeType());
            }
            InputStream is = httpRsp.getEntity().getContent();
            ByteBuffer buf = ByteBuffer.allocate(is.available());
            while (is.available() > 0)
            {
                buf.put((byte) is.read());
            }
            is.close();
            content = new String(buf.array());
        }
    }
    
    protected abstract Request getRequest(final String uri);
}
