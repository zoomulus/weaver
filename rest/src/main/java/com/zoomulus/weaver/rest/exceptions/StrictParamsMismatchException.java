package com.zoomulus.weaver.rest.exceptions;

public class StrictParamsMismatchException extends Exception
{
    private static final long serialVersionUID = 1L;

    public StrictParamsMismatchException(final String className,
            final String methodName,
            final int nExpectedParams,
            final int nActualParams)
    {
        super(String.format("Method %s.%s() expects %d %s but was invoked with %d",
                className,
                methodName,
                nExpectedParams,
                (nExpectedParams == 1 ? "parameter" : "parameters"),
                nActualParams));
    }
}
