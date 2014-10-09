package com.zoomulus.weaver.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
public class TestResource
{
    @GET
    @Path("test")
    public Response getTest() {
        return Response.status(Status.OK).entity("Test endpoint called").build();
    }
}
