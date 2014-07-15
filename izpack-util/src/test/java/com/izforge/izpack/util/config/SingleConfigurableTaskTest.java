package com.izforge.izpack.util.config;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SingleConfigurableTaskTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

//    @Test
//    public void testPropertiesCommentsAtEnd() throws IOException
//    {
//        SingleOptionFileTask task = new SingleOptionFileTask();
//
//        URL oldFileUrl = getClass().getResource("oldversion.properties");
//        URL newFileUrl = getClass().getResource("newversion.properties");
//        URL expectedFileUrl = getClass().getResource("expected_after_merge.properties");
//        assertNotNull("Old file missing", oldFileUrl);
//        assertNotNull("New file missing", newFileUrl);
//        assertNotNull("Expected result file missing", expectedFileUrl);
//
//        File oldFile = new File(oldFileUrl.getFile());
//        File newFile = new File(newFileUrl.getFile());
//        File expectedFile = new File(expectedFileUrl.getFile());
//        File toFile = tmpDir.newFile("to.properties");
//
//        task.setToFile(toFile);
//        task.setOldFile(oldFile);
//        task.setNewFile(newFile);
//        task.setCleanup(false);
//        task.setCreate(true);
//        task.setPatchPreserveEntries(false);
//        task.setPatchPreserveValues(true);
//        task.setPatchResolveVariables(false);
//        task.setOperator("=");
//
//        try
//        {
//            task.execute();
//        }
//        catch (Exception e)
//        {
//            fail("Task could not be executed: " + e.getMessage());
//        }
//
//        printFileContent(toFile);
//        printFileContent(expectedFile);
//        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1"), true);
//    }
//
//    @Test
//    public void testWrapperCommentsAtEnd() throws IOException
//    {
//        SingleOptionFileTask task = new SingleOptionFileTask();
//
//        URL oldFileUrl = getClass().getResource("oldversion.wrapper.conf");
//        URL newFileUrl = getClass().getResource("newversion.wrapper.conf");
//        URL expectedFileUrl = getClass().getResource("expected_after_merge.wrapper.conf");
//        assertNotNull("Old file missing", oldFileUrl);
//        assertNotNull("New file missing", newFileUrl);
//        assertNotNull("Expected result file missing", expectedFileUrl);
//
//        File oldFile = new File(oldFileUrl.getFile());
//        File newFile = new File(newFileUrl.getFile());
//        File expectedFile = new File(expectedFileUrl.getFile());
//        File toFile = tmpDir.newFile("to.wrapper.conf");
//
//        task.setToFile(toFile);
//        task.setOldFile(oldFile);
//        task.setNewFile(newFile);
//        task.setCleanup(false);
//        task.setCreate(true);
//        task.setPatchPreserveEntries(false);
//        task.setPatchPreserveValues(true);
//        task.setPatchResolveVariables(false);
//        task.setOperator("=");
//        task.setEncoding(Charset.forName("UTF-8"));
//        task.setHeaderComment(true);
//
//        try
//        {
//            task.execute();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            fail("Task could not be executed: " + e.getMessage());
//        }
//
//        printFileContent(toFile);
//        printFileContent(expectedFile);
//        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "UTF-8"), true);
//    }

    @Test
    public void testIniCommentsAtEnd() throws IOException
    {
        SingleIniFileTask task = new SingleIniFileTask();

        URL oldFileUrl = getClass().getResource("oldversion.ini");
        URL newFileUrl = getClass().getResource("newversion.ini");
        URL expectedFileUrl = getClass().getResource("expected_after_merge.ini");
        assertNotNull("Old file missing", oldFileUrl);
        assertNotNull("New file missing", newFileUrl);
        assertNotNull("Expected result file missing", expectedFileUrl);

        File oldFile = new File(oldFileUrl.getFile());
        File newFile = new File(newFileUrl.getFile());
        File expectedFile = new File(expectedFileUrl.getFile());
        File toFile = tmpDir.newFile("to.ini");

        task.setToFile(toFile);
        task.setOldFile(oldFile);
        task.setNewFile(newFile);
        task.setCleanup(false);
        task.setCreate(true);
        task.setPatchPreserveEntries(false);
        task.setPatchPreserveValues(true);
        task.setPatchResolveVariables(false);

        try
        {
            task.execute();
        }
        catch (Exception e)
        {
            fail("Task could not be executed: " + e.getMessage());
        }

        printFileContent(toFile);
        printFileContent(expectedFile);
        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1"), true);
    }

    private void printFileContent(File file)
    {
        BufferedReader br;

        System.out.println();
        System.out.println("+++ " + file + " +++");
        try
        {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
