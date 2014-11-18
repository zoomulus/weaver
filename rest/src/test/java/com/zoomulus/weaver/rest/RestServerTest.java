package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.ws.rs.core.Response.Status;

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
        verifyOkResult(new RequestResult("get"), "get");
    }
    
    @Test
    public void testGetId() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/id/id.12345.0"), "id:id.12345.0");
    }
    
    @Test
    public void testGetIdNoIdFails() throws ClientProtocolException, IOException
    {
        verifyNotFoundResult(new RequestResult("get/id"), "/get/id", "GET");
    }
    
    @Test
    public void testGetMatchingId() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/idmatch/12345"), "id:12345");
    }
    
    @Test
    public void testGetMultipleMatches() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/multiple/abc123/789xyz"), "second:789xyz,first:abc123");
    }
    
    // Test multiple matching pathparams on same path
    // Test conversion of pathparam to all native types and to custom types via ctor or valueOf
    // Test all http methods
    // Test POST/PUT retrieves payload
    // Test proper ordering of resource selection (best match wins)
    // Bubble processing exceptions up somehow... (invalid/unclosed regexes for example)

    private static RestServer server;
    
    private void verifyOkResult(final RequestResult rr, final String expectedResponse)
    {
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(expectedResponse, rr.content());
    }
    
    private void verifyNotFoundResult(final RequestResult rr, final String path, final String method)
    {
        assertEquals(Status.NOT_FOUND.getStatusCode(), rr.status());
        assertEquals(Status.NOT_FOUND.getReasonPhrase(), rr.reason());
        assertEquals(String.format("No matching resource found for path \"%s\" and method \"%s\"", path, method), rr.content());
    }

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
