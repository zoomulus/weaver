package com.zoomulus.weaver.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.zoomulus.weaver.core.content.ContentType;

@Path("single")
public class SinglePost
{
    @Path("post")
    @POST
    @Consumes(ContentType.APPLICATION_FORM_URLENCODED)
    @Produces(ContentType.TEXT_HTML)
    public String post()
    {
        return "<html><body>hi</body></html>";
    }
}
