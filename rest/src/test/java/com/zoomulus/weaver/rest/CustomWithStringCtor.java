package com.zoomulus.weaver.rest;

import lombok.Value;

@Value
public class CustomWithStringCtor
{
    String s;
    
    public String toString()
    {
        return s;
    }
}
