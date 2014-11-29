package com.zoomulus.weaver.rest;

import lombok.Getter;

@Getter
public class SimplePerson
{
    final String name;
    int age = 30;
    final String city = "Nowhere";
    
    public SimplePerson(final String name)
    {
        this.name = name;
    }
}
