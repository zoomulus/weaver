package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.HEAD;
import javax.ws.rs.Path;

@Path("single")
public class SingleHead
{
    @Path("head")
    @HEAD
    public String head()
    {
        return "";
    }
}
