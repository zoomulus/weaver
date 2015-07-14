package com.zoomulus.weaver.rest.jaxrs;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.PathSegmentImpl;

import lombok.NonNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WeaverUriInfo implements UriInfo
{
    private final String path;
    private final Map<String, PathSegment> pathSegments;
    private final Map<String, String> pathParameters;
    private final Map<String, String> matrixParameters;
    
    private WeaverUriInfo(final String requestPath)
    {
        this(requestPath, Maps.newHashMap(), Maps.newHashMap(), Maps.newHashMap());
    }
    
    private WeaverUriInfo(final String requestPath,
            final Map<String, PathSegment> pathSegments,
            final Map<String, String> pathParameters,
            final Map<String, String> matrixParameters)
    {
        this.path = requestPath.startsWith("/") ? requestPath : String.format("/%s", requestPath);
        this.pathSegments = pathSegments;
        this.pathParameters = pathParameters;
        this.matrixParameters = matrixParameters;
    }
    
    public static Optional<WeaverUriInfo> create(
            @NonNull final String resourcePath,
            final String requestPath)
    {
        final Map<String, String> matrixParams = Maps.newHashMap();

        if (resourcePath.equals(requestPath))
        {
            return Optional.of(new WeaverUriInfo(requestPath));
        }
        
        if (null == requestPath)
        {
            return Optional.empty();
        }
        
        List<String> patternParts = Lists.newArrayList();
        for (final String patternPart : resourcePath.split("/"))
        {
            if (0 != patternPart.length()) patternParts.add(patternPart);
        }
        List<String> pathParts = Lists.newArrayList();
        List<String> pathSegmentParts = Lists.newArrayList();
        for (final String pathPart : requestPath.split("/"))
        {
            if (0 != pathPart.length())
            {
                pathSegmentParts.add(pathPart);
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
        
        final Map<String, String> pathSegmentValues = Maps.newHashMap();
        final Map<String, PathSegment> pathSegments = Maps.newHashMap();
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
                        pathSegments.put(key, new PathSegmentImpl(pathSegmentParts.get(i), false));
                        pathSegmentValues.put(key, pathParts.get(i));
                    }
                    else
                    {
                        return Optional.empty();
                    }
                }
                else
                {
                    final String key = patternPart.substring(1, patternPart.length()-1);
                    pathSegments.put(key, new PathSegmentImpl(pathSegmentParts.get(i), false));
                    pathSegmentValues.put(key, pathParts.get(i));
                }
            }
            else if (! patternPart.equals(pathParts.get(i)))
            {
                return Optional.empty();
            }
        }
        
        return Optional.of(new WeaverUriInfo(requestPath, pathSegments, pathSegmentValues, matrixParams));
    }
    
    @Override
    public String getPath()
    {
        return getPath(true);
    }

    @Override
    public String getPath(boolean decode)
    {
        return path;
    }

    @Override
    public List<PathSegment> getPathSegments()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Optional<PathSegment> getPathSegment(final String pathParameterName)
    {
        return pathSegments.containsKey(pathParameterName) ? Optional.of(pathSegments.get(pathParameterName)) : Optional.empty();
    }

    @Override
    public URI getRequestUri()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UriBuilder getRequestUriBuilder()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI getAbsolutePath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UriBuilder getAbsolutePathBuilder()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI getBaseUri()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UriBuilder getBaseUriBuilder()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String> getPathParameterKeys()
    {
        return pathParameters.keySet();
    }
    
    public Collection<String> getPathParameterValues()
    {
        return pathParameters.values();
    }
    
    public Set<Entry<String, String>> getPathParameterEntries()
    {
        return pathParameters.entrySet();
    }
    
    public Optional<String> getPathParameter(final String key)
    {
        return pathParameters.containsKey(key) ? Optional.of(pathParameters.get(key)) : Optional.empty();
    }
    
    public boolean hasMatrixParameters()
    {
        return ! matrixParameters.isEmpty();
    }
    
    public Set<String> getMatrixParameterKeys()
    {
        return matrixParameters.keySet();
    }
    
    public Optional<String> getMatrixParameter(final String key)
    {
        return matrixParameters.containsKey(key) ? Optional.of(matrixParameters.get(key)) : Optional.empty();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getMatchedURIs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getMatchedURIs(boolean decode)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getMatchedResources()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI resolve(URI uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI relativize(URI uri)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
