package com.zoomulus.weaver.core.util;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PathJoiner
{
    final List<String> pathElements = Lists.newArrayList();
    
    public PathJoiner with(final String pathElement)
    {
        if (! Strings.isNullOrEmpty(pathElement) && ! pathElement.equals("/"))
        {
            pathElements.add(pathElement.startsWith("/") ?
                    (pathElement.endsWith("/") ? pathElement.substring(0, pathElement.length()-1)
                            : pathElement) :
                    (pathElement.endsWith("/") ? "/" + pathElement.substring(0, pathElement.length()-1)
                            : "/" + pathElement));
        }
        return this;
    }
    
    public String join()
    {
        final StringBuilder builder = new StringBuilder();
        
        for (final String element : pathElements)
        {
            builder.append(element);
        }
        
        builder.append("/");
        String result = builder.toString();
        //if (0 == result.length()) result = "/";
        
        return result;
    }
}
