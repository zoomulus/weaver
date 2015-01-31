package com.zoomulus.weaver.rest;

import lombok.Getter;

public class CustomWithStringCtorChild extends CustomWithStringCtor
{
    @Getter
    private final CustomInvalid invalid = new CustomInvalid();
    
    public CustomWithStringCtorChild(String s)
    {
        super(s);
    }
}
