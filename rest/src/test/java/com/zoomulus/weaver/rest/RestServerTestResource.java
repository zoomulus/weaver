package com.zoomulus.weaver.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Path("/")
public class RestServerTestResource
{
    @GET
    @Path("get")
    public Response get()
    {
        return Response.status(Status.OK).entity("get").build();
    }
    
    @POST
    @Path("post")
    public Response post()
    {
        return Response.status(Status.OK).entity("post").build();
    }
    
    @PUT
    @Path("put")
    public Response put()
    {
        return Response.status(Status.OK).entity("put").build();
    }
    
    @DELETE
    @Path("delete")
    public Response delete()
    {
        return Response.status(Status.OK).entity("delete").build();
    }
    
    @GET
    @Path("get/id/{id}")
    public Response getId(@PathParam("id") String id)
    {
        return Response.status(Status.OK).entity("id:" + id).build();
    }
    
    @GET
    @Path("get/idmatch/{id: \\d{5}}")
    public Response getFiveDigitId(@PathParam("id") String id)
    {
        return Response.status(Status.OK).entity("id:" + id).build();
    }
    
    @GET
    @Path("get/multiple/{first: [a-z]{3}[0-9]{3}}/{second: [0-9]{3}[a-z]{3}}")
    public Response getMultipleMatches(@PathParam("second") final String second,
            @PathParam("first") final String first)
    {
        return Response.status(Status.OK).entity("second:"+second+",first:"+first).build();
    }
    
    @GET
    @Path("get/multiple/first/{id}/second/{id}")
    public Response getLastRepeatedId(@PathParam("id") final String id)
    {
        return Response.status(Status.OK).entity("id:" + id).build();
    }
    
    @GET
    @Path("get/typematch/boolean/{booleanval}")
    public Response getBooleanMatch(@PathParam("booleanval") boolean b)
    {
        return Response.status(Status.OK).entity(Boolean.toString(b)).build();
    }
    
    @GET
    @Path("get/typematch/byte/{byteval}")
    public Response getByteMatch(@PathParam("byteval") byte b)
    {
        return Response.status(Status.OK).entity(Byte.toString(b)).build();
    }
    
    @GET
    @Path("get/typematch/short/{shortval}")
    public Response getShortMatch(@PathParam("shortval") short i)
    {
        return Response.status(Status.OK).entity(Short.toString(i)).build();
    }
    
    @GET
    @Path("get/typematch/int/{intval}")
    public Response getIntMatch(@PathParam("intval") int i)
    {
        return Response.status(Status.OK).entity(Integer.toString(i)).build();
    }
    
    @GET
    @Path("get/typematch/long/{longval}")
    public Response getLongMatch(@PathParam("longval") long l)
    {
        return Response.status(Status.OK).entity(Long.toString(l)).build();
    }
    
    @GET
    @Path("get/typematch/float/{floatval}")
    public Response getFloatMatch(@PathParam("floatval") float f)
    {
        return Response.status(Status.OK).entity(Float.toString(f)).build();
    }
    
    @GET
    @Path("get/typematch/double/{doubleval}")
    public Response getDoubleMatch(@PathParam("doubleval") double d)
    {
        return Response.status(Status.OK).entity(Double.toString(d)).build();
    }
    
    @GET
    @Path("get/typematch/Integer/{intval}")
    public Response getIntegerMatch(@PathParam("intval") final Integer i)
    {
        return Response.status(Status.OK).entity(i).build();
    }
    
    @GET
    @Path("get/typematch/customwithstringctor/{value}")
    public Response getCustomWithStringCtorMatch(@PathParam("value") final CustomWithStringCtor c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/typematch/customvalueofstring/{value}")
    public Response getCustomValueOfStringMatch(@PathParam("value") final CustomValueOfString c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/typematch/custominvalid/{value}")
    public Response getCustomInvalidMatch(@PathParam("value") final CustomInvalid c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/pathsegment/{ps}")
    public Response getPathSegment(@PathParam("ps") final PathSegment ps)
    {
        return Response.status(Status.OK).entity(String.format("pp:%s;kval:%s,jval:%s",
                ps.getPath(),
                ps.getMatrixParameters().getFirst("k"),
                ps.getMatrixParameters().getFirst("j"))).build();
    }
    
    @GET
    @Path("get/matrix/single/{id: \\d{5}}")
    public Response getMatrixParamSingle(@PathParam("id") final String id, @MatrixParam("name") final String name)
    {
        return Response.status(Status.OK).entity(String.format("id:%s,name:%s", id, name)).build();
    }
    
    @GET
    @Path("get/matrix/multiple/{p1: first}/{p2: second}")
    public Response getMatrixParamMultiple(@PathParam("p1") final String p1,
            @MatrixParam("1") final String one,
            @MatrixParam("two") int two,
            @PathParam("p2") final String p2)
    {
        return Response.status(Status.OK).entity(String.format("p1:%s,n:%s;p2:%s,n:%d", p1, one, p2, two)).build();
    }
    
    @GET
    @Path("get/matrix/multiple/rep/{rep1}/{rep2}")
    public Response getMatrixParamRepeated(@MatrixParam("var") final String name)
    {
        return Response.status(Status.OK).entity("var:" + name).build();
    }
    
    @GET
    @Path("get/matrix/typematch/boolean/{booleanval}")
    public Response getBooleanMatrixMatch(@MatrixParam("var") boolean b)
    {
        return Response.status(Status.OK).entity(Boolean.toString(b)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/byte/{byteval}")
    public Response getByteMatrixMatch(@MatrixParam("var") byte b)
    {
        return Response.status(Status.OK).entity(Byte.toString(b)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/short/{shortval}")
    public Response getShortMatrixMatch(@MatrixParam("var") short i)
    {
        return Response.status(Status.OK).entity(Short.toString(i)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/int/{intval}")
    public Response getIntMatrixMatch(@MatrixParam("var") int i)
    {
        return Response.status(Status.OK).entity(Integer.toString(i)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/long/{longval}")
    public Response getLongMatrixMatch(@MatrixParam("var") long l)
    {
        return Response.status(Status.OK).entity(Long.toString(l)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/float/{floatval}")
    public Response getFloatMatrixMatch(@MatrixParam("var") float f)
    {
        return Response.status(Status.OK).entity(Float.toString(f)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/double/{doubleval}")
    public Response getDoubleMatrixMatch(@MatrixParam("var") double d)
    {
        return Response.status(Status.OK).entity(Double.toString(d)).build();
    }
    
    @GET
    @Path("get/matrix/typematch/Integer/{intval}")
    public Response getIntegerMatrixMatch(@MatrixParam("var") final Integer i)
    {
        return Response.status(Status.OK).entity(i).build();
    }
    
    @GET
    @Path("get/matrix/typematch/customwithstringctor/{value}")
    public Response getCustomMatrixParamWithStringCtorMatch(@MatrixParam("var") final CustomWithStringCtor c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/matrix/typematch/customvalueofstring/{value}")
    public Response getCustomMatrixParamValueOfStringMatch(@MatrixParam("var") final CustomValueOfString c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/matrix/typematch/custominvalid/{value}")
    public Response getCustomMatrixParamInvalidMatch(@MatrixParam("var") final CustomInvalid c)
    {
        return Response.status(Status.OK).entity(c.toString()).build();
    }
    
    @GET
    @Path("get/return/boolean/{v}")
    public boolean getBoolean(@PathParam("v") boolean v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/byte/{v}")
    public byte getByte(@PathParam("v") byte v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/char/{v}")
    public char getChar(@PathParam("v") final String v)
    {
        return v.charAt(0);
    }
    
    @GET
    @Path("get/return/short/{v}")
    public short getShort(@PathParam("v") short v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/int/{v}")
    public int getInt(@PathParam("v") int v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/long/{v}")
    public long getLong(@PathParam("v") long v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/float/{v}")
    public float getFloat(@PathParam("v") float v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/double/{v}")
    public double getDouble(@PathParam("v") double v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/string/{v}")
    public String getString(@PathParam("v") final String v)
    {
        return v;
    }
    
    @GET
    @Path("get/return/person/{name}")
    public SimplePerson getPerson(@PathParam("name") final String name)
    {
        return new SimplePerson(name);
    }
    
    @GET
    @Path("get/return/tostring/{s}")
    public CustomWithStringCtor getToString(@PathParam("s") final String s)
    {
        return new CustomWithStringCtor(s);
    }
    
    @GET
    @Path("get/return/array/{l}")
    public String[] getArray(@PathParam("l") final String l)
    {
        return l.split(",");
    }
    
    @GET
    @Path("get/return/list/{l}")
    public List<Integer> getList(@PathParam("l") final String l)
    {
        List<Integer> rv = Lists.newArrayList();
        for (final String s : l.split(","))
        {
            rv.add(Integer.valueOf(s));
        }
        return rv;
    }
    
    @GET
    @Path("get/return/map/{name}/{age}/{city}")
    public Map<String, String> getMap(@PathParam("name") final String name,
            @PathParam("age") final String age,
            @PathParam("city") final String city)
    {
        Map<String, String> rv = Maps.newHashMap();
        rv.put("name", name);
        rv.put("age", age);
        rv.put("city", city);
        return rv;
    }
    
    @GET
    @Path("get/return/null")
    public String getNull()
    {
        return null;
    }
    
    @GET
    @Path("get/return/throws")
    public Response getThrowsException()
    {
        throw new RuntimeException("get/return/throws fail");
    }
    
    @GET
    @Path("get/queryparams/single")
    public String getQueryParams(@QueryParam("firstname") final String firstName)
    {
        return firstName;
    }
    
    @GET
    @Path("get/queryparams/multiple")
    public String getQueryParams(@QueryParam("lastname") final String lastName, @QueryParam("firstname") final String firstName)
    {
        return String.format("%s %s", firstName, lastName);
    }
    
    @GET
    @Path("get/queryparams/multsamekey")
    public String getQueryParams(@QueryParam("name") final List<String> names)
    {
        return Joiner.on(",").join(names);
    }
    
    @POST
    @Path("post/formparam/single")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postFormParam(@FormParam("p1") final String p1)
    {
        return Response.status(Status.OK).entity(p1).build();
    }
    
    // Handle POST of different content types
    // Disambiguate on POST to same paths with different content types
    // Handle post when @Consumes is declared at class level
    // Handle POST with @QueryParams and @FormParams
    // Handle POST with non-matching params / missing params
    // Type matching on @FormParam
    //  - What is even supposed to happen here?
    //  - I think it should match and send them NULL.
    
    @POST
    @Path("post/formparam/multiple")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postFormParamMultiple(@FormParam("p1") final String p1,
            @FormParam("p3") final String p3,
            @FormParam("p2") final String p2)
    {
        return Response.status(Status.OK).entity(String.format("%s,%s,%s", p1, p2, p3)).build();
    }
    
    @POST
    @Path("/post/queryandform")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postQueryAndForm(@QueryParam("qp1") final String qp1,
            @FormParam("fp1") final String fp1,
            @FormParam("fp2") final String fp2,
            @QueryParam("qp2") final String qp2,
            @FormParam("fp3") final String fp3,
            @QueryParam("qp3") final String qp3)
    {
        return Response.status(Status.OK).entity(String.format("qp1=%s,qp2=%s,qp3=%s,fp1=%s,fp2=%s,fp3=%s", qp1, qp2, qp3, fp1, fp2, fp3)).build();
    }
    
    @POST
    @Path("post/formparam/typematch/boolean")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postFormparamTypematchBoolean(@FormParam("p") boolean v)
    {
        return Boolean.toString(v);
    }
    
    @POST
    @Path("post/formparam/typematch/byte")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public byte postFormparamTypematchByte(@FormParam("p") byte v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/short")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Short postFormparamTypematchShort(@FormParam("p") short v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/int")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Integer postFormparamTypematchInt(@FormParam("p") int v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/long")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Long postFormparamTypematchLong(@FormParam("p") long v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/float")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Float postFormparamTypematchFloat(@FormParam("p") float v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/double")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Double postFormparamTypematchDouble(@FormParam("p") double v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/Integer")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Integer postFormparamTypematchInteger(@FormParam("p") final Integer v)
    {
        return v;
    }
    
    @POST
    @Path("post/formparam/typematch/customwithstringctor")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postFormparamTypematchCustomWithStringCtor(@FormParam("p") final CustomWithStringCtor v)
    {
        return v.getS();
    }
    
    @POST
    @Path("post/formparam/typematch/customvalueofstring")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postFormparamTypematchCustomValueOfString(@FormParam("p") final CustomValueOfString v)
    {
        return v.toString();
    }
    
    @POST
    @Path("post/formparam/typematch/custominvalid")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postFormparamTypematchCustomInvalid(@FormParam("p") final CustomInvalid v)
    {
        return "";
    }
}
