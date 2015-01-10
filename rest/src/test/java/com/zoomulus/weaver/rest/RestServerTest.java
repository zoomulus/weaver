package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

import javax.ws.rs.core.Response.Status;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Strings;
import com.zoomulus.weaver.core.connector.ServerConnector;
import com.zoomulus.weaver.rest.connector.RestServerConnector;

public class RestServerTest
{
    @Getter
    @Accessors(fluent=true)
    abstract class RequestResult
    {
        private final int status;
        private final String reason;
        private final String content;
        
        protected static final String host = "http://localhost:22002/";
        
        public RequestResult(final String uri) throws ClientProtocolException, IOException
        {
            this(uri, null, ContentType.TEXT_PLAIN);
        }
        
        public RequestResult(final String uri, final String body, final ContentType contentType) throws ClientProtocolException, IOException
        {
            final Request req = getRequest(uri);
            if (! Strings.isNullOrEmpty(body))
            {
                req.bodyString(body, contentType);
            }
            final Response rsp = req.execute();
            final HttpResponse httpRsp = rsp.returnResponse();
            status = httpRsp.getStatusLine().getStatusCode();
            reason = httpRsp.getStatusLine().getReasonPhrase();
            if (null != httpRsp.getEntity())
            {
                InputStream is = httpRsp.getEntity().getContent();
                ByteBuffer buf = ByteBuffer.allocate(is.available());
                while (is.available() > 0)
                {
                    buf.put((byte) is.read());
                }
                is.close();
                content = new String(buf.array());
            }
            else content = null;
        }
        
        protected abstract Request getRequest(final String uri);
    }
    
    class GetRequestResult extends RequestResult
    {
        public GetRequestResult(final String uri) throws ClientProtocolException, IOException
        {
            super(uri);
        }
        
        protected Request getRequest(final String uri)
        {
            return Request.Get(host + uri);
        }
    }
    
    class PostRequestResult extends RequestResult
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
    
    class PutRequestResult extends RequestResult
    {
        public PutRequestResult(final String uri) throws ClientProtocolException, IOException
        {
            super(uri);
        }
        
        protected Request getRequest(final String uri)
        {
            return Request.Put(host + uri);
        }
    }
    
    class DeleteRequestResult extends RequestResult
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
    
    
    
    // HTTP Method Tests
    
    @Test
    public void testGet() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get"), "get");
    }
    
    @Test
    public void testPost() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post"), "post");
    }
    
    @Test
    public void testPut() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PutRequestResult("put"), "put");
    }
    
    @Test
    public void testDelete() throws ClientProtocolException, IOException
    {
        verifyOkResult(new DeleteRequestResult("delete"), "delete");
    }
    
    
    
    // PathParam tests
    
    @Test
    public void testGetId() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/id/id.12345.0"), "id:id.12345.0");
    }
    
    @Test
    public void testGetIdNoIdFails() throws ClientProtocolException, IOException
    {
        verifyNotFoundResult(new GetRequestResult("get/id"));
    }
    
    @Test
    public void testGetMatchingId() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/idmatch/12345"), "id:12345");
    }
    
    @Test
    public void testGetMultipleMatches() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/multiple/abc123/789xyz"), "second:789xyz,first:abc123");
    }
    
    @Test
    public void testGetRepeatedMatchesReturnsLast() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/multiple/first/123/second/456"), "id:456");
    }
    
    @Test
    public void testGetIntParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/int/4000000"), "4000000");
    }
    
    @Test
    public void testGetIntWithFloatValueFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/typematch/int/123.45"));
    }
    
    @Test
    public void testGetShortParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/short/20000"), "20000");
    }
    
    @Test
    public void testGetLongParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/long/8000000000"), "8000000000");        
    }
    
    @Test
    public void testGetFloatParam() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final GetRequestResult rr = new GetRequestResult(String.format("get/typematch/float/%f", floatVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleParam() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final GetRequestResult rr = new GetRequestResult(String.format("get/typematch/double/%f", doubleVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleWithIntValueConverts() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/double/13579"), "13579.0");
    }
    
    @Test
    public void testGetByteParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/byte/127"), "127");        
    }
    
    @Test
    public void testGetBooleanParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/boolean/true"), "true");        
        verifyOkResult(new GetRequestResult("get/typematch/boolean/false"), "false");        
        verifyOkResult(new GetRequestResult("get/typematch/boolean/True"), "true");        
        verifyOkResult(new GetRequestResult("get/typematch/boolean/FALSE"), "false");        
    }
    
    public void testGetStandardClassWithString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/Integer/5"), "5");
    }
    
    @Test
    public void testGetCustomClassWithStringConstructor() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/customwithstringctor/test"), "test");
    }
    
    @Test
    public void testGetCustomClassWithStringViaValueOf() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/typematch/customvalueofstring/tset"), "tset");
    }
    
    @Test
    public void testGetCustomClassWithoutStringConversionFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/typematch/custominvalid/test"));
    }
    
    
    
    // PathSegment Tests
    
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
    
    // TODO: *Possibly* support List<PathSegment>.
    //
    // This is a part of the spec but to me it seems an unnecessary complication.
    // It seems more reasonable to me to not convolute the processing of the path
    // for that special case, and instead expect users to figure out how to work around it.
    // I'm going to wait until someone makes me support List<PathSegment> before
    // I add it.
    // Of course if someone else wants to add support for it, whatever. -MR
    
    
    
    // MatrixParam Tests
    
    @Test
    public void testGetMatrixParamSingle() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/single/12345;name=bob"), "id:12345,name:bob");
    }
    
    @Test
    public void testGetMatrixParamMultiple() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/multiple/first;1=one/second;two=2"), "p1:first,n:one;p2:second,n:2");
    }
    
    @Test
    public void testGetMatrixParamRepeatedMatchesReturnsLast() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/multiple/rep/rep1;var=alice/rep2;var=bob"), "var:bob");
    }
    
    @Test
    public void testGetMatrixParamDoesntParseMultipleParamsInSingleStatement() throws ClientProtocolException, IOException
    {
        // This is how MatrixParam should behave; if you want to split the whole string
        // you have to use PathSegment and do it yourself, you big baby
        verifyOkResult(new GetRequestResult("get/matrix/single/12345;name=bob&age=30&home=Nowhere"), "id:12345,name:bob&age=30&home=Nowhere");
    }
    
    @Test
    public void testGetIntMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/int/x;var=4000000"), "4000000");
    }
    
    @Test
    public void testGetIntMatrixParamWithFloatValueFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/matrix/typematch/int/x;var=123.45"));
    }
    
    @Test
    public void testGetShortMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/short/x;var=20000"), "20000");
    }
    
    @Test
    public void testGetLongMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/long/x;var=8000000000"), "8000000000");        
    }
    
    @Test
    public void testGetFloatMatrixParam() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final GetRequestResult rr = new GetRequestResult(String.format("get/matrix/typematch/float/x;var=%f", floatVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleMatrixParam() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final GetRequestResult rr = new GetRequestResult(String.format("get/matrix/typematch/double/x;var=%f", doubleVal));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testGetDoubleMatrixParamWithIntValueConverts() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/double/x;var=13579"), "13579.0");
    }
    
    @Test
    public void testGetByteMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/byte/x;var=127"), "127");        
    }
    
    @Test
    public void testGetBooleanMatrixParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=true"), "true");        
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=false"), "false");        
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=True"), "true");        
        verifyOkResult(new GetRequestResult("get/matrix/typematch/boolean/x;var=FALSE"), "false");        
    }
    
    public void testGetStandardClassMatrixParamWithString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/Integer/x;var=5"), "5");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithStringConstructor() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/customwithstringctor/x;var=test"), "test");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithStringViaValueOf() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/matrix/typematch/customvalueofstring/x;var=tset"), "tset");
    }
    
    @Test
    public void testGetCustomClassMatrixParamWithoutStringConversionFails() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("get/matrix/typematch/custominvalid/x;var=test"));
    }
    
    @Test
    public void testConvertNativeBooleanToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/boolean/true"), "true");
    }
    
    @Test
    public void testConvertNativeByteToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/byte/127"), "127");
    }
    
    @Test
    public void testConvertNativeCharToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/char/c"), "c");
    }
    
    @Test
    public void testConvertNativeShortToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/short/20000"), "20000");
    }
    
    @Test
    public void testConvertNativeIntToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/int/4000000"), "4000000");
    }
    
    @Test
    public void testConvertNativeLongToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/long/8000000000"), "8000000000");
    }
    
    @Test
    public void testConvertNativeFloatToResponse() throws ClientProtocolException, IOException
    {
        float f = 1234567890.1121314151f;
        final GetRequestResult rr = new GetRequestResult(String.format("get/return/float/%f", f));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(f), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testConvertNativeDoubleToResponse() throws ClientProtocolException, IOException
    {
        double d = 102030405060708090.019181716151413121;
        final GetRequestResult rr = new GetRequestResult(String.format("get/return/double/%f", d));
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(d), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testConvertStringToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/string/abc123"), "abc123");
    }
    
    @Test
    public void testConvertJsonSerializableClassToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/person/bob"), "{\"name\":\"bob\",\"age\":30,\"city\":\"Nowhere\"}");
    }
    
    @Test
    public void testConvertClassToResponseWithToString() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/tostring/xyz789"), "xyz789");
    }
    
    @Test
    public void testConvertNativeArrayToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/array/1,2,3"), "[\"1\",\"2\",\"3\"]");
    }
    
    @Test
    public void testConvertListToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/list/1,2,3"), "[1,2,3]");
    }
    
    @Test
    public void testConvertMapToResponse() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/return/map/bob/30/Nowhere"), "{\"city\":\"Nowhere\",\"name\":\"bob\",\"age\":\"30\"}");
    }
    
    @Test
    public void testHandlerReturnsNullSends204() throws ClientProtocolException, IOException
    {
        final GetRequestResult rr = new GetRequestResult("get/return/null");
        assertEquals(Status.NO_CONTENT.getStatusCode(), rr.status());
        assertEquals(Status.NO_CONTENT.getReasonPhrase(), rr.reason());
    }
    
    @Test
    public void testHandlerThrowsExceptionSends500() throws ClientProtocolException, IOException
    {
        final GetRequestResult rr = new GetRequestResult("get/return/throws");
        verifyInternalServerErrorResult(rr);
    }
    
    
    
    // QueryParam Tests
    
    @Test
    public void testSingleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/single?firstname=bob"), "bob");
    }
    
    @Test
    public void testMultipleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/multiple?firstname=bob&lastname=bobson"), "bob bobson");
    }
    
    @Test
    public void testMultipleQueryParamForSameKey() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/multsamekey?name=alice&name=bob&name=eve"), "alice,bob,eve");
    }
    
    @Test
    public void testNoObjectQueryParamSendsNullValue() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/single"), "null");
    }
    
    @Test
    public void testObjectQueryParamWithRequiredParamWorks() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/requiredsingle?firstname=bob"), "bob");
    }
    
    @Test
    public void testNoObjectQueryParamWithRequiredParamReturns400() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/queryparams/requiredsingle"));
    }
    
    @Test
    public void testNoNativeQueryParamReturns400() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/queryparams/int"));
    }
    
    @Test
    public void testNonmatchingQueryParamIsIgnored() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/single?firstname=tim&lastname=timson"), "tim");
    }
    
    
    // FormParam Tests
    
    @Test
    public void testSingleFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/single", formdata, ContentType.APPLICATION_FORM_URLENCODED), "v1");
    }
    
    @Test
    public void testMultipleFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p3=v3&p2=v2&p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/multiple", formdata, ContentType.APPLICATION_FORM_URLENCODED), "v1,v2,v3");
    }
    
    @Test
    public void testQueryAndForm() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("fp2=fv2&fp3=fv3&fp1=fv1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/queryandform?qp1=qv1&qp3=qv3&qp2=qv2",
                formdata,
                ContentType.APPLICATION_FORM_URLENCODED),
            "qp1=qv1,qp2=qv2,qp3=qv3,fp1=fv1,fp2=fv2,fp3=fv3");
    }
    
    @Test
    public void testNoObjectFormParamSendsNullValue() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p2=v2", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/single", formdata, ContentType.APPLICATION_FORM_URLENCODED), "null");
    }
    
    @Test
    public void testObjectFormParamWithRequiredParamWorks() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparams/requiredsingle", formdata, ContentType.APPLICATION_FORM_URLENCODED), "v1");
    }

    @Test
    public void testNoObjectFormParamWithRequiredParamReturns400() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p2=v2", CharsetUtil.UTF_8.name());
        verify400Result(new PostRequestResult("post/formparam/requiredsingle", formdata, ContentType.APPLICATION_FORM_URLENCODED));
    }
    
    @Test
    public void testNoNativeFormParamReturns400()
    {
        // TODO
    }
    
    @Test
    public void testNonmatchingFormParamIsIgnored()
    {
        // TODO
    }
    
    @Test
    public void testFormPostBoolean() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=true", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/boolean", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "true");
    }
    
    @Test
    public void testFormPostByte() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=127", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/byte", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "127");
    }
    
    @Test
    public void testFormPostShort() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=20000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/short", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "20000");
    }
    
    @Test
    public void testFormPostInt() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=4000000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/int", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "4000000");
    }
    
    @Test
    public void testFormPostIntWithFloatValueFails() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=123.45", CharsetUtil.UTF_8.name());
        verifyInternalServerErrorResult(new PostRequestResult("post/formparam/typematch/int",
                formdata, ContentType.APPLICATION_FORM_URLENCODED));
    }
    
    @Test
    public void testFormPostFloat() throws ClientProtocolException, IOException
    {
        float floatVal = 1234567890.1121314151f;
        final String formdata = URLEncoder.encode(String.format("p=%f", floatVal), CharsetUtil.UTF_8.name());
        final PostRequestResult rr = new PostRequestResult("post/formparam/typematch/float", formdata, ContentType.APPLICATION_FORM_URLENCODED);
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Float.valueOf(floatVal), Float.valueOf(rr.content()));
    }
    
    @Test
    public void testFormPostDouble() throws ClientProtocolException, IOException
    {
        double doubleVal = 102030405060708090.019181716151413121;
        final String formdata = URLEncoder.encode(String.format("p=%f", doubleVal), CharsetUtil.UTF_8.name());
        final PostRequestResult rr = new PostRequestResult("post/formparam/typematch/double", formdata, ContentType.APPLICATION_FORM_URLENCODED);
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(Double.valueOf(doubleVal), Double.valueOf(rr.content()));
    }
    
    @Test
    public void testFormPostLong() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=8000000000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/long", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "8000000000");
    }
    
    @Test
    public void testFormPostInteger() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=4000000", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/Integer", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "4000000");
    }
    
    @Test
    public void testFormPostCustomWithStringCtor() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=bob", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/customwithstringctor", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "bob");
    }
    
    @Test
    public void testFormPostCustomValueOfString() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=bill", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/formparam/typematch/customvalueofstring", formdata, ContentType.APPLICATION_FORM_URLENCODED),
                "bill");
    }
    
    @Test
    public void testFormPostCustomInvalid() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p=ben", CharsetUtil.UTF_8.name());
        verifyInternalServerErrorResult(new PostRequestResult("post/formparam/typematch/custominvalid",
                formdata, ContentType.APPLICATION_FORM_URLENCODED));
    }
    
    
    // Consumes tests
    
    @Test
    public void testFormPostWithClassLevelConsumes() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("form/post/single", formdata, ContentType.APPLICATION_FORM_URLENCODED), "p1=v1");
        verifyNotAcceptableResult(new PostRequestResult("form/post/single", "p1=v1", ContentType.TEXT_PLAIN));
    }
    
    @Test
    public void testPostDifferentContentTypes() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/xml", xml, ContentType.APPLICATION_XML), xml);
        verifyOkResult(new PostRequestResult("post/json", json, ContentType.APPLICATION_JSON), json);
        verifyOkResult(new PostRequestResult("post/text", text, ContentType.TEXT_PLAIN), text);
    }
    
    // We are not going to support this for now.
    // The spec allows for an endpoint to accept and deliver multiple content types, but
    // also that a single resource can disambiguate between requests based on the content type
    // provided.  In our implementation that means the value for @Consumes would have to be
    // used to identify a resource in the ResourceIdentifier class.  This ruins the quicker
    // lookup Weaver is trying to achieve - and for no real purpose, because it is a minor matter
    // for the implementer to simply look at the @HeaderParam("Content-Type") and make a decision
    // based on that instead of writing two endpoints.
    @Ignore
    @Test
    public void testPostDisambiguatesOnContentType() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/bycontenttype", xml, ContentType.APPLICATION_XML), "xml");
        verifyOkResult(new PostRequestResult("post/bycontenttype", json, ContentType.APPLICATION_JSON), "json");
        verifyOkResult(new PostRequestResult("post/bycontenttype", text, ContentType.TEXT_PLAIN), "text");
    }
    
    
    // @DefaultValue
    
    @Test
    public void testDefaultValueNativeQueryParam()
    {
        // TODO
    }
    
    @Test
    public void testDefaultValueCustomQueryParam()
    {
        // TODO
    }
    
    @Test
    public void testDefaultValueMultipleQueryParam()
    {
        // TODO
    }
    
    @Test
    public void testSomeDefaultValuesMultipleQueryParam()
    {
        // TODO
    }
    
    @Test
    public void testProvidedQueryParamOverridesDefaultValue()
    {
        // TODO
    }
    
    @Test
    public void testObjectQueryParamWithRequiredParamAndDefaultValueWorks() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/queryparams/requiredanddefaultsingle?firstname=bob"), "bob");
        verifyOkResult(new GetRequestResult("get/queryparams/requiredanddefaultsingle"), "tim");
    }    
    
    @Test
    public void testDefaultValueNativeFormParam()
    {
        // TODO
    }
    
    @Test
    public void testDefaultValueCustomFormParam()
    {
        // TODO
    }
    
    @Test
    public void testDefaultValueMultipleFormParam()
    {
        // TODO
    }
    
    @Test
    public void testSomeDefaultValuesMultipleFormParam()
    {
        // TODO
    }
    
    @Test
    public void testProvidedFormParamOverridesDefaultValue()
    {
        // TODO
    }
    
    @Test
    public void testDefaultValueQueryAndFormParam()
    {
        // TODO
    }
    
    // TODO:
    // Test PUT retrieves payload
    // Test proper ordering of resource selection (best match wins)
    // Test return 405 - Method Not Allowed - when calling an endpoint that exists but has nonmatching method
    // Bubble processing exceptions up somehow... (invalid/unclosed regexes for example)
    // Handle query with missing/nonmatching @FormParam/@QueryParam
    //  - Missing (parameter is specified in handler, but not present in request)
    //    - If the parameter is a native type:
    //      - If @DefaultValue is specified, use that
    //      - Otherwise return a 400 error
    //      - No missing parameters allowed (no "NULL" value for primitives)
    //    - If the parameter is a class type:
    //      - If @DefaultValue is specified, use that
    //      - Else, if @RequiredParam is specified, return a 400 error
    //      - Otherwise, use NULL
    //  - Nonmatching - Ignore the parameter.
    //    - We could, at some point, define an annotation (@StrictParams?) which
    //      would mean that strict matching is enforced, or 400 level error.
    

    private static RestServer server;
    private final String json = "{ \"property\" : \"value\", \"array\" : [1, 2, 3], \"embedded\" : { \"ep\" : \"ev\" } }";
    private final String xml = "<xml><mynode myattr=\"myval\">text</mynode></xml>";
    private final String text = "abc123";
    
    private void verifyOkResult(final RequestResult rr, final String expectedResponse)
    {
        assertEquals(Status.OK.getStatusCode(), rr.status());
        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
        assertEquals(expectedResponse, rr.content());
    }
    
    private void verifyNotFoundResult(final RequestResult rr)
    {
        assertEquals(Status.NOT_FOUND.getStatusCode(), rr.status());
        assertEquals(Status.NOT_FOUND.getReasonPhrase(), rr.reason());
        assertTrue(Strings.isNullOrEmpty(rr.content()));
    }
    
    private void verifyNotAcceptableResult(final RequestResult rr)
    {
        assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), rr.status());
        assertEquals(Status.NOT_ACCEPTABLE.getReasonPhrase(), rr.reason());
        assertTrue(Strings.isNullOrEmpty(rr.content()));
    }
    
    private void verify400Result(final RequestResult rr)
    {
        assertEquals(Status.BAD_REQUEST.getStatusCode(), rr.status());
        assertEquals(Status.BAD_REQUEST.getReasonPhrase(), rr.reason());
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

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
}
