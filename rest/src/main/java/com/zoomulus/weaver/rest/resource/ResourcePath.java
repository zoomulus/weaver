package com.zoomulus.weaver.rest.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//@EqualsAndHashCode(exclude={"pattern"})
public class ResourcePath
{
//    private final String pattern;
    private final String path;
    private final Map<String, String> values;
    private final Map<String, String> matrixParams;
    
    private ResourcePath(final String path)
    {
        this(path, Maps.newHashMap(), Maps.newHashMap());
    }
    
    private ResourcePath(final String path, final Map<String, String> values, final Map<String, String> matrixParams)
    {
        this.path = path;
        this.values = values;
        this.matrixParams = matrixParams;
    }
    
    public String toString()
    {
        return path;
    }
    
    public Set<String> keySet()
    {
        return values.keySet();
    }
    
    public Collection<String> values()
    {
        return values.values();
    }
    
    public Set<Entry<String, String>> entrySet()
    {
        return values.entrySet();
    }
    
    public String get(final String key)
    {
        return values.get(key);
    }
    
    public boolean hasMatrixParams()
    {
        return ! matrixParams.isEmpty();
    }
    
    public Set<String> matrixParamKeySet()
    {
        return matrixParams.keySet();
    }
    
    public String matrixParamGet(final String key)
    {
        return matrixParams.get(key);
    }
    
    public static ResourcePathParser withPattern(final String pattern)
    {
        return new ResourcePathParser(pattern);
    }
    
    @RequiredArgsConstructor
    public static class ResourcePathParser
    {
        @NonNull final String pattern;
        
        private String keyFromRawPlaceholder(final String w)
        {
            return w.substring(1, w.length()-1);
        }
        
        public Optional<ResourcePath> parse(final String path)
        {
            final Map<String, String> matrixParams = Maps.newHashMap();
            
            if (pattern.equals(path))
            {
                return Optional.of(new ResourcePath(path));
            }
            
            if (null == path)
            {
                return Optional.empty();
            }
            
            List<String> patternParts = Lists.newArrayList();
            for (final String patternPart : pattern.split("/"))
            {
                if (0 != patternPart.length()) patternParts.add(patternPart);
            }
            List<String> pathParts = Lists.newArrayList();
            for (final String pathPart : path.split("/"))
            {
                if (0 != pathPart.length())
                {
                    String[] ppParts = pathPart.split(";");
                    pathParts.add(ppParts[0]);
                    if (ppParts.length > 1)
                    {
                        String[] matrixParamParts = ppParts[1].split("=", 2);
                        if (matrixParamParts.length >= 2)
                        {
                            matrixParams.put(matrixParamParts[0], matrixParamParts[1]);
                        }
                    }
                }
            }
            
            int len = patternParts.size();
            if (len != pathParts.size())
            {
                return Optional.empty();
            }
            
            final Map<String, String> values = Maps.newHashMap();
            for (int i=0; i<len; i++)
            {
                final String patternPart = patternParts.get(i);
                if (patternPart.startsWith("{") && patternPart.endsWith("}"))
                {
                    final String[] wcParts = patternPart.split(":");
                    if (wcParts.length > 1)
                    {
                        // contains a regex
                        final String key = wcParts[0].substring(1, wcParts[0].length()).trim();
                        final String regex = wcParts[1].substring(0, wcParts[1].length()-1).trim();
                        if (pathParts.get(i).matches(regex))
                        {
                            values.put(key,  pathParts.get(i));
                        }
                        else
                        {
                            return Optional.empty();
                        }
                    }
                    else
                    {
                        values.put(keyFromRawPlaceholder(patternPart), pathParts.get(i));
                    }
                }
                else if (! patternPart.equals(pathParts.get(i)))
                {
                    return Optional.empty();
                }
            }
            
            return Optional.of(new ResourcePath(path, values, matrixParams));
        }
    }
}
