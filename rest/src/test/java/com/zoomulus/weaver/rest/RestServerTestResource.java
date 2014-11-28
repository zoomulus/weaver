package com.zoomulus.weaver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
public class RestServerTestResource
{
    @GET
    @Path("get")
    public Response get()
    {
        return Response.status(Status.OK).entity("get").build();
    }
    
    @GET
    @Path("get/id/{id}")
    public Response getId(@PathParam("id") String id)
    {
        return Response.status(Status.OK).entity("id:" + id).build();
    }
    
    @GET
    @Path("get/idmatch/{id: \\d{5}}")
    public Response getFiveDigitId(@PathParam("id") String id)
    {
        return Response.status(Status.OK).entity("id:" + id).build();
    }
    
    @GET
    @Path("get/multiple/{first: [a-z]{3}[0-9]{3}}/{second: [0-9]{3}[a-z]{3}}")
    public Response getMultipleMatches(@PathParam("second") final String second,
            @PathParam("first") final String first)
    {
        return Response.status(Status.OK).entity("second:"+second+",first:"+first).build();
    }
    
    @GET
    @Path("get/multiple/first/{id}/second/{id}")
    public Response getLastRepeatedId(@PathParam("id") final String id)
    {
        return Response.status(Status.OK).entity("id:" + id).build();
    }
    
    @GET
    @Path("get/typematch/boolean/{booleanval}")
    public Response getBooleanMatch(@PathParam("booleanval") boolean b)
    {
        return Response.status(Status.OK).entity(Boolean.toString(b)).build();
    }
    
    @GET
    @Path("get/typematch/byte/{byteval}")
    public Response getByteMatch(@PathParam("byteval") byte b)
    {
        return Response.status(Status.OK).entity(Byte.toString(b)).build();
    }
    
    @GET
    @Path("get/typematch/short/{shortval}")
    public Response getShortMatch(@PathParam("shortval") short i)
    {
        return Response.status(Status.OK).entity(Short.toString(i)).build();
    }
    
    @GET
    @Path("get/typematch/int/{intval}")
    public Response getIntMatch(@PathParam("intval") int i)
    {
        return Response.status(Status.OK).entity(Integer.toString(i)).build();
    }
    
    @GET
    @Path("get/typematch/long/{longval}")
    public Response getLongMatch(@PathParam("longval") long l)
    {
        return Response.status(Status.OK).entity(Long.toString(l)).build();
    }
    
    @GET
    @Path("get/typematch/float/{floatval}")
    public Response getFloatMatch(@PathParam("floatval") float f)
    {
        return Response.status(Status.OK).entity(Float.toString(f)).build();
    }
    
    @GET
    @Path("get/typematch/double/{doubleval}")
    public Response getDoubleMatch(@PathParam("doubleval") double d)
    {
        return Response.status(Status.OK).entity(Double.toString(d)).build();
    }
    
    @GET
    @Path("get/typematch/Integer/{intval}")
    public Response getIntegerMatch(@PathParam("intval") final Integer i)
    {
        return Response.status(Status.OK).entity(i).build();
    }
    
    @GET
    @Path("get/typematch/customwithstringctor/{value}")
    public Response getCustomWithStringCtorMatch(@PathParam("value") final CustomWithStringCtor c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/typematch/customvalueofstring/{value}")
    public Response getCustomValueOfStringMatch(@PathParam("value") final CustomValueOfString c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/typematch/custominvalid/{value}")
    public Response getCustomInvalidMatch(@PathParam("value") final CustomInvalid c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/matrix/single/{id: \\d{5}}")
    public Response getMatrixParamSingle(@PathParam("id") final String id, @MatrixParam("name") final String name)
    {
        return Response.status(Status.OK).entity(String.format("id:%s,name:%s", id, name)).build();
    }
    
    @GET
    @Path("get/matrix/multiple/{p1: first}/{p2: second}")
    public Response getMatrixParamMultiple(@PathParam("p1") final String p1,
            @MatrixParam("1") final String one,
            @MatrixParam("two") int two,
            @PathParam("p2") final String p2)
    {
        return Response.status(Status.OK).entity(String.format("p1:%s,n:%s;p2:%s,n:%d", p1, one, p2, two)).build();
    }
    
    @GET
    @Path("get/matrix/multiple/rep/{rep1}/{rep2}")
    public Response getMatrixParamRepeated(@MatrixParam("var") final String name)
    {
        return Response.status(Status.OK).entity("var:" + name).build();
    }
    
    @GET
    @Path("get/matrix/typematch/boolean/{booleanval}")
    public Response getBooleanMatrixMatch(@MatrixParam("var") boolean b)
    {
        return Response.status(Status.OK).entity(Boolean.toString(b)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/byte/{byteval}")
    public Response getByteMatrixMatch(@MatrixParam("var") byte b)
    {
        return Response.status(Status.OK).entity(Byte.toString(b)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/short/{shortval}")
    public Response getShortMatrixMatch(@MatrixParam("var") short i)
    {
        return Response.status(Status.OK).entity(Short.toString(i)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/int/{intval}")
    public Response getIntMatrixMatch(@MatrixParam("var") int i)
    {
        return Response.status(Status.OK).entity(Integer.toString(i)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/long/{longval}")
    public Response getLongMatrixMatch(@MatrixParam("var") long l)
    {
        return Response.status(Status.OK).entity(Long.toString(l)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/float/{floatval}")
    public Response getFloatMatrixMatch(@MatrixParam("var") float f)
    {
        return Response.status(Status.OK).entity(Float.toString(f)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/double/{doubleval}")
    public Response getDoubleMatrixMatch(@MatrixParam("var") double d)
    {
        return Response.status(Status.OK).entity(Double.toString(d)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/Integer/{intval}")
    public Response getIntegerMatrixMatch(@MatrixParam("var") final Integer i)
    {
        return Response.status(Status.OK).entity(i).build();
    }
    
    @GET
    @Path("get/matrix/typematch/customwithstringctor/{value}")
    public Response getCustomMatrixParamWithStringCtorMatch(@MatrixParam("var") final CustomWithStringCtor c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/matrix/typematch/customvalueofstring/{value}")
    public Response getCustomMatrixParamValueOfStringMatch(@MatrixParam("var") final CustomValueOfString c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/matrix/typematch/custominvalid/{value}")
    public Response getCustomMatrixParamInvalidMatch(@MatrixParam("var") final CustomInvalid c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
}
