package com.zoomulus.weaver.rest;

import static io.netty.buffer.Unpooled.copiedBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Maps;
import com.zoomulus.weaver.rest.resource.DefaultResourceScannerStrategy;
import com.zoomulus.weaver.rest.resource.Resource;
import com.zoomulus.weaver.rest.resource.ResourceIdentifier;
import com.zoomulus.weaver.rest.resource.ResourcePath;

public class RestHandler extends ChannelInboundHandlerAdapter
{
    Optional<HttpRequest> request = Optional.empty();
    Optional<HttpHeaders> headers = Optional.empty();
    Map<String, List<String>> queryParams = Maps.newHashMap();
    
    final Set<Class<?>> resourceClasses;
    final Map<ResourceIdentifier, Resource> resources;
    
    Optional<Resource> handlingResource = Optional.empty();
    Optional<ResourcePath> handlingResourcePath = Optional.empty();
    StringBuilder buffer = new StringBuilder();
    
    public RestHandler(final Set<Class<?>> resourceClasses)
    {
        this.resourceClasses = resourceClasses;
        this.resources = new DefaultResourceScannerStrategy().scan(resourceClasses);
    }
    
    protected boolean is100ContinueExpected()
    {
        return request.isPresent() ? HttpHeaders.is100ContinueExpected(request.get()) : false;
    }
    
    protected boolean isSupportedHttpVersion()
    {
        return request.isPresent() ? request.get().getProtocolVersion() == HttpVersion.HTTP_1_1 : true;
    }
    
    protected boolean isHttpKeepaliveRequest()
    {
        return request.isPresent() ? HttpHeaders.isKeepAlive(request.get()) : false;
    }
    
    protected boolean isAllowedHost(final Optional<String> host)
    {
        return true;
    }
    
    protected Optional<Resource> parseResource(final HttpMethod method, final String requestPath)
    {
        Optional<Resource> resource = Optional.empty();
        for (final Entry<ResourceIdentifier, Resource> entry : resources.entrySet())
        {
            Optional<ResourcePath> rp =
                    ResourcePath
                        .withPattern(entry.getKey().getPath())
                        .parse(requestPath);
            if (rp.isPresent() && entry.getKey().getMethod() == method)
            {
                handlingResourcePath = rp;
                resource = Optional.of(entry.getValue());
            }
        }
        return resource;
    }
    
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx)
    {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception
    {
        if (msg instanceof HttpRequest)
        {
            HttpRequest request = (HttpRequest) msg;
            this.request = Optional.of(request);
            
            if (is100ContinueExpected())
            {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            }
            
            HttpHeaders headers = request.headers();
            this.headers = Optional.of(headers);
            
            final Optional<String> host = Optional.ofNullable(headers.get("Host"));
            if (! isSupportedHttpVersion())
            {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_0,
                        HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED,
                        copiedBuffer("HTTP 1.1 Required".getBytes())));
            }
            else if (! isAllowedHost(host))
            {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.FORBIDDEN));
            }
            else
            {
                handlingResource = parseResource(request.getMethod(), request.getUri().split("\\?")[0]);
                
                //if (! haveMatchingResource(resourceIdentifier))
                if (! handlingResource.isPresent() || ! handlingResourcePath.isPresent())
                {
                    FullHttpResponse fullRsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.NOT_FOUND);
                    
                    fullRsp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fullRsp.content().readableBytes());
                    if (isHttpKeepaliveRequest())
                    {
                        fullRsp.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    }
                    
                    ctx.writeAndFlush(fullRsp);
                }
                else
                {
                    queryParams = new QueryStringDecoder(request.getUri()).parameters();
                }
            }
        }
        else if (msg instanceof HttpContent)
        {
            // extract payload
            HttpContent httpContent = (HttpContent) msg;
            if (httpContent.content().isReadable())
            {
               buffer.append(httpContent.content().toString(CharsetUtil.UTF_8));
            }

            if (msg instanceof LastHttpContent)
            {
                // invoke the handler
                if (handlingResource.isPresent() && handlingResourcePath.isPresent())
                {
                    FullHttpResponse fullRsp = null;
                    
                    try
                    {
                        Response rsp = handlingResource.get().invoke(buffer.toString(),
                                handlingResourcePath.get(),
                                this.headers,
                                queryParams);
                        if (null != rsp.getEntity())
                        {
                            final String entity = (rsp.getEntity() instanceof String ? (String) rsp.getEntity() : rsp.getEntity().toString());
                            fullRsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.valueOf(rsp.getStatus()),
                                            copiedBuffer(entity.getBytes()));
                            fullRsp.headers().set("Content-Type", (null != rsp.getMediaType() ? rsp.getMediaType().toString() : MediaType.TEXT_PLAIN));
                        }
                        else
                        {
                            fullRsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                    HttpResponseStatus.valueOf(rsp.getStatus()));
                        }
                    }
                    catch (Exception e)
                    {
                        final StringBuilder sb =  new StringBuilder();
                        for (final StackTraceElement ste : e.getStackTrace())
                        {
                            sb.append(ste.toString());
                        }
                        fullRsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                        copiedBuffer(sb.toString().getBytes()));
                    }
                    
                    fullRsp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fullRsp.content().readableBytes());
                    if (isHttpKeepaliveRequest())
                    {
                        fullRsp.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    }
                    
                    ctx.writeAndFlush(fullRsp);
                    
                    buffer = new StringBuilder();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception
    {
        // TODO Auto-generated method stub

    }
}
