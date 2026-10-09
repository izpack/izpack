package com.izforge.izpack.util.config;

import static com.izforge.izpack.api.config.spi.OptionsBuilder.newInstance;
import static java.nio.file.Files.createFile;
import static org.apache.commons.io.FileUtils.contentEqualsIgnoreEOL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.config.spi.OptionsBuilder;
import com.izforge.izpack.util.config.SingleConfigurableTask.Entry;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SingleConfigurableTaskTest
{
    @TempDir
    public Path tmpDir;

//    @Test
//    public void testPropertiesCommentsAtEnd() throws IOException
//    {
//        SingleOptionFileTestTask task = new SingleOptionFileTestTask();
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
//        File toFile = java.nio.file.createFile(tmpDir.toPath().resolve("to.properties")).toFile();
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
//        assertEquals(contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1"), true);
//    }
//
//    @Test
//    public void testWrapperCommentsAtEnd() throws IOException
//    {
//        SingleOptionFileTestTask task = new SingleOptionFileTestTask();
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
//        File toFile = java.nio.file.createFile(tmpDir.toPath().resolve("to.wrapper.conf")).toFile();
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
//        assertEquals(contentEqualsIgnoreEOL(expectedFile, toFile, "UTF-8"), true);
//    }

    @Test
    public void testAutoNumberingPatchPreserveEntries() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");
        ob.handleOption("abc.xyz0.2", "value2");

        Options toOptions = new Options(config);
        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(false);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc.xyz0.")).isTrue();
        assertThat(result.length("abc.xyz0.")).isEqualTo(3);
        assertThat(result.get("abc.xyz0.", 0)).isEqualTo("value0");
        assertThat(result.get("abc.xyz0.", 1)).isEqualTo("value1");
        assertThat(result.get("abc.xyz0.", 2)).isEqualTo("value2");
    }

    @Test
    public void testAutoNumberingPatchPreserveEntries2() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder fromBuilder = newInstance(fromOptions);
        fromBuilder.handleOption("http.param.os.1", "Windows");
        fromBuilder.handleOption("http.param.os.1.nativelib.1", "bin/x86-32/native_1.jar");
        fromBuilder.handleOption("http.param.os.1.nativelib.2", "bin/x86-32/native_2.jar");

        Options toOptions = new Options(config);
        OptionsBuilder toBuilder = newInstance(toOptions);
        toBuilder.handleOption("http.param.os.1", "Windows");
        toBuilder.handleOption("http.param.os.1.nativelib.1", "bin/x86-32/native_1.jar");
        toBuilder.handleOption("http.param.os.1.nativelib.2", "bin/x86-32/native_2.jar");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(false);
        task.setPatchPreserveValues(true);
        task.execute();
        Options result = task.getResult();

        assertThat(result.keySet().contains("http.param.os.")).isTrue();
        assertThat(result.get("http.param.os.", 1)).isEqualTo("Windows");
        assertThat(result.keySet().contains("http.param.os..nativelib.1")).isTrue();
        assertThat(result.get("http.param.os..nativelib.1", 1)).isEqualTo("bin/x86-32/native_1.jar");
        assertThat(result.get("http.param.os..nativelib.2", 1)).isEqualTo("bin/x86-32/native_2.jar");

        // Test correct mapping back in OptionsFormatter to properties during storing the options
        StringWriter sw = new StringWriter();
        toOptions.store(sw);
        Properties props = new Properties();
        StringReader sr = new StringReader(sw.toString());
        props.load(sr);
        assertThat(props).hasSize(3);
        assertThat(props.getProperty("http.param.os.1")).isEqualTo("Windows");
        assertThat(props.getProperty("http.param.os.1.nativelib.1")).isEqualTo("bin/x86-32/native_1.jar");
        assertThat(props.getProperty("http.param.os.1.nativelib.2")).isEqualTo("bin/x86-32/native_2.jar");
        sr.close();
        sw.close();
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndMoreValues() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");
        ob.handleOption("abc.xyz0.2", "value2");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0", "value3");
        ob2.handleOption("abc.xyz0.1", "value4");
        ob2.handleOption("abc.xyz0.2", "value5");
        ob2.handleOption("abc.xyz0.3", "value6");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc.xyz0.")).isTrue();
        assertThat(result.length("abc.xyz0.")).isEqualTo(4);
        assertThat(result.get("abc.xyz0.", 0)).isEqualTo("value3");
        assertThat(result.get("abc.xyz0.", 1)).isEqualTo("value4");
        assertThat(result.get("abc.xyz0.", 2)).isEqualTo("value5");
        assertThat(result.get("abc.xyz0.", 3)).isEqualTo("value6");
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndLessValues() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");
        ob.handleOption("abc.xyz0.2", "value2");
        ob.handleOption("abc.xyz0.3", "value3");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0", "value3");
        ob2.handleOption("abc.xyz0.1", "value4");
        ob2.handleOption("abc.xyz0.2", "value5");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc.xyz0.")).isTrue();
        assertThat(result.length("abc.xyz0.")).isEqualTo(4);
        assertThat(result.get("abc.xyz0.", 0)).isEqualTo("value3");
        assertThat(result.get("abc.xyz0.", 1)).isEqualTo("value4");
        assertThat(result.get("abc.xyz0.", 2)).isEqualTo("value5");
    }

    @Test
    public void testAutoNumberingEmbeddedKeyPatchPreserveEntriesAndLessValues() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0.key", "value0");
        ob.handleOption("abc.xyz0.1.key", "value1");
        ob.handleOption("abc.xyz0.2.key", "value2");
        ob.handleOption("abc.xyz0.3.key", "value3");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0.key", "value3");
        ob2.handleOption("abc.xyz0.1.key", "value4");
        ob2.handleOption("abc.xyz0.2.key", "value5");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc.xyz0..key")).isTrue();
        assertThat(result.length("abc.xyz0..key")).isEqualTo(4);
        assertThat(result.get("abc.xyz0..key", 0)).isEqualTo("value3");
        assertThat(result.get("abc.xyz0..key", 1)).isEqualTo("value4");
        assertThat(result.get("abc.xyz0..key", 2)).isEqualTo("value5");
        assertThat(result.get("abc.xyz0..key", 3)).isEqualTo("value3");
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndValuesWithOverride() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0", "value3");
        ob2.handleOption("abc.xyz0.1", "value4");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        Entry entry = new Entry();
        entry.setKey("abc.xyz0.0");
        entry.setValue("value0_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.xyz0.1");
        entry.setValue("value1_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.xyz.unnumbered");
        entry.setValue("value_unnumbered_overridden");
        task.addEntry(entry);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc.xyz0.")).isTrue();
        assertThat(result.length("abc.xyz0.")).isEqualTo(2);
        assertThat(result.get("abc.xyz0.0")).isNull();
        assertThat(result.get("abc.xyz0.1")).isNull();
        assertThat(result.get("abc.xyz0.", 0)).isNotNull();
        assertThat(result.get("abc.xyz0.", 1)).isNotNull();
        assertThat(result.get("abc.xyz.unnumbered")).isNotNull();
        assertThat(result.get("abc.xyz0.", 0)).isEqualTo("value0_overridden");
        assertThat(result.get("abc.xyz0.", 1)).isEqualTo("value1_overridden");
        assertThat(result.get("abc.xyz.unnumbered")).isEqualTo("value_unnumbered_overridden");
    }

    @Test
    public void testAutoNumberingEmbeddedKeyPatchPreserveEntriesAndValuesWithOverride() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = newInstance(fromOptions);
        ob.handleOption("abc.0.xyz0", "value0");
        ob.handleOption("abc.1.xyz0", "value1");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = newInstance(fromOptions);
        ob2.handleOption("abc.0.xyz0", "value3");
        ob2.handleOption("abc.1.xyz0", "value4");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        Entry entry = new Entry();
        entry.setKey("abc.0.xyz0");
        entry.setValue("value0_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.1.xyz0");
        entry.setValue("value1_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.xyz.unnumbered");
        entry.setValue("value_unnumbered_overridden");
        task.addEntry(entry);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc..xyz0")).isTrue();
        assertThat(result.length("abc..xyz0")).isEqualTo(2);
        assertThat(result.get("abc.0.xyz0")).isNull();
        assertThat(result.get("abc.1.xyz0")).isNull();
        assertThat(result.get("abc..xyz0", 0)).isNotNull();
        assertThat(result.get("abc..xyz0", 1)).isNotNull();
        assertThat(result.get("abc.xyz.unnumbered")).isNotNull();
        assertThat(result.get("abc..xyz0", 0)).isEqualTo("value0_overridden");
        assertThat(result.get("abc..xyz0", 1)).isEqualTo("value1_overridden");
        assertThat(result.get("abc.xyz.unnumbered")).isEqualTo("value_unnumbered_overridden");
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndValuesFromIndex1() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        Options toOptions = new Options(config);

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        Entry entry = new Entry();
        entry.setKey("abc.xyz0.1");
        entry.setValue("value1_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.xyz0.2");
        entry.setValue("value2_overridden");
        task.addEntry(entry);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc.xyz0.")).isTrue();
        assertThat(result.length("abc.xyz0.")).isEqualTo(3);
        assertThat(result.get("abc.xyz0.1")).isNull();
        assertThat(result.get("abc.xyz0.2")).isNull();
        assertThat(result.get("abc.xyz0.", 0)).isNull();
        assertThat(result.get("abc.xyz0.", 1)).isNotNull();
        assertThat(result.get("abc.xyz0.", 2)).isNotNull();
        assertThat(result.get("abc.xyz0.", 1)).isEqualTo("value1_overridden");
        assertThat(result.get("abc.xyz0.", 2)).isEqualTo("value2_overridden");
    }

    @Test
    public void testAutoNumberingEmbeddedKeyPatchPreserveEntriesAndValuesFromIndex1() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        Options toOptions = new Options(config);

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        Entry entry = new Entry();
        entry.setKey("abc.1.xyz0");
        entry.setValue("value1_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.2.xyz0");
        entry.setValue("value2_overridden");
        task.addEntry(entry);
        task.execute();
        Options result = task.getResult();
        assertThat(result.keySet().contains("abc..xyz0")).isTrue();
        assertThat(result.length("abc..xyz0")).isEqualTo(3);
        assertThat(result.get("abc.1.xyz0")).isNull();
        assertThat(result.get("abc.2.xyz0")).isNull();
        assertThat(result.get("abc..xyz0", 0)).isNull();
        assertThat(result.get("abc..xyz0", 1)).isNotNull();
        assertThat(result.get("abc..xyz0", 2)).isNotNull();
        assertThat(result.get("abc..xyz0", 1)).isEqualTo("value1_overridden");
        assertThat(result.get("abc..xyz0", 2)).isEqualTo("value2_overridden");
    }

    @Test
    public void testIniCommentsAtEnd() throws IOException, URISyntaxException
    {
        SingleIniFileTask task = new SingleIniFileTask();

        URL oldFileUrl = getClass().getResource("oldversion.ini");
        URL newFileUrl = getClass().getResource("newversion.ini");
        URL expectedFileUrl = getClass().getResource("expected_after_merge.ini");
        assertThat(oldFileUrl).as("Old file missing").isNotNull();
        assertThat(newFileUrl).as("New file missing").isNotNull();
        assertThat(expectedFileUrl).as("Expected result file missing").isNotNull();

        File oldFile = new File(oldFileUrl.toURI());
        File newFile = new File(newFileUrl.toURI());
        File expectedFile = new File(expectedFileUrl.toURI());
        File toFile = createFile(tmpDir.resolve("to.ini")).toFile();

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
        assertThat(contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1")).isTrue();
    }

    @Test
    public void testIniCommentsAtEndWithSpaceInPath() throws IOException, URISyntaxException
    {
        SingleIniFileTask task = new SingleIniFileTask();

        URL oldFileUrl = getClass().getResource("/com/izforge/izpack/util/config with space/oldversion2.ini");
        URL newFileUrl = getClass().getResource("/com/izforge/izpack/util/config with space/newversion2.ini");
        URL expectedFileUrl = getClass().getResource("/com/izforge/izpack/util/config with space/expected_after_merge2.ini");
        assertThat(oldFileUrl).as("Old file missing").isNotNull();
        assertThat(newFileUrl).as("New file missing").isNotNull();
        assertThat(expectedFileUrl).as("Expected result file missing").isNotNull();

        System.out.println("The oldFileUrl: "+ oldFileUrl.toURI().toString());

        File oldFile = new File(oldFileUrl.toURI());
        File newFile = new File(newFileUrl.toURI());
        File expectedFile = new File(expectedFileUrl.toURI());
        File toFile = createFile(tmpDir.resolve("to.ini")).toFile();

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
        assertThat(contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1")).isTrue();
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
