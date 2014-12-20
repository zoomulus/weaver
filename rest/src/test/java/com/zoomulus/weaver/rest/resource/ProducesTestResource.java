package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/ptr")
@Produces(MediaType.TEXT_HTML)
public class ProducesTestResource
{
    @GET
    @Path("r1")
    public String r1()
    {
        return "<html><body>r1</body></html>";
    }
    
    @GET
    @Path("r2")
    @Produces(MediaType.TEXT_XML)
    public String r2()
    {
        return "<html><body>r2</body></html>";
    }
}
