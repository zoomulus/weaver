package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("single")
public class SinglePost
{
    @Path("post")
    @POST
    public void post()
    {
        
    }
}
