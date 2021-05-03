package com.yelloco.fingodriver.utils.exceptions;

public class FingoSDKException extends Exception
{
    public FingoSDKException() {
    }

    public FingoSDKException(String message) {
        super(message);
    }

    public FingoSDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
