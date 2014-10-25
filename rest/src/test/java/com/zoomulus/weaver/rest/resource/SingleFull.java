package com.zoomulus.weaver.rest.resource;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path("/single/full")
public class SingleFull
{
    @Path("new")
    @PUT
    public void insertNewPair(final String key, final String value)
    {
        
    }
    
    @Path("update")
    @POST
    public void updatePair(final String key, final String value)
    {
        
    }

    @GET
    public List<String> getAll()
    {
        return null;
    }
    
    @Path("single")
    @GET
    public String get(final String key)
    {
        return null;
    }
    
    @DELETE
    public void deleteAll()
    {

    }
    
    @Path("single")
    @DELETE
    public void delete(final String key)
    {
        
    }
    
    @Path("summary")
    @HEAD
    public String getAllSummary()
    {
        return null;
    }
    
    @Path("single/summary")
    @HEAD
    public String getSummary()
    {
        return null;
    }
}
