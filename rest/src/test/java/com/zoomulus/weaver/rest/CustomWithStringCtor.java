package com.zoomulus.weaver.rest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NoArgsConstructor
@AllArgsConstructor
@NonFinal
public class CustomWithStringCtor
{
    @NonFinal
    String s;
    
    public String toString()
    {
        return s;
    }
}
