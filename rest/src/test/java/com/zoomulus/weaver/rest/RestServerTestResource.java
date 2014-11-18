package com.zoomulus.weaver.rest;

import javax.ws.rs.GET;
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
}
