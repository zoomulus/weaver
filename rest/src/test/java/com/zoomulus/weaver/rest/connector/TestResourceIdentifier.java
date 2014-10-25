package com.zoomulus.weaver.rest.connector;

import static org.junit.Assert.assertEquals;
import io.netty.handler.codec.http.HttpMethod;

import org.junit.Test;

import com.zoomulus.weaver.rest.resource.ResourceIdentifier;

public class TestResourceIdentifier
{
    @Test
    public void testConstruct()
    {
        final ResourceIdentifier ri = new ResourceIdentifier("path/to/resource", HttpMethod.GET);
        assertEquals("/path/to/resource/", ri.getPath());
        assertEquals(HttpMethod.GET, ri.getMethod());
    }
    
    @Test
    public void testConstructWithTwoPaths()
    {
        final ResourceIdentifier ri = new ResourceIdentifier("base/path", "resource/path", HttpMethod.POST);
        assertEquals("/base/path/resource/path/", ri.getPath());
        assertEquals(HttpMethod.POST, ri.getMethod());
    }
    
    @Test
    public void testConstructWithTwoPathsDedupsSeparator()
    {
        final ResourceIdentifier ri = new ResourceIdentifier("base/path/", "/resource/path", HttpMethod.POST);
        assertEquals("/base/path/resource/path/", ri.getPath());
        assertEquals(HttpMethod.POST, ri.getMethod());
    }
}
