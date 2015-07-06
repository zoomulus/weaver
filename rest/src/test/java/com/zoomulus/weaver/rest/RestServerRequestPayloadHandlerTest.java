package com.zoomulus.weaver.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.testutils.PostRequestResult;
import com.zoomulus.weaver.rest.testutils.PutRequestResult;
import com.zoomulus.weaver.rest.testutils.RequestResult;

public class RestServerRequestPayloadHandlerTest extends RestServerTestBase
{
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
}
