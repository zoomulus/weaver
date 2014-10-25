package com.zoomulus.weaver.rest.resource;

import java.util.Map;
import java.util.Set;

public interface ResourceScannerStrategy
{
    Map<ResourceIdentifier, Resource> scan(final Set<Class<?>> resourceClasses);
}
