package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/ctr")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class ConsumesTestResource
{
    @POST
    @Path("/r1")
    public void r1() { }
    
    @POST
    @Path("/r2")
    @Consumes(MediaType.APPLICATION_JSON)
    public void r2() { }
    
    @POST
    @Path("/r3")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void r3() { }
    
    @GET
    @Path("/r4")
    @Consumes(MediaType.APPLICATION_JSON)
    public void r4() { }
}
