package com.zoomulus.weaver.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class PathJoinerTest
{
    private static final String TEST_PATH = "/path/to/resource/";

    @Test
    public void testJoinOne()
    {
        assertEquals("/path/", new PathJoiner().with("/path").join());
    }
    
    @Test
    public void testJoinOneWithoutLeadingSeparator()
    {
        assertEquals("/path/", new PathJoiner().with("path").join());
    }
    
    @Test
    public void testJoinOneWithTrailingSeparator()
    {
        assertEquals("/path/", new PathJoiner().with("/path/").join());
    }
    
    @Test
    public void testJoinOneSeparatorOnly()
    {
        assertEquals("/", new PathJoiner().with("/").join());
    }
    
    @Test
    public void testJoinOneEmptyString()
    {
        assertEquals("/", new PathJoiner().with("").join());
    }
    
    @Test
    public void testJoinOneNullString()
    {
        final String nullString = null;
        assertEquals("/", new PathJoiner().with(nullString).join());
    }
    
    @Test
    public void testJoinTwo()
    {
        assertEquals(TEST_PATH, new PathJoiner().with("/path").with("/to/resource").join());
    }
    
    @Test
    public void testJoinThree()
    {
        assertEquals(TEST_PATH, new PathJoiner().with("/path").with("/to").with("/resource").join());
    }
    
    @Test
    public void testJoinWithoutSeparators()
    {
        assertEquals(TEST_PATH, new PathJoiner().with("/path").with("to").with("resource").join());
    }
    
    @Test
    public void testJoinWithoutLeadingSeparator()
    {
        assertEquals(TEST_PATH, new PathJoiner().with("path").with("/to").with("resource").join());
    }
    
    @Test
    public void testJoinWithTrailingSeparators()
    {
        assertEquals(TEST_PATH, new PathJoiner().with("/path/").with("to/").with("/resource/").join());
    }
    
    @Test
    public void testJoinWithList()
    {
        assertEquals(TEST_PATH, new PathJoiner().with(Lists.newArrayList("path", "to", "resource")).join());
    }
}
