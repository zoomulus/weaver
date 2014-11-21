package com.zoomulus.weaver.rest;

public class CustomValueOfString
{
    private String s;
    private CustomValueOfString() { }
    
    private void set(final String s)
    {
        this.s = s;
    }
    
    public static CustomValueOfString valueOf(final String s)
    {
        CustomValueOfString rv = new CustomValueOfString();
        rv.set(s);
        return rv;
    }
    
    public String toString()
    {
        return s;
    }
}
