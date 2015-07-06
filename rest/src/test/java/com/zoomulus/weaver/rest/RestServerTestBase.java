package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.connector.ServerConnector;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.connector.RestServerConnector;
import com.zoomulus.weaver.rest.testutils.RequestResult;

public abstract class RestServerTestBase
{
    static RestServer server;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        final ServerConnector connector = RestServerConnector.builder()
                .withPort(22002)
                .withResource(RestServerTestResource.class)
                .withResource(RestServerTestResourceFormData.class)
                .build();
        server = new RestServer(connector);
        server.start();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        server.shutdown();
    }

    protected Map<String, String> getAcceptHeader(final String ct)
    {
        final Map<String, String> headers = Maps.newHashMap();
        headers.put("Accept", ct);
        return headers;
    }
    
    protected void verifyOkResult(final RequestResult rr, final String expectedResponse)
    {
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(expectedResponse, rr.content());
    }
    
    protected void verifyNotFoundResult(final RequestResult rr)
    {
        assertEquals(Status.NOT_FOUND.getStatusCode(), rr.status());
        assertEquals(Status.NOT_FOUND.getReasonPhrase(), rr.reason());
        assertTrue(Strings.isNullOrEmpty(rr.content()));
    }
    
    protected void verifyMethodNotAllowedResult(final RequestResult rr)
    {
        assertEquals(Status.METHOD_NOT_ALLOWED.getStatusCode(), rr.status());
        assertEquals(Status.METHOD_NOT_ALLOWED.getReasonPhrase(), rr.reason());
    }
    
    protected void verifyNotAcceptableResult(final RequestResult rr)
    {
        assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), rr.status());
        assertEquals(Status.NOT_ACCEPTABLE.getReasonPhrase(), rr.reason());
        assertTrue(Strings.isNullOrEmpty(rr.content()));
    }
    
    protected void verifyUnsupportedMediaTypeResult(final RequestResult rr)
    {
        assertEquals(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), rr.status());
        assertEquals(Status.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), rr.reason());
    }
    
    protected void verify400Result(final RequestResult rr)
    {
        assertEquals(Status.BAD_REQUEST.getStatusCode(), rr.status());
        assertEquals(Status.BAD_REQUEST.getReasonPhrase(), rr.reason());
    }
    
    protected void verifyInternalServerErrorResult(final RequestResult rr)
    {
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), rr.status());
        assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), rr.reason());
    }
    
    protected void verifyContentType(final RequestResult rr, final ContentType contentType)
    {
        assertTrue(contentType.isCompatibleWith(rr.contentType()));
    }
}
