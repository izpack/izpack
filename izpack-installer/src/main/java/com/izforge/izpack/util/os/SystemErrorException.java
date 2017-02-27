package com.izforge.izpack.util.os;

public class SystemErrorException extends Exception
{

    private static final long serialVersionUID = 1416855067735971989L;

    /**
     * System error code
     */
    private int errorCode = 0;

    /**
     * Constructs a system error exception with no descriptive information.
     */
    public SystemErrorException()
    {
        super();
    }

    /**
     * Constructs a system error exception with the given descriptive message.
     *
     * @param message A description of or information about the exception.
     * Should not be <code>null</code>.
     */
    public SystemErrorException(String message)
    {
        super(message);
    }

    /**
     * Constructs a system error exception with the given Windows system error
     * code and message.
     *
     * @param errorCode The system error code.
     * @param message A description of or information about the exception.
     * Should not be <code>null</code> unless a cause is specified.
     */
    public SystemErrorException(int errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    public String toString()
    {
        return super.toString();
    }

    /**
     * Sets the system error code.
     *
     * @param errorCode the system error cod.
     */
    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    /**
     * Returns the system error code.
     *
     * @return the system error code.
     */
    public int getErrorCode()
    {
        return errorCode;
    }

}
