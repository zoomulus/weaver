package com.zoomulus.weaver.rest.resource;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class Resource
{
    Class<?> referencedClass;
    Method referencedMethod;
    String path;
    HttpMethod httpMethod;
    
    // @Consumes / @Produces
    // Single unnamed parameter - body (input stream, byte array, or String - depending on content type?)
    // @PathParam w/ @Path e.g. @Path("{id"}), @PathParam("id") int id
    //  regex matching also, e.g. @Path("{id : \\d+}")
    //  Also handle matrix params, e.g. @Path("{/e55/{year}") with uri path /e55;color=black/2006
    //  would match year as 2006
    // Handle javax.ws.rs.WebApplicationException (chap 7)
    // Return type - javax.ws.rs.core.StreamingOutput
    //  Support other types (OutputStream, byte array, String, or JSON-serializable object) automatically
    //  sent to StreamingOutput
    // Support providing annotations on an interface, not implementation
    // Should work on subclasses also, but subclass must still have @Path annotation
    // Support all injected parameter types:
    //  - PathParam
    //  - MatrixParam
    //  - QueryParam
    //  - FormParam
    //  - HeaderParam
    //  - CookieParam
    //  - BeanParam
    //  - Context
    //  - DefaultValue
    //  - Encoded
    // Support string conversion to native types, objects with single String constructor param,
    //  objects with valueOf(String), containers of objects matching the above, or Optional?
    // Support ParamConverter<T>
    
    public Response invoke()
    {
        return Response.status(Status.OK).entity("from Resource class").build();
    }
}
