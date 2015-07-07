package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.PathSegment;

import lombok.Getter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zoomulus.weaver.core.content.ContentType;
import com.zoomulus.weaver.rest.annotations.RequiredParam;
import com.zoomulus.weaver.rest.annotations.StrictParams;
import com.zoomulus.weaver.rest.content.HttpContent;
import com.zoomulus.weaver.rest.exceptions.StrictParamsMismatchException;

public class ResourceArgs
{
    static private ObjectMapper jsonMapper = new ObjectMapper();
    static private XmlMapper xmlMapper = new XmlMapper();
    
    @Getter
    private final Map<String, List<String>> queryParams;
    @Getter
    private final Map<String, List<String>> formParams;
    @Getter
    private final Object[] args;
    
    private final Method referencedMethod;
    
    private ResourceArgs(final ResourcePath resourcePath,
            final Optional<String> body,
            final Optional<HttpHeaders> headers,
            final Map<String, List<String>> queryParams,
            final Optional<ContentType> contentType,
            final HttpMethod httpMethod,
            final Method referencedMethod) throws JsonParseException, JsonMappingException, InstantiationException,
                IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                IOException, StrictParamsMismatchException
    {
        this.queryParams = queryParams;
        this.formParams = parseFormData(body, contentType, httpMethod);
        this.referencedMethod = referencedMethod;
        
        this.args = populateArgs(referencedMethod, body, headers, contentType, resourcePath, queryParams, formParams);
        performStrictParamsCheck(this.args.length, queryParams.size(), formParams.size());
    }
    
    private Map<String, List<String>> parseFormData(final Optional<String> body,
            final Optional<ContentType> contentType,
            final HttpMethod httpMethod)
    {
        Map<String, List<String>> formParams = Maps.newHashMap();
        
        if (! body.isPresent() || ! contentType.isPresent()) return formParams;
        
        if (! contentType.get().isCompatibleWith(ContentType.APPLICATION_FORM_URLENCODED_TYPE))
            return formParams;
        
        if (HttpMethod.POST == httpMethod ||
                HttpMethod.PUT == httpMethod)
        {
            formParams = new QueryStringDecoder(body.get(), false).parameters();
        }
        
        return formParams;        
    }
    
    private Object[] populateArgs(final Method referencedMethod,
            final Optional<String> body,
            final Optional<HttpHeaders> headers,
            final Optional<ContentType> contentType,
            final ResourcePath resourcePath,
            final Map<String, List<String>> queryParams,
            final Map<String, List<String>> formParams) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JsonParseException, JsonMappingException, IOException
    {
        final List<Object> args = Lists.newArrayList();
        
        Class<?>[] parameterTypes = referencedMethod.getParameterTypes();
        
        int idx = 0;
        for (final Annotation[] paramAnnotations : referencedMethod.getParameterAnnotations())
        {
            if (0 == paramAnnotations.length)
            {
                boolean added = false;
                if (contentType.isPresent() && parameterTypes[idx] != String.class && ! parameterTypes[idx].getName().equals("[B"))
                {
                    if (! body.isPresent())
                    {
                        throw new IllegalArgumentException("Content type specified but no body provided");
                    }
                    
                    final String messageBody = body.get();
                    
                    if (contentType.get().isCompatibleWith(ContentType.APPLICATION_JSON_TYPE))
                    {
                        args.add(jsonMapper.readValue(messageBody, parameterTypes[idx]));
                        added = true;
                    }
                    else if (contentType.get().isCompatibleWith(ContentType.APPLICATION_XML_TYPE))
                    {
                        args.add(xmlMapper.readValue(messageBody, parameterTypes[idx]));
                        added = true;
                    }
                    else if (contentType.get().isCompatibleWith(ContentType.TEXT_PLAIN_TYPE))
                    {
                        if (parameterTypes[idx].isPrimitive())
                        {
                            final Class<?> parameterType = parameterTypes[idx]; 
                            if (parameterType == boolean.class) { args.add(Boolean.valueOf(messageBody)); added=true; }
                            else if (parameterType == byte.class) { args.add(Byte.valueOf(messageBody)); added=true; }
                            else if (parameterType == short.class) { args.add(Short.valueOf(messageBody)); added=true; }
                            else if (parameterType == int.class) { args.add(Integer.valueOf(messageBody)); added=true; }
                            else if (parameterType == long.class) { args.add(Long.valueOf(messageBody)); added=true; }
                            else if (parameterType == float.class) { args.add(Float.valueOf(messageBody)); added=true; }
                            else if (parameterType == double.class) { args.add(Double.valueOf(messageBody)); added=true; }                            
                        }
                        else
                        {
                            Optional<Constructor<?>> ctor = getStringConstructor(parameterTypes[idx]);
                            if (ctor.isPresent())
                            {
                                args.add(ctor.get().newInstance(messageBody));
                                added = true;
                            }
                            else
                            {
                                Optional<Method> valueOf = getValueOfStringMethod(parameterTypes[idx]);
                                if (valueOf.isPresent())
                                {
                                    args.add(valueOf.get().invoke(null, messageBody));
                                    added = true;
                                }
                            }
                        }
                    }
                }
                
                if (! added)
                {
                    if (parameterTypes[idx].getName().equals("[B"))
                    {
                        args.add(body.isPresent() ? body.get().getBytes() : new byte[0] );
                    }
                    else
                    {
                        args.add(body.isPresent() ? body.get() : null);
                    }
                }
            }
            else
            {
                Annotation paramTypeAnnotation = null;
                Annotation defaultValueAnnotation = null;
                Annotation requiredParamAnnotation = null;
                boolean allowNullArg = false;
                for (final Annotation annotation : paramAnnotations)
                {
                    if (annotation instanceof PathParam ||
                            annotation instanceof MatrixParam ||
                            annotation instanceof QueryParam ||
                            annotation instanceof FormParam ||
                            annotation instanceof HeaderParam)
                    {
                        paramTypeAnnotation = annotation;
                    }
                    else if (annotation instanceof DefaultValue)
                    {
                        defaultValueAnnotation = annotation;
                    }
                    else if (annotation instanceof RequiredParam)
                    {
                        requiredParamAnnotation = annotation;
                    }
                }
                
                Class<?> parameterType = parameterTypes[idx];
                String s_arg = null;

                if (null != paramTypeAnnotation)
                {
                    if (paramTypeAnnotation instanceof PathParam)
                    {
                        final String paramValue = ((PathParam) paramTypeAnnotation).value();
                        
                        if (PathSegment.class.isAssignableFrom(parameterType))
                        {
                            Optional<PathSegment> ps = resourcePath.getPathSegment(paramValue);
                            if (ps.isPresent()) args.add(ps.get());
                        }
                        else
                        {
                            s_arg = resourcePath.get(paramValue);
                        }
                    }
                    else if (paramTypeAnnotation instanceof MatrixParam)
                    {
                        s_arg = resourcePath.matrixParamGet(((MatrixParam) paramTypeAnnotation).value());
                    }
                    else if (paramTypeAnnotation instanceof QueryParam ||
                            paramTypeAnnotation instanceof FormParam)
                    {
                        final List<String> params = (paramTypeAnnotation instanceof QueryParam) ?
                                queryParams.get(((QueryParam) paramTypeAnnotation).value()) :
                                formParams.get(((FormParam) paramTypeAnnotation).value());
                        if (null != params && ! params.isEmpty())
                        {
                            if (List.class.isAssignableFrom(parameterType))
                            {
                                args.add(params);
                            }
                            else
                            {
                                s_arg = params.get(0);
                            }
                        }
                        else if (null != defaultValueAnnotation)
                        {
                            s_arg = ((DefaultValue) defaultValueAnnotation).value();
                        }
                        else if (! parameterType.isPrimitive())
                        {
                            allowNullArg = (null == requiredParamAnnotation);
                        }
                    }
                    else if (paramTypeAnnotation instanceof HeaderParam)
                    {
                        if (headers.isPresent())  // Should always be true - most HTTP requests have *some* headers at least
                        {
                            final String paramValue = ((HeaderParam) paramTypeAnnotation).value();
                            if (headers.get().contains(paramValue))
                            {
                                s_arg = headers.get().get(paramValue);
                            }
                        }
                        
                        if (null == s_arg && null != defaultValueAnnotation)
                        {
                            s_arg = ((DefaultValue) defaultValueAnnotation).value();
                        }
                    }
                }
                
                if (null != s_arg)
                {
                    Object arg = getParameterOfMatchingType(parameterType, s_arg);
                    if (null != arg)
                    {
                        args.add(arg);
                    }
                }
                else if (allowNullArg)
                {
                    args.add(null);
                }
            }
            ++idx;
        }
        
        return args.toArray();
    }
    
    private Optional<Constructor<?>> getStringConstructor(final Class<?> klass)
    {
        for (final Constructor<?> ctor : klass.getConstructors())
        {
            final Class<?>[] params = ctor.getParameterTypes();
            if (params.length != 1)
            {
                continue;
            }
            if (params[0] == String.class)
            {
                return Optional.of(ctor);
            }
        }
        return Optional.empty();
    }
    
    private Optional<Method> getValueOfStringMethod(final Class<?> klass)
    {
        try
        {
            return Optional.of(klass.getDeclaredMethod("valueOf", String.class));            
        }
        catch (NoSuchMethodException e) { }
        return Optional.empty();
    }
    
    private Object getParameterOfMatchingType(final Class<?> parameterType, final String s_arg)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object arg = null;
        if (parameterType.isPrimitive())
        {
            if (parameterType == boolean.class) { arg = Boolean.valueOf(s_arg); }
            else if (parameterType == byte.class) { arg = Byte.valueOf(s_arg); }
            else if (parameterType == short.class) { arg = Short.valueOf(s_arg); }
            else if (parameterType == int.class) { arg = Integer.valueOf(s_arg); }
            else if (parameterType == long.class) { arg = Long.valueOf(s_arg); }
            else if (parameterType == float.class) { arg = Float.valueOf(s_arg); }
            else if (parameterType == double.class) { arg = Double.valueOf(s_arg); }
        }
        else
        {
            Optional<Constructor<?>> stringConstructor = getStringConstructor(parameterType);
            if (stringConstructor.isPresent())
            {
                arg = stringConstructor.get().newInstance(s_arg);
            }
            else
            {
                Optional<Method> valueOfStringMethod = getValueOfStringMethod(parameterType);
                if (valueOfStringMethod.isPresent())
                {
                    arg = valueOfStringMethod.get().invoke(null, s_arg);
                }
                else
                {
                    arg = s_arg;
                }
            }
        }
        return arg;
    }
    
    private void performStrictParamsCheck(int nArgs, int nQueryParams, int nFormParams) throws StrictParamsMismatchException
    {
        for (final Annotation annotation : referencedMethod.getAnnotations())
        {
            if (annotation instanceof StrictParams)
            {
                if (nArgs != (nQueryParams + nFormParams))
                {
                    throw new StrictParamsMismatchException(referencedMethod.getDeclaringClass().getName(),
                            referencedMethod.getName(), nArgs, nQueryParams+nFormParams);
                }
                break;
            }
        }
    }

    
    public static ResourceArgsBuilder builder()
    {
        return new ResourceArgsBuilder();
    }
    public static class ResourceArgsBuilder
    {
        private Optional<String> body = Optional.empty();
        private ResourcePath resourcePath = null;
        private Map<String, List<String>> queryParams = Maps.newHashMap();
        private Optional<ContentType> contentType = Optional.empty();
        private Optional<HttpHeaders> headers = Optional.empty();
        private HttpMethod httpMethod = null;
        private Method referencedMethod = null;
        
        public ResourceArgsBuilder resourcePath(final ResourcePath resourcePath)
        {
            this.resourcePath = resourcePath;
            return this;
        }
        
        public ResourceArgsBuilder content(final Optional<HttpContent> content)
        {
            if (content.isPresent())
            {
                body = Optional.of(content.get().getContent());
                contentType = Optional.of(content.get().getContentType());
            }
            return this;
        }
        
        public ResourceArgsBuilder queryParams(final Map<String, List<String>> queryParams)
        {
            this.queryParams.putAll(queryParams);
            return this;
        }
        
        public ResourceArgsBuilder httpMethod(final HttpMethod httpMethod)
        {
            this.httpMethod = httpMethod;
            return this;
        }
        
        public ResourceArgsBuilder httpHeaders(final Optional<HttpHeaders> headers)
        {
            this.headers = headers;
            return this;
        }
        
        public ResourceArgsBuilder referencedMethod(final Method referencedMethod)
        {
            this.referencedMethod = referencedMethod;
            return this;
        }
        
        public ResourceArgs build() throws ResourceArgsBuilderException, JsonParseException, JsonMappingException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            IOException, StrictParamsMismatchException
        {
            if (null == resourcePath)
            {
                throw this.new ResourceArgsBuilderException("ResourcePath required");
            }
            if (null == httpMethod)
            {
                throw this.new ResourceArgsBuilderException("HttpMethod required");
            }
            if (null == referencedMethod)
            {
                throw this.new ResourceArgsBuilderException("Method required");
            }
            return new ResourceArgs(resourcePath,
                    body,
                    headers,
                    queryParams,
                    contentType,
                    httpMethod,
                    referencedMethod);
        }
        
        public class ResourceArgsBuilderException extends Exception
        {
            private static final long serialVersionUID = 1L;
            public ResourceArgsBuilderException(final String message) { super(message); }
        }
    }
}
