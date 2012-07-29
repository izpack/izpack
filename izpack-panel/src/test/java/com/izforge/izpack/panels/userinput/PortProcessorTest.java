package com.izforge.izpack.panels.userinput;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.izforge.izpack.panels.userinput.processor.PortProcessor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

import junit.framework.Assert;


@Ignore
public class PortProcessorTest
{
    @Test
    public void testProcessGenericBoundPort() throws IOException
    {
        // create a ServerSocket on any free port (for all available network interfaces)
        ServerSocket use = new ServerSocket(0); // create a serversocket on any free port
        int usedPort = use.getLocalPort();
        ProcessingClient pc = new ProcessingClientStub(usedPort);
        PortProcessor pp = new PortProcessor();
        String result = pp.process(pc);
        Assert.assertTrue((Integer.toString(usedPort)).equals(result));
        try
        {
            use.close();
        }
        catch (Throwable t)
        {
            //ignore cleanup errors
        }
    }

    @Test
    public void testProcessSpecificBoundPort() throws IOException
    {
        // create a ServerSocket for localhost on any free port
        ServerSocket use = new ServerSocket(0, 0, InetAddress.getByName("localhost"));
        int usedPort = use.getLocalPort();
        ProcessingClient pc = new ProcessingClientStub("localhost", usedPort);
        PortProcessor pp = new PortProcessor();
        String result = pp.process(pc);
        System.out.println(result);
        Assert.assertFalse(("localhost*" + Integer.toString(usedPort)).equals(result));
        try
        {
            use.close();
        }
        catch (Throwable t)
        {
            //ignore cleanup errors
        }
    }

    @Test
    public void testProcessGenericOnGenericBoundPortIPv6() throws IOException
    {
        // create a ServerSocket for localhost on any free port
        ServerSocket use = new ServerSocket(0, 0, InetAddress.getByName("::"));
        int usedPort = use.getLocalPort();
        ProcessingClient pc = new ProcessingClientStub("::", usedPort);
        PortProcessor pp = new PortProcessor();
        String result = pp.process(pc);
        Assert.assertFalse(("::*" + Integer.toString(usedPort)).equals(result));
        try
        {
            use.close();
        }
        catch (Throwable t)
        {
            //ignore cleanup errors
        }
    }

    @Test
    public void testProcessGenericOnGenericBoundPortIPv4() throws IOException
    {
        // create a ServerSocket for localhost on any free port
        ServerSocket use = new ServerSocket(0, 0, InetAddress.getByName("0.0.0.0"));
        int usedPort = use.getLocalPort();
        ProcessingClient pc = new ProcessingClientStub("0.0.0.0", usedPort);
        PortProcessor pp = new PortProcessor();
        String result = pp.process(pc);
        Assert.assertFalse(("0.0.0.0*" + Integer.toString(usedPort)).equals(result));
        try
        {
            use.close();
        }
        catch (Throwable t)
        {
            //ignore cleanup errors
        }
    }

    @Test
    public void testProcessSpecificOnGenericBoundPortIPv4() throws IOException
    {
        // create a ServerSocket for localhost on any free port
        ServerSocket use = new ServerSocket(0, 0, InetAddress.getByName("0.0.0.0"));
        int usedPort = use.getLocalPort();
        ProcessingClient pc = new ProcessingClientStub("127.0.0.1", usedPort);
        PortProcessor pp = new PortProcessor();
        String result = pp.process(pc);
        Assert.assertEquals("127.0.0.1*" + Integer.toString(usedPort), result);
        try
        {
            use.close();
        }
        catch (Throwable t)
        {
            //ignore cleanup errors
        }
    }

    class ProcessingClientStub implements ProcessingClient
    {

        String[] fields;

        public ProcessingClientStub(String host, int port)
        {
            fields = new String[2];
            fields[0] = host;
            fields[1] = Integer.toString(port);
        }

        public ProcessingClientStub(int port)
        {
            fields = new String[1];
            fields[0] = Integer.toString(port);
        }

        public String getFieldContents(int index)
        {
            if (index < fields.length)
            {
                return fields[index];
            }
            else
            {
                throw new IndexOutOfBoundsException();
            }
        }

        public int getNumFields()
        {
            return fields.length;
        }

        public String getText()
        {
            return null;
        }

        public Map<String, String> getValidatorParams()
        {
            return null;
        }

        public boolean hasParams()
        {
            return false;
        }

    }
}
