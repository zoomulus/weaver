package com.zoomulus.weaver.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.RequestResult;

public class RestServerAcceptHeaderTest extends RestServerTestBase
{
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
}
