package com.zoomulus.weaver.rest.jaxrs;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
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

import lombok.NonNull;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.PathSegmentImpl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WeaverUriInfo implements UriInfo
{
    private final URI baseUri;
    private final URI requestUri;
    private final Map<String, PathSegment> pathSegments;
    private final List<PathSegment> orderedPathSegmentValues;
    private final List<PathSegment> orderedDecodedPathSegmentValues;
    private final MultivaluedMap<String, String> pathParameters;
    private final Map<String, String> matrixParameters;
    private final MultivaluedMap<String, String> queryParameters;
    
    private WeaverUriInfo(
            final URI requestUri,
            final Map<String, PathSegment> pathSegments,
            final List<PathSegment> orderedPathSegmentValues,
            final List<PathSegment> orderedDecodedPathSegmentValues,
            final MultivaluedMap<String, String> pathParameters,
            final Map<String, String> matrixParameters)
    {
        this.baseUri = requestUri.resolve("/");
        this.requestUri = requestUri;
        this.orderedPathSegmentValues = orderedPathSegmentValues;
        this.orderedDecodedPathSegmentValues = orderedDecodedPathSegmentValues;
        this.pathSegments = pathSegments;
        this.pathParameters = pathParameters;
        this.matrixParameters = matrixParameters;
        queryParameters = decodeQueryParameters(requestUri);
    }
    
    private MultivaluedMap<String, String> decodeQueryParameters(final URI requestUri)
    {
        // TODO: this could be optimized
        final String query = requestUri.getRawQuery();
        final MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
        if (null == query) return params;
        
        for (final String queryPair : query.split("&"))
        {
            final String[] kv = queryPair.split("=", 2);
            String value = "";
            if (2 == kv.length)
            {
                value = kv[1];
            }
            List<String> values = params.get(kv[0]);
            if (null == values)
            {
                values = Lists.newArrayList(value);
            }
            else
            {
                values.add(value);
            }
            params.put(kv[0], values);
        }
        return params;
    }
    
    public static Optional<WeaverUriInfo> create(
            @NonNull final String resourcePath,
            final HttpRequest request)
    {
        if (null == request)
        {
            return Optional.empty();
        }
        
        final Map<String, String> matrixParams = Maps.newHashMap();
        final String hostname = request.headers().get("Host");
        final String requestPath = request.getUri();
        URI requestUri = URI.create(String.format("http://%s/", hostname != null ? hostname : "localhost")).resolve(requestPath);
        
        if (resourcePath.equals(requestUri.getPath()))
        {
            return Optional.of(new WeaverUriInfo(
                    requestUri,
                    Maps.newHashMap(),
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    new MultivaluedMapImpl<String, String>(),
                    Maps.newHashMap()
            ));
        }
        
        if (Strings.isNullOrEmpty(requestUri.getPath()))
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
        
        final List<PathSegment> orderedPathSegmentValues = Lists.newArrayList();
        final List<PathSegment> orderedDecodedPathSegmentValues = Lists.newArrayList();
        final MultivaluedMap<String, String> pathSegmentValues = new MultivaluedMapImpl<String, String>();
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
                        final PathSegment ps = new PathSegmentImpl(pathSegmentParts.get(i), false);
                        pathSegments.put(key, ps);
                        pathSegmentValues.put(key, Lists.newArrayList(ps.getPath()));
                        orderedPathSegmentValues.add(ps);
                        orderedDecodedPathSegmentValues.add(new PathSegmentImpl(pathSegmentParts.get(i), true));
                    }
                    else
                    {
                        return Optional.empty();
                    }
                }
                else
                {
                    final String key = patternPart.substring(1, patternPart.length()-1);
                    final PathSegment ps = new PathSegmentImpl(pathSegmentParts.get(i), false);
                    pathSegments.put(key, ps);
                    pathSegmentValues.put(key, Lists.newArrayList(ps.getPath()));
                    orderedPathSegmentValues.add(ps);
                    orderedDecodedPathSegmentValues.add(new PathSegmentImpl(pathSegmentParts.get(i), true));
                }
            }
            else if (! patternPart.equals(pathParts.get(i)))
            {
                return Optional.empty();
            }
            else
            {
                final PathSegment ps = new PathSegmentImpl(pathSegmentParts.get(i), false);
                pathSegments.put(patternPart, ps);
                pathSegmentValues.put(patternPart, Lists.newArrayList(ps.getPath()));
                orderedPathSegmentValues.add(ps);
                orderedDecodedPathSegmentValues.add(new PathSegmentImpl(pathSegmentParts.get(i), true));
            }
        }
        
        return Optional.of(new WeaverUriInfo(
                requestUri,
                pathSegments,
                orderedPathSegmentValues,
                orderedDecodedPathSegmentValues,
                pathSegmentValues,
                matrixParams));
    }
    
    @Override
    public String getPath()
    {
        return getPath(true);
    }

    @Override
    public String getPath(boolean decode)
    {
        return decode ? requestUri.getPath() : requestUri.getRawPath();
    }

    @Override
    public List<PathSegment> getPathSegments()
    {
        return getPathSegments(true);
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode)
    {
        return decode ? orderedDecodedPathSegmentValues : orderedPathSegmentValues;
    }
    
    public Optional<PathSegment> getPathSegment(final String pathParameterName)
    {
        return pathSegments.containsKey(pathParameterName) ? Optional.of(pathSegments.get(pathParameterName)) : Optional.empty();
    }
    
    @Override
    public URI getRequestUri()
    {
        return requestUri;
    }

    @Override
    public UriBuilder getRequestUriBuilder()
    {
        return UriBuilder.fromUri(getRequestUri());
    }

    @Override
    public URI getAbsolutePath()
    {
        return getBaseUri().resolve(getPath(false));
    }

    @Override
    public UriBuilder getAbsolutePathBuilder()
    {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    @Override
    public URI getBaseUri()
    {
        return baseUri;
    }

    @Override
    public UriBuilder getBaseUriBuilder()
    {
        return UriBuilder.fromUri(getBaseUri());
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters()
    {
        return getPathParameters(true);
    }
    
    private MultivaluedMap<String, String> getDecodedMultivaluedMap(final MultivaluedMap<String, String> encodedMap)
    {
        final MultivaluedMap<String, String> decodedMap = new MultivaluedMapImpl<String, String>();
        for (final Entry<String, List<String>> entry : encodedMap.entrySet())
        {
            final List<String> decodedParamValues = Lists.newArrayList();
            for (final String value : entry.getValue())
            {
                try
                {
                    decodedParamValues.add(URLDecoder.decode(value, CharsetUtil.UTF_8.name()));
                }
                catch (UnsupportedEncodingException e)
                {
                    decodedParamValues.add(value);
                }
            }
            decodedMap.put(entry.getKey(), decodedParamValues);
        }
        return decodedMap;
    }
    
    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode)
    {
        return decode ? getDecodedMultivaluedMap(pathParameters) : pathParameters;
    }

    public Set<String> getPathParameterKeys()
    {
        return pathParameters.keySet();
    }
    
    public Collection<List<String>> getPathParameterValues()
    {
        return pathParameters.values();
    }
    
    public Set<Entry<String, List<String>>> getPathParameterEntries()
    {
        return pathParameters.entrySet();
    }
    
    public Optional<List<String>> getPathParameter(final String key)
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
        return getQueryParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode)
    {
        return decode ? getDecodedMultivaluedMap(queryParameters) : queryParameters;
    }
    
    private Optional<String> getQueryParameterString()
    {
        final List<String> params = Lists.newArrayList();
        for (final Entry<String, List<String>> entry : getQueryParameters().entrySet())
        {
            final String k = entry.getKey();
            for (final String v : entry.getValue())
            {
                params.add(String.format("%s=%s", k, v));
            }
        }
        if (params.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(String.format("?%s", String.join("&", params)));
    }

    @Override
    public List<String> getMatchedURIs()
    {
        return getMatchedURIs(true);
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
        return getBaseUri().resolve(uri);
    }

    @Override
    public URI relativize(URI uri)
    {
        return getBaseUri().relativize(uri);
    }
}
