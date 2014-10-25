package com.zoomulus.weaver.rest;

import lombok.RequiredArgsConstructor;

import com.zoomulus.weaver.core.connector.ServerConnector;
import com.zoomulus.weaver.rest.connector.RestServerConnector;

@RequiredArgsConstructor
public class TestNettyService
{
    private final int port;
    
    public void start()
    {
        ServerConnector connector = RestServerConnector.builder()
                .withPort(port)
                .withResources("com.zoomulus.weaver.rest.testresources")
                .build();
        RestServer server = new RestServer(connector);
        server.start();
    }
    
    public static void main(final String[] args)
    {
        new TestNettyService(5150).start();
    }
}
