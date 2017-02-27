package com.izforge.izpack.api.exception;

/**
 * This exception is thrown when the installation is aborted by the user, for
 * example by pressing quit or CTRL-C.
 */
public class UserInterruptException extends RuntimeException
{

    private static final long serialVersionUID = 7808127359861400565L;

    public UserInterruptException()
    {
        super();
    }

    public UserInterruptException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UserInterruptException(String message)
    {
        super(message);
    }

    public UserInterruptException(Throwable cause)
    {
        super(cause);
    }

}
