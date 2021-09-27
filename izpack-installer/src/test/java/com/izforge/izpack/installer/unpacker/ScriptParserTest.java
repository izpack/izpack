package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.ParsableFile;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;

public class ScriptParserTest {

  private File file;

  @Before
  public void setUp() throws Exception {
    file = File.createTempFile("test", "txt");
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.forceDelete(file);
  }

  @Test
  public void givenPlainTextFileAndSpecialCharactersInVariables_whenParseWithDefaultEncoding_contentIsWrittenWithDefaultEncoding() throws Exception {
    Variables variables = new DefaultVariables();
    variables.set("pippo", "PIPPO");
    variables.set("pluto", "plutò");
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    FileUtils.writeStringToFile(file, "${pippo}\n${pluto}\n", Charset.defaultCharset());

    ParsableFile parsable = new ParsableFile(file.getAbsolutePath(), SubstitutionType.TYPE_PLAIN, null, new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = FileUtils.readFileToString(file, Charset.defaultCharset());
    Assert.assertThat(content, containsString("PIPPO"));
    Assert.assertThat(content, containsString("plutò"));
  }

  @Test
  public void givenPlainTextFileAndSpecialCharactersInVariables_whenParseWithUtf8Encoding_contentIsWrittenWithUtf8Encoding() throws Exception {
    Variables variables = new DefaultVariables();
    variables.set("pippo", "PIPPO");
    variables.set("pluto", "plutò");
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    FileUtils.writeStringToFile(file, "${pippo}\n${pluto}\n", "UTF-8");

    ParsableFile parsable = new ParsableFile(file.getAbsolutePath(), SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = FileUtils.readFileToString(file, "UTF-8");
    Assert.assertThat(content, containsString("PIPPO"));
    Assert.assertThat(content, containsString("plutò"));
  }

  @Test
  public void givenPlainTextFileAndSpecialCharactersInContent_whenParseWithUtf8Encoding_contentIsWrittenWithUtf8Encoding() throws Exception {
    Variables variables = new DefaultVariables();
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    FileUtils.writeStringToFile(file, "PIPPO\nplutò\n", "UTF-8");

    ParsableFile parsable = new ParsableFile(file.getAbsolutePath(), SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = FileUtils.readFileToString(file, "UTF-8");
    Assert.assertThat(content, containsString("PIPPO"));
    Assert.assertThat(content, containsString("plutò"));
  }

  @Test
  public void givenPlainTextFileAndCyrillicCharactersInContent_whenParseWithUtf8Encoding_contentIsWrittenWithUtf8Encoding() throws Exception {
    Variables variables = new DefaultVariables();
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    String cyrillicContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<Board visible=\"$serverboard\" x=\"100\" y=\"100\" Наименование=\"Сохранить конфигурацию табло\">\n" +
        "    <Main>\n" +
        "        <Параметер Наименование=\"Номер дополнительного монитора для табло\" Тип=\"1\" Значение=\"1\"/>\n" +
        "        <Параметер Наименование=\"Фоновое изображение\" Тип=\"3\" Значение=\"config/board/wall_ligth.jpg\"/>\n" +
        "        <Параметер Наименование=\"Количество строк на табло\" Тип=\"1\" Значение=\"4\"/>\n" +
        "        <Параметер Наименование=\"Количество столбцов на табло\" Тип=\"1\" Значение=\"1\"/>\n" +
        "        <Параметер Наименование=\"Окантовка строк\" Тип=\"3\" Значение=\"0,0,0,0;5,0,0,0\"/>";

    FileUtils.writeStringToFile(file, cyrillicContent, "UTF-8");

    ParsableFile parsable = new ParsableFile(file.getAbsolutePath(), SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = FileUtils.readFileToString(file, "UTF-8");
    Assert.assertEquals(cyrillicContent, content);
  }

}