package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.zoomulus.weaver.core.content.ContentType;

@Path("/ctr")
@Consumes(ContentType.APPLICATION_FORM_URLENCODED)
public class ConsumesTestResource
{
    @POST
    @Path("/r1")
    public void r1() { }
    
    @POST
    @Path("/r2")
    @Consumes(ContentType.APPLICATION_JSON)
    public void r2() { }
    
    @POST
    @Path("/r3")
    @Consumes({ContentType.APPLICATION_JSON, ContentType.APPLICATION_XML})
    public void r3() { }
    
    @GET
    @Path("/r4")
    @Consumes(ContentType.APPLICATION_JSON)
    public void r4() { }
}
