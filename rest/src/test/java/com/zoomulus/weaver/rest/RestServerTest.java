package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zoomulus.weaver.core.connector.ServerConnector;
import com.zoomulus.weaver.rest.connector.RestServerConnector;

public class RestServerTest
{
    @Getter
    @Accessors(fluent=true)
    class RequestResult
    {
        private final int status;
        private final String reason;
        private final String content;
        public RequestResult(final String uri) throws ClientProtocolException, IOException
        {
            final Response rsp = Request.Get("http://localhost:22002/" + uri).execute();
            final HttpResponse httpRsp = rsp.returnResponse();
            status = httpRsp.getStatusLine().getStatusCode();
            reason = httpRsp.getStatusLine().getReasonPhrase();
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
    
    @Test
    public void testGet() throws ClientProtocolException, IOException
    {
        final RequestResult rr = new RequestResult("get");
        assertEquals(200, rr.status());
        assertEquals("OK", rr.reason());
        assertEquals("get", rr.content());
    }
    
    @Test
    public void testGetId() throws ClientProtocolException, IOException
    {
        final RequestResult rr = new RequestResult("get/id/id.12345.0");
        assertEquals(200, rr.status());
        assertEquals("OK", rr.reason());
        assertEquals("id:id.12345.0", rr.content());
    }
    
    @Test
    public void testGetMatchingId() throws ClientProtocolException, IOException
    {
        final RequestResult rr = new RequestResult("get/id/12345");
        assertEquals(200, rr.status());
        assertEquals("OK", rr.reason());
        assertEquals("id:12345", rr.content());
    }

    private static RestServer server;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        final ServerConnector connector = RestServerConnector.builder()
                .withPort(22002)
                .withResource(RestServerTestResource.class)
                .build();
        server = new RestServer(connector);
        server.start();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        server.shutdown();
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
}
