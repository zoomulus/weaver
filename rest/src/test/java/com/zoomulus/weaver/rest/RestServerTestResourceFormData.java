package com.zoomulus.weaver.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/form")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RestServerTestResourceFormData
{
    @POST
    @Path("/post/single")
    public Response postSingle(@FormParam("p1") final String p1)
    {
        return Response.status(Status.OK).entity("p1="+p1).build();
    }
}
