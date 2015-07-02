package com.zoomulus.weaver.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.GetRequestResult;
import com.zoomulus.weaver.rest.testutils.RequestResult;

public class RestServerProducesTest extends RestServerTestBase
{
    @Test
    public void testGetResponseWithStringEntityWithProducesJsonReturnsDataTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/response/string/json");
        verifyOkResult(result, "not actually json");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetStringWithProducesJsonReturnsJsonizedString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/string/json");
        verifyOkResult(result, "\"text\"");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testGetObjectWithProducesJsonReturnsJsonizedObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/object/json");
        verifyOkResult(result, "{\"s\":\"custom\"}");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testGetNativeTypeWithProducesJsonReturnsJsonizedValue() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/native/json");
        verifyOkResult(result, "111");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testGetUnJsonifiableObjectWithProducesJsonReturns500() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/invalid/json");
        verifyInternalServerErrorResult(result);
    }
    
    @Test
    public void testGetResponseWithStringEntityWithProducesXmlReturnsDataTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/string/xml");
        verifyOkResult(result, "not actually xml");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetStringWithProducesXmlReturnsXmlizedString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/string/xml");
        verifyOkResult(result, "<String>text</String>");
        verifyContentType(result, ContentType.APPLICATION_XML_TYPE);
    }
    
    @Test
    public void testGetObjectWithProducesXmlReturnsXmlizedObject() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/object/xml");
        verifyOkResult(result, "<CustomWithStringCtor><s>custom</s></CustomWithStringCtor>");
        verifyContentType(result, ContentType.APPLICATION_XML_TYPE);
    }
    
    @Test
    public void testGetNativeTypeWithProducesXmlReturnsXmlizedValue() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/native/xml");
        verifyOkResult(result, "<Integer>111</Integer>");
        verifyContentType(result, ContentType.APPLICATION_XML_TYPE);
    }
    
    @Test
    public void testGetUnXmlizableObjectWithProducesXmlReturns500() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("get/produces/invalid/xml");
        verifyInternalServerErrorResult(result);
    }
    
    @Test
    public void testGetResponseWithStringEntityWithProducesTextDataIsUnmodified() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/string/text");
        verifyOkResult(result, "some text");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithObjectEntityWithProducesJsonReturnsObjectToStringTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/object/json");
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithNativeEntityWithProducesJsonReturnsEntityToStringTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/native/json");
        verifyOkResult(result, "5");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithObjectEntityWithProducesXmlReturnsObjectToStringTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/object/xml");
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithNativeEntityWithProducesXmlReturnsEntityToStringTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/native/xml");
        verifyOkResult(result, "5");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithStringEntityWithNoProducesReturnsString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/string/noproduces");
        verifyOkResult(result, "text");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithObjectWithToStringEntityWithNoProducesReturnsToString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/object/noproduces");
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetResponseWithNativeEntityWithNoProducesReturnsStringRep() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/native/noproduces");
        verifyOkResult(result, "111");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetStringWithNoProducesReturnsString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/string/noproduces");
        verifyOkResult(result, "text");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetObjectWithToStringWithNoProducesReturnsString() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/object/noproduces");
        verifyOkResult(result, "custom");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetJsonizableObjectWithNoProducesReturnsJson() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/jsonobject/noproduces");
        verifyOkResult(result, "{\"s\":\"abc\",\"i\":123}");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testGetNonJsonizableObjectWithNoProducesReturnsDefaultToString() throws ClientProtocolException, IOException
    {
        final RequestResult result =  new GetRequestResult("/get/produces/nonjsonobject/noproduces");
        verifyOkResult(result, "text");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testGetNativeTypeWithNoProducesReturnsStringRep() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/native/noproduces");
        verifyOkResult(result, "111");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testResponseWithCustomContentTypeReturnsCorrectContentType() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/response/custom");
        verifyOkResult(result, "custom content");
        verifyContentType(result, new ContentType("application/z-nonstandard"));
    }
    
    @Test
    public void testStringWithCustomContentTypeReturnsCorrectContentType() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/string/custom");
        verifyOkResult(result, "custom content");
        verifyContentType(result, new ContentType("application/z-nonstandard"));
    }
    
    @Test
    public void testStringWithMultipleProducesReturnsJson() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/string/multipleproduces");
        verifyOkResult(result, "\"text\"");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testObjectWithMultipleProducesReturnsJson() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/object/multipleproduces");
        verifyOkResult(result, "{\"s\":\"custom\"}");
        verifyContentType(result, ContentType.APPLICATION_JSON_TYPE);
    }
    
    @Test
    public void testJsonizableObjectWithMultipleProducesReturnsTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/jsonobject/multipleproduces");
        verifyOkResult(result, "{\"s\":\"abc\",\"i\":123}");
        verifyContentType(result, ContentType.TEXT_PLAIN_TYPE);
    }
    
    @Test
    public void testNonJsonizableObjectWithMultipleProducesReturnsTextPlain() throws ClientProtocolException, IOException
    {
        final RequestResult result = new GetRequestResult("/get/produces/nonjsonobject/multipleproduces");
        verifyInternalServerErrorResult(result);
    }
}
