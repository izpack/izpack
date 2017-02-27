package com.izforge.izpack.util.os;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.util.Librarian;

public class FileQueue
{

    private List<FileQueueOperation> operations = new ArrayList<FileQueueOperation>();

    protected WinSetupFileQueue filequeue;

    /**
     * The librarian.
     */
    private final Librarian librarian;

    /**
     * Constructs a <tt>FileQueue</tt>.
     *
     * @param librarian the librarian
     */
    public FileQueue(Librarian librarian)
    {
        this.librarian = librarian;
    }

    /**
     * Add a file queue operation.
     *
     * @param op the operation (copy/move/delete)
     */
    public void add(FileQueueOperation op)
    {
        operations.add(op);
    }

    /**
     * Determines if the queue is empty.
     *
     * @return {@code true} if the queue is empty
     */
    public boolean isEmpty()
    {
        return operations.isEmpty();
    }

    public void execute() throws IOException
    {
        WinSetupDefaultCallbackHandler handler = new WinSetupDefaultCallbackHandler();
        try
        {
            filequeue = new WinSetupFileQueue(librarian, handler);
        }
        catch (IOException ioe)
        {
            throw new IOException("Failed to open a file queue due to " + ioe.getMessage());
        }
        try
        {
            for (FileQueueOperation operation : operations)
            {
                operation.addTo(filequeue);
            }
            filequeue.commit();

            List<SystemErrorException> exceptions = handler.getExceptions();
            if (exceptions != null)
            {
                StringBuilder buf = new StringBuilder();
                buf.append("The following system errors occured during committing the file queue:\n");
                for (SystemErrorException exception : exceptions)
                {
                    buf.append('\t');
                    buf.append(exception.toString());
                    buf.append('\n');
                }
                throw new IOException(buf.toString());
            }

        }
        catch (IOException ioe)
        {
            throw new IOException("File queue operation failed due to " + ioe.getMessage());
        }
        finally
        {
            filequeue.close();
        }
    }

    public boolean isRebootNecessary()
    {
        return filequeue != null && filequeue.isRebootNecessary();
    }

    /**
     * Returns the file queue operations.
     *
     * @return the file queue operations
     */
    public List<FileQueueOperation> getOperations()
    {
        return operations;
    }

}
