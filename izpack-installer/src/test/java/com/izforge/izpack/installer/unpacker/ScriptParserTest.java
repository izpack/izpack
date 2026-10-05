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
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
  public void givenPosixFile_whenParsed_permissionsOwnerAndGroupArePreserved() throws Exception {
    String osName = System.getProperty("os.name");
    String groupName;
    //TODO:JUnit6 use @EnabledOnOs({ LINUX, MAC }) in the future
    if ("Linux".equals(osName)) {
      groupName = "users";
    } else if ("Mac OS X".equals(osName)) {
      groupName = "everyone";
    } else {
      return;
    }
    Path path = file.toPath();
    Assume.assumeTrue("POSIX permissions are required",
        Files.getFileAttributeView(path, PosixFileAttributeView.class) != null);
    // Use a group different from the temporary file's inherited group.
    Files.getFileAttributeView(path, PosixFileAttributeView.class).setGroup(
            path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByGroupName(groupName));

    Variables variables = new DefaultVariables();
    variables.set("INSTALL_PATH", "/Applications/Example.app");
    ScriptParser scriptParser = new ScriptParser(new VariableSubstitutorImpl(variables),
        new PlatformModelMatcher(new Platforms(), Platforms.MAC_OSX));
    ParsableFile parsable = new ParsableFile(file.getAbsolutePath(),
        SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    // Preserve both shared launch-script permissions and restricted file permissions.
    for (String mode : new String[] {"rwxr-xr-x", "rw-r-----"}) {
      FileUtils.writeStringToFile(file, "exec ${INSTALL_PATH}/bin/java", "UTF-8");
      Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(mode);
      Files.setPosixFilePermissions(path, permissions);
      PosixFileAttributes expectedAttributes = Files.readAttributes(path, PosixFileAttributes.class);

      scriptParser.parse(parsable);

      Assert.assertEquals("exec /Applications/Example.app/bin/java",
          FileUtils.readFileToString(file, "UTF-8"));
      Assert.assertEquals("Permissions must survive parsing", permissions,
          Files.getPosixFilePermissions(path));
      PosixFileAttributes actualAttributes = Files.readAttributes(path, PosixFileAttributes.class);
      Assert.assertEquals("Owner must survive parsing", expectedAttributes.owner(), actualAttributes.owner());
      Assert.assertEquals("Group must survive parsing", expectedAttributes.group(), actualAttributes.group());
    }
  }

  @Test
  public void givenWindowsFile_whenParsed_aclAndOwnerArePreserved() throws Exception {
    //TODO:JUnit6 use @EnabledOnOs(WINDOWS) in the future
    if (!System.getProperty("os.name").startsWith("Windows")) {
      return;
    }
    Path path = file.toPath();
    AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
    Assume.assumeNotNull(aclView);

    List<AclEntry> originalAcl = aclView.getAcl();
    UserPrincipal expectedOwner = aclView.getOwner();
    // Add an explicit entry that a new temporary file would not inherit.
    // Denying execution leaves reading, rewriting and cleanup available.
    AclEntry denyExecute = AclEntry.newBuilder()
        .setType(AclEntryType.DENY)
        .setPrincipal(aclView.getOwner())
        .setPermissions(AclEntryPermission.EXECUTE)
        .build();
    List<AclEntry> customAcl = new ArrayList<>(originalAcl);
    customAcl.add(0, denyExecute);
    try {
      aclView.setAcl(customAcl);
      List<AclEntry> expectedAcl = aclView.getAcl();
      Assert.assertTrue("The explicit ACL entry must be present", expectedAcl.contains(denyExecute));

      Variables variables = new DefaultVariables();
      variables.set("INSTALL_PATH", "C:/Example");
      ScriptParser scriptParser = new ScriptParser(new VariableSubstitutorImpl(variables),
          new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS));
      FileUtils.writeStringToFile(file, "${INSTALL_PATH}/bin/java.exe", "UTF-8");
      ParsableFile parsable = new ParsableFile(file.getAbsolutePath(),
          SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

      scriptParser.parse(parsable);

      Assert.assertEquals("C:/Example/bin/java.exe", FileUtils.readFileToString(file, "UTF-8"));
      Assert.assertEquals("ACL must survive parsing", expectedAcl,
          Files.getFileAttributeView(path, AclFileAttributeView.class).getAcl());
      Assert.assertEquals("Owner must survive parsing", expectedOwner, Files.getOwner(path));
    } finally {
      Files.getFileAttributeView(path, AclFileAttributeView.class).setAcl(originalAcl);
    }
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
  public void givenPlainTextFile_whenParseWithNullEncoding_contentIsWrittenWithDefaultEncoding() throws Exception {
    Variables variables = new DefaultVariables();
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    String content = "Simple ASCII content";
    FileUtils.writeStringToFile(file, content, Charset.defaultCharset());

    ParsableFile parsable = new ParsableFile(file.getAbsolutePath(), SubstitutionType.TYPE_PLAIN, null, new ArrayList<>());

    scriptParser.parse(parsable);

    String readContent = FileUtils.readFileToString(file, Charset.defaultCharset());
    Assert.assertEquals(content, readContent);
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
