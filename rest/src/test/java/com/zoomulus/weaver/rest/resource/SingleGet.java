package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("single")
public class SingleGet
{
    @Path("get")
    @GET
    public String get()
    {
        return "";
    }
}
