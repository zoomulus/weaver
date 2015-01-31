package com.zoomulus.weaver.rest;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class CustomWithStringCtor
{
    String s;
    
    public String toString()
    {
        return s;
    }
}
