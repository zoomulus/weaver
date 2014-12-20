package com.zoomulus.weaver.rest.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.google.common.collect.Sets;

public class TestDefaultResourceScannerStrategy
{
    private final ResourceScannerStrategy scanner = new DefaultResourceScannerStrategy();
    
    private final Set<Class<?>> singleGet = Sets.newHashSet(SingleGet.class);
    private final Set<Class<?>> singlePost = Sets.newHashSet(SinglePost.class);
    private final Set<Class<?>> singlePut = Sets.newHashSet(SinglePut.class);
    private final Set<Class<?>> singleDelete = Sets.newHashSet(SingleDelete.class);
    private final Set<Class<?>> singleHead = Sets.newHashSet(SingleHead.class);
    private final Set<Class<?>> singleOptions = Sets.newHashSet(SingleOptions.class);
    private final Set<Class<?>> singleFull = Sets.newHashSet(SingleFull.class);
    
    private void verifySingle(final Map<ResourceIdentifier, Resource> result,
            final String expectedPath,
            final HttpMethod expectedMethod)
    {
        verifySingle(result, expectedPath, expectedMethod, 1);
    }
    
    private void verifySingle(final Map<ResourceIdentifier, Resource> result,
            final String expectedPath,
            final HttpMethod expectedMethod,
            int expectedSize)
    {
        assertNotNull(result);
        assertEquals(expectedSize, result.size());
        
        Resource testResource = null;
        if (1 == expectedSize)
        {
            final ResourceIdentifier ri = result.keySet().iterator().next();
            assertEquals(expectedPath, ri.getPath());
            assertEquals(expectedMethod, ri.getMethod());
            
            testResource = result.get(ri);
        }
        else
        {
            final Set<ResourceIdentifier> ris = result.keySet();
            final ResourceIdentifier ri = new ResourceIdentifier(expectedPath, expectedMethod);
            assertTrue(ris.contains(ri));
            
            testResource = result.get(ri);
        }
        
        assertNotNull(testResource);
    }
    
    private void verifyFull(final Map<ResourceIdentifier, Resource> result)
    {
        final Set<ResourceIdentifier> ris = result.keySet();
        
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/new/", HttpMethod.PUT)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/update/", HttpMethod.POST)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/", HttpMethod.GET)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/single/", HttpMethod.GET)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/", HttpMethod.DELETE)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/single/", HttpMethod.DELETE)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/summary/", HttpMethod.HEAD)));
        assertTrue(ris.contains(new ResourceIdentifier("/single/full/single/summary/", HttpMethod.HEAD)));        
    }
    
    @Test
    public void testScanSingleGet()
    {
        verifySingle(scanner.scan(singleGet), "/single/get/", HttpMethod.GET);
    }
    
    @Test
    public void testScanSinglePost()
    {
        verifySingle(scanner.scan(singlePost), "/single/post/", HttpMethod.POST);
    }
    
    @Test
    public void testScanSinglePut()
    {
        verifySingle(scanner.scan(singlePut), "/single/put/", HttpMethod.PUT);        
    }
    
    @Test
    public void testScanSingleDelete()
    {
        verifySingle(scanner.scan(singleDelete), "/single/delete/", HttpMethod.DELETE);        
    }
    
    @Test
    public void testScanSingleHead()
    {
        verifySingle(scanner.scan(singleHead), "/single/head/", HttpMethod.HEAD);
    }
    
    @Test
    public void testScanSingleOptions()
    {
        verifySingle(scanner.scan(singleOptions), "/single/options/", HttpMethod.OPTIONS);
    }
    
    @Test
    public void testScanSingleFull()
    {
        Map<ResourceIdentifier, Resource> result = scanner.scan(singleFull);
        
        assertNotNull(result);
        assertEquals(8, result.size());
        
        verifyFull(result);
    }
    
    @Test
    public void testScanNonResource()
    {
        Set<Class<?>> resources = Sets.newHashSet(TestDefaultResourceScannerStrategy.class);
        Map<ResourceIdentifier, Resource> result = scanner.scan(resources);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testScanMultiple()
    {
        Set<Class<?>> resources =
                Sets.newHashSet(SingleGet.class,
                        SinglePost.class,
                        SinglePut.class,
                        SingleDelete.class,
                        SingleHead.class,
                        SingleOptions.class,
                        SingleFull.class);
        Map<ResourceIdentifier, Resource> results = scanner.scan(resources);
        
        assertNotNull(results);
        assertEquals(14, results.size());
        
        verifySingle(results, "/single/get/", HttpMethod.GET, 14);
        verifySingle(results, "/single/post/", HttpMethod.POST, 14);
        verifySingle(results, "/single/put/", HttpMethod.PUT, 14);
        verifySingle(results, "/single/delete/", HttpMethod.DELETE, 14);
        verifySingle(results, "/single/head/", HttpMethod.HEAD, 14);
        verifySingle(results, "/single/options/", HttpMethod.OPTIONS, 14);
        
        verifyFull(results);
    }
    
    @Test
    public void testScanMultipleWithNonResources()
    {
        Set<Class<?>> resources =
                Sets.newHashSet(SingleGet.class,
                        SinglePost.class,
                        SinglePut.class,
                        TestDefaultResourceScannerStrategy.class,
                        SingleFull.class);
        Map<ResourceIdentifier, Resource> results = scanner.scan(resources);
        
        assertNotNull(results);
        assertEquals(11, results.size());
        
        verifySingle(results, "/single/get/", HttpMethod.GET, 11);
        verifySingle(results, "/single/post/", HttpMethod.POST, 11);
        verifySingle(results, "/single/put/", HttpMethod.PUT, 11);
        
        verifyFull(results);
    }
    
    @Test
    public void testRememberClassConsumes()
    {
        final Set<Class<?>> s = Sets.newHashSet(ConsumesTestResource.class);
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(s);
        final Resource r = ris.get(new ResourceIdentifier("/ctr/r1", HttpMethod.POST));
        assertTrue(r.consumes(MediaType.APPLICATION_FORM_URLENCODED));
        assertFalse(r.consumes(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testRememberMethodConsumes()
    {
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(singlePost);
        final Resource r = ris.values().iterator().next();
        assertTrue(r.consumes(MediaType.APPLICATION_FORM_URLENCODED));
        assertFalse(r.consumes(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testMethodConsumesExtendsClassConsumes()
    {
        final Set<Class<?>> s = Sets.newHashSet(ConsumesTestResource.class);
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(s);
        final Resource r = ris.get(new ResourceIdentifier("/ctr/r2", HttpMethod.POST));
        assertTrue(r.consumes(MediaType.APPLICATION_FORM_URLENCODED));
        assertTrue(r.consumes(MediaType.APPLICATION_JSON));        
    }
    
    @Test
    public void testAllowMultipleConsumes()
    {
        final Set<Class<?>> s = Sets.newHashSet(ConsumesTestResource.class);
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(s);
        final Resource r = ris.get(new ResourceIdentifier("/ctr/r3", HttpMethod.POST));
        assertTrue(r.consumes(MediaType.APPLICATION_JSON));
        assertTrue(r.consumes(MediaType.APPLICATION_XML));
    }
    
    @Test
    public void testNoConsumesOnGet()
    {
        final Set<Class<?>> s = Sets.newHashSet(ConsumesTestResource.class);
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(s);
        final Resource r = ris.get(new ResourceIdentifier("/ctr/r4", HttpMethod.GET));
        assertFalse(r.consumes(MediaType.APPLICATION_FORM_URLENCODED));        
        assertFalse(r.consumes(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testDefaultConsumesTextPlain()
    {
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(singlePut);
        final Resource r = ris.values().iterator().next();
        assertTrue(r.consumes(MediaType.TEXT_PLAIN));
    }
        
    @Test
    public void testRememberClassProduces()
    {
        final Set<Class<?>> s = Sets.newHashSet(ProducesTestResource.class);
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(s);
        final Resource r = ris.get(new ResourceIdentifier("/ptr/r1", HttpMethod.GET));
        assertTrue(r.produces(MediaType.TEXT_HTML));
        assertFalse(r.produces(MediaType.TEXT_PLAIN));
    }
    
    @Test
    public void testRememberMethodProduces()
    {
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(singlePost);
        final Resource r = ris.values().iterator().next();
        assertTrue(r.produces(MediaType.TEXT_HTML));
        assertFalse(r.produces(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testMethodProducesExtendsClassProduces()
    {
        final Set<Class<?>> s = Sets.newHashSet(ProducesTestResource.class);
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(s);
        final Resource r = ris.get(new ResourceIdentifier("/ptr/r2", HttpMethod.GET));
        assertTrue(r.produces(MediaType.TEXT_HTML));
        assertTrue(r.produces(MediaType.TEXT_XML));        
    }
    
    @Test
    public void testDefaultProducesTextPlain()
    {
        final Map<ResourceIdentifier, Resource> ris = scanner.scan(singleGet);
        final Resource r = ris.values().iterator().next();
        assertTrue(r.produces(MediaType.TEXT_PLAIN));
    }
}
