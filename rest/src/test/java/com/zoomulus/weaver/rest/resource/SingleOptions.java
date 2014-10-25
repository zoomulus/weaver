package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;

@Path("single")
public class SingleOptions
{
    @Path("options")
    @OPTIONS
    public String options()
    {
        return null;
    }
}
