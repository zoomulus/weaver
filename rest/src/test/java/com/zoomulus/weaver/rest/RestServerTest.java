package com.zoomulus.weaver.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.connector.ServerConnector;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.connector.RestServerConnector;
import com.zoomulus.weaver.rest.testutils.DeleteRequestResult;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.HeadRequestResult;
import com.zoomulus.weaver.rest.testutils.OptionsRequestResult;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;
import com.zoomulus.weaver.rest.testutils.PutRequestResult;
import com.zoomulus.weaver.rest.testutils.RequestResult;

public class RestServerTest extends RestServerTestBase
{    
    private Map<String, String> getAcceptHeader(final String ct)
    {
        final Map<String, String> headers = Maps.newHashMap();
        headers.put("Accept", ct);
        return headers;
    }    
        
    
    // PathSegment Tests
    
    
    
    
    // MatrixParam Tests
    
    
    
    // Type Conversions
    
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
    
    
    // Http Response Code tests
    
    @Test
    public void testGetHandlerReturns200() throws ClientProtocolException, IOException
    {
        final RequestResult rr = new GetRequestResult("get/return/normal");
        verifyOkResult(rr, "normal");
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
    
    @Test
    public void testNonmatchingHandlerSends404() throws ClientProtocolException, IOException
    {
        final GetRequestResult result = new GetRequestResult("get/return/missing");
        verifyNotFoundResult(result);
    }
    
    @Test
    public void testPostToGetResourceSends405() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("get/return/normal");
        verifyMethodNotAllowedResult(result);
    }
    
    @Test
    public void testGetWithNonmatchingAcceptSends406() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/return/applicationxml", getAcceptHeader(ContentType.APPLICATION_JSON));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testPostWithNonmatchingContentTypeSends415() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("post/formparam/single",
                "{\"type\":\"json\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testEndpointCanReturn201() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("post/return/created");
        assertEquals(Status.CREATED.getStatusCode(), result.status());
        assertEquals(Status.CREATED.getReasonPhrase(), result.reason());
    }
    
    @Test
    public void testEndpointCanReturn202() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("post/return/accepted");
        assertEquals(Status.ACCEPTED.getStatusCode(), result.status());
        assertEquals(Status.ACCEPTED.getReasonPhrase(), result.reason());
    }
    
    @Test
    public void testEndpointCanReturnAnyValidHttpStatus() throws ClientProtocolException, IOException
    {
        // No 100-level requests here, those have to do with continuation
        RequestResult result = new GetRequestResult("get/return/custom?status=205");
        assertEquals(205, result.status());
        result = new GetRequestResult("get/return/custom?status=302");
        assertEquals(302, result.status());
        result = new GetRequestResult("get/return/custom?status=409");
        assertEquals(409, result.status());
        result = new GetRequestResult("get/return/custom?status=503");
        assertEquals(503, result.status());
        result = new GetRequestResult("get/return/custom?status=600");
        assertEquals(600, result.status());
    }

    
    // QueryParam Tests
    
    
    
    // FormParam Tests
    
    
    
    // Consumes tests
    
    @Test
    public void testFormPostWithClassLevelConsumes() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("p1=v1", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("form/post/single", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "p1=v1");
        verifyUnsupportedMediaTypeResult(new PostRequestResult("form/post/single", "p1=v1", ContentType.TEXT_PLAIN_TYPE));
    }
    
    @Test
    public void testPostDifferentContentTypes() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/xml", xml, ContentType.APPLICATION_XML_TYPE), xml);
        verifyOkResult(new PostRequestResult("post/json", json, ContentType.APPLICATION_JSON_TYPE), json);
        verifyOkResult(new PostRequestResult("post/text", text, ContentType.TEXT_PLAIN_TYPE), text);
    }
    
    @Test
    public void testConsumesOnGetReturns500() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new GetRequestResult("/get/consumes"));
    }
    
    @Test
    public void testConsumesOnHeadReturns500() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new HeadRequestResult("/head/consumes"));
    }
    
    @Test
    public void testConsumesOnOptionsReturns500() throws ClientProtocolException, IOException
    {
        verifyInternalServerErrorResult(new OptionsRequestResult("/options/consumes"));
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
        verifyOkResult(new PostRequestResult("post/bycontenttype", xml, ContentType.APPLICATION_XML_TYPE), "xml");
        verifyOkResult(new PostRequestResult("post/bycontenttype", json, ContentType.APPLICATION_JSON_TYPE), "json");
        verifyOkResult(new PostRequestResult("post/bycontenttype", text, ContentType.TEXT_PLAIN_TYPE), "text");
    }
    
    
    // DefaultValue tests
    
    @Test
    public void testDefaultValueNativeQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/int"), "111");
    }
    
    @Test
    public void testDefaultValueObjectQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/string"), "tim");
    }
    
    @Test
    public void testDefaultValueMultipleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/multiple"), "tim,111");
    }
    
    @Test
    public void testSomeDefaultValuesMultipleQueryParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/multiple?name=bob"), "bob,111");
    }
    
    @Test
    public void testProvidedQueryParamOverridesDefaultValue() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/string?name=bob"), "bob");
    }
    
    @Test
    public void testObjectQueryParamWithRequiredParamAndDefaultValueWorks() throws ClientProtocolException, IOException
    {
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/requiredanddefaultsingle?firstname=bob"), "bob");
        verifyOkResult(new GetRequestResult("get/defaultvalue/query/requiredanddefaultsingle"), "tim");
    }    
    
    @Test
    public void testDefaultValueNativeFormParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/int", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "111");
    }
    
    @Test
    public void testDefaultValueObjectFormParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/string", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim");
    }
    
    @Test
    public void testDefaultValueMultipleFormParam() throws ClientProtocolException, IOException
    {
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/multiple", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim,111");
    }
    
    @Test
    public void testSomeDefaultValuesMultipleFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("age=222", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/multiple", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim,222");
    }
    
    @Test
    public void testProvidedFormParamOverridesDefaultValue() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("name=bob", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/string", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "bob");
    }
    
    @Test
    public void testObjectFormParamWithRequiredParamAndDefaultValueWorks() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("name=bob", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/requiredanddefaultsingle", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE), "bob");
        verifyOkResult(new PostRequestResult("post/defaultvalue/form/requiredanddefaultsingle", "", ContentType.APPLICATION_FORM_URLENCODED_TYPE), "tim");
    }    
    
    @Test
    public void testDefaultValueQueryAndFormParam() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("firstname=tim", CharsetUtil.UTF_8.name());
        verifyOkResult(new PostRequestResult("post/defaultvalue/queryandform?gender=male", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE),
                "tim timson,male,111");
    }
    
    
    // StrictParams tests
    @Test
    public void testNonmatchingQueryParamWithStrictParamsFails() throws ClientProtocolException, IOException
    {
        verify400Result(new GetRequestResult("get/strictparams?name=bob&catname=killer"));
    }
    
    @Test
    public void testNonmatchingFormParamWithStrictParamsFails() throws ClientProtocolException, IOException
    {
        final String formdata = URLEncoder.encode("name=bob&catname=killer", CharsetUtil.UTF_8.name());
        verify400Result(new PostRequestResult("post/strictparams", formdata, ContentType.APPLICATION_FORM_URLENCODED_TYPE));
    }
    
    
    // Produces tests
    
    
    
    // Accept/@Produces matching and type conversion
    
    @Test
    public void testAcceptWithResponseMatchingContentType() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/response/singlect", getAcceptHeader(ContentType.APPLICATION_JSON));
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testAcceptTextPlainWithResponseSpecifyingOtherContentTypeFails() throws ClientProtocolException, IOException
    {
        // Even if the response entity could be text/plain.
        final RequestResult result = new GetRequestResult("/get/accept/response/jsonstring", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testAcceptTextPlainWithStringReturnsTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/string/text", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyOkResult(result, "text");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testAcceptJsonWithStringReturnsJsonString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/string/text", getAcceptHeader(ContentType.APPLICATION_JSON));
        verifyOkResult(result, "\"text\"");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testAcceptXmlWithStringReturnsXmlString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/string/text", getAcceptHeader(ContentType.APPLICATION_XML));
        verifyOkResult(result, "<String>text</String>");
        verifyContentType(result, ContentType.APPLICATION_XML_TYPE);
    }
    
    @Test
    public void testAcceptTextHtmlWithStringAndNoProducesFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/string/text", getAcceptHeader(ContentType.TEXT_HTML));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testAcceptTextPlainWithStringAndProducesOtherFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/string/text/html", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testAcceptTextPlainWithStringAndProducesMultipleSelectsTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/string/text/multipleproduces", getAcceptHeader(ContentType.TEXT_HTML));
        verifyOkResult(result, "text");
        verifyContentType(result, ContentType.TEXT_HTML_TYPE);
    }
    
    @Test
    public void testAcceptTextPlainWithObjectReturnsToString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/object/text", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testAcceptJsonWithObjectReturnsJsonString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/object/text", getAcceptHeader(ContentType.APPLICATION_JSON));
        verifyOkResult(result, "{\"s\":\"custom\"}");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testAcceptXmlWithObjectReturnsXmlString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/object/text", getAcceptHeader(ContentType.APPLICATION_XML));
        verifyOkResult(result, "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>");
        verifyContentType(result, ContentType.APPLICATION_XML_TYPE);
    }
    
    @Test
    public void testAcceptTextPlainWithObjectAndProducesOtherFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/object/text/html", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testAcceptTextPlainWithObjectAndProducesMultipleSelectsTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/object/text/multipleproduces", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testAcceptTextPlainWithNativeReturnsToString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/native/text", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyOkResult(result, "111");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testAcceptJsonWithNativeReturnsJsonString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/native/text", getAcceptHeader(ContentType.APPLICATION_JSON));
        verifyOkResult(result, "111");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testAcceptXmlWithNativeReturnsXmlString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/native/text", getAcceptHeader(ContentType.APPLICATION_XML));
        verifyOkResult(result, "<Integer>111</Integer>");
        verifyContentType(result, ContentType.APPLICATION_XML_TYPE);
    }
    
    @Test
    public void testAcceptTextPlainWithNativeAndProducesOtherFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/native/text/html", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyNotAcceptableResult(result);
    }
    
    @Test
    public void testAcceptTextPlainWithNativeAndProducesMultipleSelectsTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/accept/native/text/multipleproduces", getAcceptHeader(ContentType.TEXT_PLAIN));
        verifyOkResult(result, "111");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    
    // POST/PUT tests - retrieving non-standard payloads and converting to types
    
    @Test
    public void testPostTextPlainToStringPayloadProvidesRawData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string", "text", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "text");
    }
    
    @Test
    public void testPostApplicationJsonToStringPayloadProvidesJsonData() throws ClientProtocolException, IOException
    {
        final RequestResult result =  new PostRequestResult("/post/string", "{\"s\":\"custom\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "{\"s\":\"custom\"}");
    }
    
    @Test
    public void testPostApplicationXmlToStringPayloadProvidesXmlData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string", "<String>text</String>", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "<String>text</String>");
    }
    
    @Test
    public void testPostOtherContentTypeToStringPayloadProvidesRawData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string", "<html>hi</html>", ContentType.TEXT_HTML_TYPE);
        verifyOkResult(result, "<html>hi</html>");
    }
    
    @Test
    public void testPostToStringPayloadWithConsumesTextPlainProvidesText() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/text", "text", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "text");
    }
    
    @Test
    public void testPostToStringPayloadWithConsumesApplicationJsonProvidesJson() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/json", "{\"s\":\"custom\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostNonJsonStringPayloadWithConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/json", "not json", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostToStringPayloadWithConsumesApplicationXmlProvidesXml() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/xml",
                "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostNonXmlStringPayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result =  new PostRequestResult("/post/string/xml", "not xml", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostTextHtmlStringWithConsumesTextPlainFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/text", "<html>hi</html>", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostApplicationJsonToObjectPayloadProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/json/noconsumes", "{\"s\":\"custom\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostApplicationXmlToObjectPayloadProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/xml/noconsumes",
                "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostTextPlainToObjectCallsStringConstructor() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/object/text/noconsumes/stringctor", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostTextPlainToObjectCallsValueOf() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/object/text/noconsumes/valueof", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostOtherContentTypeToObjectFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/object/text/noconsumes/valueof", "custom", ContentType.TEXT_HTML_TYPE);
        verifyInternalServerErrorResult(result);
    }
    
    @Test
    public void testPostJsonStringToObjectPayloadWithConsumesApplicationJsonProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/json", "{\"s\":\"custom\"}", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostNonJsonStringToObjectPayloadWithConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/json", "not json", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostXmlStringToObjectPayloadWithConsumesApplicationXmlProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/xml",
                "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostNonXmlStringToObjectPayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/xml", "not xml", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostTextStringToObjectPayloadWithConsumesTextPlainCallsStringCtor() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/text/stringctor", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostTextStringToObjectPayloadWithConsumesTextPlainCallsValueOf() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/text/valueof", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPostTextHtmlToObjectConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/string/object/consumes/json", "{\"s\":\"custom\"}", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostApplicationJsonToNativePayloadProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native", "111", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPostApplicationXmlToNativePayloadFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native", "<Integer>111</Integer>", ContentType.APPLICATION_XML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
        
        // This appears at first glance to be broken, but actually it is more true that deserialization
        // of scalar type representations to scalar types is generally unsupported by the spec implemented
        // by Jackson, and only by luck does it work for JSON at all.  This is primarily due to the fact that when
        // a scalar is serialized into JSON, there is no "object" wrapper around the value, which there
        // is in XML.  It is this START_OBJECT token at the start of the XML stream that is throwing
        // the Jackson parser off and making it so this deserialization does not work as you might expect.
        //
        // Issue https://github.com/FasterXML/jackson-dataformat-xml/issues/139 was submitted for this issue.
        // I tried to write a patch but the way jackson-dataformat-xml is implemented makes it
        // extremely difficult to support this functionality without a significant redesign.
        // I came up with a hacky patch (https://github.com/FasterXML/jackson-dataformat-xml/issues/140)
        // but they had concerns which, once explained to me, I share.
        //
        // So the result is that deserialization of XML to native types is not supported.
        // JSON works though.
        //
        // -MR
    }
    
    @Test
    public void testPostTextPlainToNativeProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native", "111", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPostOtherContentTypeToNativeFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native", "111", ContentType.TEXT_HTML_TYPE);
        verifyInternalServerErrorResult(result);
    }
    
    @Test
    public void testPostJsonStringToNativePayloadWithConsumesApplicationJsonProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native/consumes/json", "111", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPostNonJsonStringToNativePayloadWithConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native/consumes/json", "not json", ContentType.APPLICATION_JSON_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }

    @Test
    public void testPostXmlStringToNativePayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native/consumes/xml", "<Integer>111</Integer>", ContentType.APPLICATION_XML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostNonXmlStringToNativePayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native/consumes/xml", "not xml", ContentType.APPLICATION_XML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostTextStringToNativePayloadWithConsumesTextPlainProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native/consumes/text", "111", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPostTextHtmlToNativeConsumesTextPlainFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/native/consumes/text", "111", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPostApplicationJsonToByteArrayPayloadProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray", "bytes", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostApplicationXmlToByteArrayPayloadProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray", "bytes", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostTextPlainToByteArrayProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray", "bytes", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostAnyContentTypeToByteArrayProvidesRawData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray", "bytes", ContentType.TEXT_HTML_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostJsonStringToByteArrayPayloadWithConsumesApplicationJsonProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray/consumes/json", "bytes", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostXmlStringToByteArrayPayloadWithConsumesApplicationXmlProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray/consumes/xml", "bytes", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostTextStringToByteArrayPayloadWithConsumesTextPlainProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray/consumes/text", "bytes", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPostTextHtmlToByteArrayConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PostRequestResult("/post/bytearray/consumes/json", "bytes", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutTextPlainToStringPayloadProvidesRawData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string", "text", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "text");
    }
    
    @Test
    public void testPutApplicationJsonToStringPayloadProvidesJsonData() throws ClientProtocolException, IOException
    {
        final RequestResult result =  new PutRequestResult("/put/string", "{\"s\":\"custom\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "{\"s\":\"custom\"}");        
    }
    
    @Test
    public void testPutApplicationXmlToStringPayloadProvidesXmlData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string", "<String>text</String>", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "<String>text</String>");        
    }
    
    @Test
    public void testPutOtherContentTypeToStringPayloadProvidesRawData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string", "<html>hi</html>", ContentType.TEXT_HTML_TYPE);
        verifyOkResult(result, "<html>hi</html>");        
    }
    
    @Test
    public void testPutToStringPayloadWithConsumesTextPlainProvidesText() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/text", "text", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "text");        
    }
    
    @Test
    public void testPutToStringPayloadWithConsumesApplicationJsonProvidesJson() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/json", "{\"s\":\"custom\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "custom");        
    }
    
    @Test
    public void testPutNonJsonStringPayloadWithConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/json", "not json", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);        
    }
    
    @Test
    public void testPutToStringPayloadWithConsumesApplicationXmlProvidesXml() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/xml",
                "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "custom");        
    }
    
    @Test
    public void testPutNonXmlStringPayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result =  new PutRequestResult("/put/string/xml", "not xml", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);        
    }
    
    @Test
    public void testPutTextHtmlToStringConsumesTextPlainFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/text", "<html>hi</html>", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutApplicationJsonToObjectPayloadProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/json/noconsumes", "{\"s\":\"custom\"}", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutApplicationXmlToObjectPayloadProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/xml/noconsumes",
                "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutTextPlainToObjectCallsStringConstructor() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/object/text/noconsumes/stringctor", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutTextPlainToObjectCallsValueOf() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/object/text/noconsumes/valueof", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutOtherContentTypeToObjectFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/object/text/noconsumes/valueof", "custom", ContentType.TEXT_HTML_TYPE);
        verifyInternalServerErrorResult(result);
    }
    
    @Test
    public void testPutJsonStringToObjectPayloadWithConsumesApplicationJsonProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/json", "{\"s\":\"custom\"}", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutNonJsonStringToObjectPayloadWithConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/json", "not json", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutXmlStringToObjectPayloadWithConsumesApplicationXmlProvidesObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/xml",
                "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutNonXmlStringToObjectPayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/xml", "not xml", ContentType.TEXT_PLAIN_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutTextStringToObjectPayloadWithConsumesTextPlainCallsStringCtor() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/text/stringctor", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutTextStringToObjectPayloadWithConsumesTextPlainCallsValueOf() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/text/valueof", "custom", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "custom");
    }
    
    @Test
    public void testPutTextHtmlToObjectConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/string/object/consumes/json", "{\"s\":\"custom\"}", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutApplicationJsonToNativePayloadProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native", "111", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPutApplicationXmlToNativePayloadFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native", "<Integer>111</Integer>", ContentType.APPLICATION_XML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutTextPlainToNativeProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native", "111", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPutOtherContentTypeToNativeFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native", "111", ContentType.TEXT_HTML_TYPE);
        verifyInternalServerErrorResult(result);
    }
    
    @Test
    public void testPutJsonStringToNativePayloadWithConsumesApplicationJsonProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native/consumes/json", "111", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPutNonJsonStringToNativePayloadWithConsumesApplicationJsonFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native/consumes/json", "not json", ContentType.APPLICATION_JSON_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutXmlStringToNativePayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native/consumes/xml", "<Integer>111</Integer>", ContentType.APPLICATION_XML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutNonXmlStringToNativePayloadWithConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native/consumes/xml", "not xml", ContentType.APPLICATION_XML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutTextStringToNativePayloadWithConsumesTextPlainProvidesNative() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("put/native/consumes/text", "111", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "111");
    }
    
    @Test
    public void testPutTextHtmlToNativeConsumesTextPlainFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/native/consumes/text", "111", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    @Test
    public void testPutApplicationJsonToByteArrayPayloadProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray", "bytes", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutApplicationXmlToByteArrayPayloadProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray", "bytes", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutTextPlainToByteArrayProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray", "bytes", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutAnyContentTypeToByteArrayProvidesRawData() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray", "bytes", ContentType.TEXT_HTML_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutJsonStringToByteArrayPayloadWithConsumesApplicationJsonProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray/consumes/json", "bytes", ContentType.APPLICATION_JSON_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutXmlStringToByteArrayPayloadWithConsumesApplicationXmlProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray/consumes/xml", "bytes", ContentType.APPLICATION_XML_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutTextStringToByteArrayPayloadWithConsumesTextPlainProvidesByteArray() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray/consumes/text", "bytes", ContentType.TEXT_PLAIN_TYPE);
        verifyOkResult(result, "bytes");
    }
    
    @Test
    public void testPutTextHtmlToByteArrayConsumesApplicationXmlFails() throws ClientProtocolException, IOException
    {
        final RequestResult result = new PutRequestResult("/put/bytearray/consumes/json", "bytes", ContentType.TEXT_HTML_TYPE);
        verifyUnsupportedMediaTypeResult(result);
    }
    
    
//    private static RestServer server;
    private final String json = "{ \"property\" : \"value\", \"array\" : [1, 2, 3], \"embedded\" : { \"ep\" : \"ev\" } }";
    private final String xml = "<xml><mynode myattr=\"myval\">text</mynode></xml>";
    private final String text = "abc123";
    
//    private void verifyOkResult(final RequestResult rr, final String expectedResponse)
//    {
//        assertEquals(Status.OK.getStatusCode(), rr.status());
//        assertEquals(Status.OK.getReasonPhrase(), rr.reason());
//        assertEquals(expectedResponse, rr.content());
//    }
//    
//    private void verifyNotFoundResult(final RequestResult rr)
//    {
//        assertEquals(Status.NOT_FOUND.getStatusCode(), rr.status());
//        assertEquals(Status.NOT_FOUND.getReasonPhrase(), rr.reason());
//        assertTrue(Strings.isNullOrEmpty(rr.content()));
//    }
//    
//    private void verifyMethodNotAllowedResult(final RequestResult rr)
//    {
//        assertEquals(Status.METHOD_NOT_ALLOWED.getStatusCode(), rr.status());
//        assertEquals(Status.METHOD_NOT_ALLOWED.getReasonPhrase(), rr.reason());
//    }
//    
//    private void verifyNotAcceptableResult(final RequestResult rr)
//    {
//        assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), rr.status());
//        assertEquals(Status.NOT_ACCEPTABLE.getReasonPhrase(), rr.reason());
//        assertTrue(Strings.isNullOrEmpty(rr.content()));
//    }
//    
//    private void verifyUnsupportedMediaTypeResult(final RequestResult rr)
//    {
//        assertEquals(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), rr.status());
//        assertEquals(Status.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), rr.reason());
//    }
//    
//    private void verify400Result(final RequestResult rr)
//    {
//        assertEquals(Status.BAD_REQUEST.getStatusCode(), rr.status());
//        assertEquals(Status.BAD_REQUEST.getReasonPhrase(), rr.reason());
//    }
//    
//    private void verifyInternalServerErrorResult(final RequestResult rr)
//    {
//        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), rr.status());
//        assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), rr.reason());
//    }
    
//    private void verifyContentType(final RequestResult rr, final ContentType contentType)
//    {
//        //assertEquals(contentType, rr.contentType());
//        assertTrue(contentType.isCompatibleWith(rr.contentType()));
//    }
    
//    private void verifyContentType(final RequestResult rr, final String contentType)
//    {
//        assertEquals(contentType, rr.contentType.toString());
//    }
    

//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception
//    {
//        final ServerConnector connector = RestServerConnector.builder()
//                .withPort(22002)
//                .withResource(RestServerTestResource.class)
//                .withResource(RestServerTestResourceFormData.class)
//                .build();
//        server = new RestServer(connector);
//        server.start();
//    }
//
//    @AfterClass
//    public static void tearDownAfterClass() throws Exception
//    {
//        server.shutdown();
//    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
}
