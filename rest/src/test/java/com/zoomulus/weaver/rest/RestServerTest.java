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
import org.junit.Ignore;
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
    
    @Test
    public void testGetRepeatedMatchesReturnsLast() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/multiple/first/123/second/456"), "id:456");
    }
    
    @Test
    public void testGetIntParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/int/4000000"), "4000000");
    }
    
    @Test
    public void testGetIntWithFloatValueFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new RequestResult("get/typematch/int/123.45"));
    }
    
    @Test
    public void testGetShortParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/short/20000"), "20000");
    }
    
    @Test
    public void testGetLongParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/long/8000000000"), "8000000000");        
    }
    
    @Test
    public void testGetFloatParam() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final RequestResult rr = new RequestResult(String.format("get/typematch/float/%f", floatVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleParam() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final RequestResult rr = new RequestResult(String.format("get/typematch/double/%f", doubleVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleWithIntValueConverts() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/double/13579"), "13579.0");
    }
    
    @Test
    public void testGetByteParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/byte/127"), "127");        
    }
    
    @Test
    public void testGetBooleanParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/boolean/true"), "true");        
        verifyOkResult(new RequestResult("get/typematch/boolean/false"), "false");        
        verifyOkResult(new RequestResult("get/typematch/boolean/True"), "true");        
        verifyOkResult(new RequestResult("get/typematch/boolean/FALSE"), "false");        
    }
    
    public void testGetStandardClassWithString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/Integer/5"), "5");
    }
    
    @Test
    public void testGetCustomClassWithStringConstructor() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/customwithstringctor/test"), "test");
    }
    
    @Test
    public void testGetCustomClassWithStringViaValueOf() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/typematch/customvalueofstring/tset"), "tset");
    }
    
    @Test
    public void testGetCustomClassWithoutStringConversionFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new RequestResult("get/typematch/custominvalid/test"));
    }
    
    // Probably do PathSegment here
    
    @Test
    public void testGetMatrixParamSingle() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/single/12345;name=bob"), "id:12345,name:bob");
    }
    
    @Test
    public void testGetMatrixParamMultiple() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/multiple/first;1=one/second;two=2"), "p1:first,n:one;p2:second,n:2");
    }
    
    @Test
    public void testGetMatrixParamRepeatedMatchesReturnsLast() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/multiple/rep/rep1;var=alice/rep2;var=bob"), "var:bob");
    }
    
    @Test
    public void testGetMatrixParamDoesntParseMultipleParamsInSingleStatement() throws ClientProtocolException, IOException
    {
        // This is how MatrixParam should behave; if you want to split the whole string
        // you have to use PathSegment and do it yourself, you big baby
        verifyOkResult(new RequestResult("get/matrix/single/12345;name=bob&age=30&home=Nowhere"), "id:12345,name:bob&age=30&home=Nowhere");
    }
    
    @Test
    public void testGetIntMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/int/x;var=4000000"), "4000000");
    }
    
    @Test
    public void testGetIntMatrixParamWithFloatValueFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new RequestResult("get/matrix/typematch/int/x;var=123.45"));
    }
    
    @Test
    public void testGetShortMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/short/x;var=20000"), "20000");
    }
    
    @Test
    public void testGetLongMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/long/x;var=8000000000"), "8000000000");        
    }
    
    @Test
    public void testGetFloatMatrixParam() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final RequestResult rr = new RequestResult(String.format("get/matrix/typematch/float/x;var=%f", floatVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleMatrixParam() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final RequestResult rr = new RequestResult(String.format("get/matrix/typematch/double/x;var=%f", doubleVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleMatrixParamWithIntValueConverts() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/double/x;var=13579"), "13579.0");
    }
    
    @Test
    public void testGetByteMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/byte/x;var=127"), "127");        
    }
    
    @Test
    public void testGetBooleanMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/boolean/x;var=true"), "true");        
        verifyOkResult(new RequestResult("get/matrix/typematch/boolean/x;var=false"), "false");        
        verifyOkResult(new RequestResult("get/matrix/typematch/boolean/x;var=True"), "true");        
        verifyOkResult(new RequestResult("get/matrix/typematch/boolean/x;var=FALSE"), "false");        
    }
    
    public void testGetStandardClassMatrixParamWithString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/Integer/x;var=5"), "5");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithStringConstructor() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/customwithstringctor/x;var=test"), "test");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithStringViaValueOf() throws ClientProtocolException, IOException
    {
        verifyOkResult(new RequestResult("get/matrix/typematch/customvalueofstring/x;var=tset"), "tset");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithoutStringConversionFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new RequestResult("get/matrix/typematch/custominvalid/x;var=test"));
    }
    
    // Test conversion of outputs from native types, string, JSON-serializable classes to Response
    // Test handling of null outputs as 204 NO CONTENT
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
    
    private void verifyInternalServerErrorResult(final RequestResult rr)
    {
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), rr.status());
        assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), rr.reason());
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
