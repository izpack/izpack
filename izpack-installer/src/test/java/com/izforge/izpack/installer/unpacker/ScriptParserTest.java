package com.izforge.izpack.installer.unpacker;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.getFileAttributeView;
import static java.nio.file.Files.getOwner;
import static java.nio.file.Files.getPosixFilePermissions;
import static java.nio.file.Files.readAttributes;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.writeString;
import static java.nio.file.attribute.AclEntry.newBuilder;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.izforge.izpack.api.data.ParsableFile;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;


public class ScriptParserTest {

  @TempDir
  Path temporaryDirectory;

  private Path file;

  @BeforeEach
  public void setUp() throws Exception {
    file = createTempFile(temporaryDirectory, "test", "txt");
  }

  @Test
  @EnabledOnOs({LINUX, MAC})
  public void givenPosixFile_whenParsed_permissionsOwnerAndGroupArePreserved() throws Exception {
    String groupName = LINUX.isCurrentOs() ? "users" : "everyone";
    Path path = file;
    assumeTrue(getFileAttributeView(path, PosixFileAttributeView.class) != null, "POSIX permissions are required");
    // Use a group different from the temporary file's inherited group.
    getFileAttributeView(path, PosixFileAttributeView.class).setGroup(
            path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByGroupName(groupName));

    Variables variables = new DefaultVariables();
    variables.set("INSTALL_PATH", "/Applications/Example.app");
    ScriptParser scriptParser = new ScriptParser(new VariableSubstitutorImpl(variables),
        new PlatformModelMatcher(new Platforms(), Platforms.MAC_OSX));
    ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(),
        SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    // Preserve both shared launch-script permissions and restricted file permissions.
    for (String mode : new String[] {"rwxr-xr-x", "rw-r-----"}) {
      writeString(file, "exec ${INSTALL_PATH}/bin/java", UTF_8);
      Set<PosixFilePermission> permissions = fromString(mode);
      setPosixFilePermissions(path, permissions);
      PosixFileAttributes expectedAttributes = readAttributes(path, PosixFileAttributes.class);

      scriptParser.parse(parsable);

      assertThat(readString(file, UTF_8)).isEqualTo("exec /Applications/Example.app/bin/java");
      assertThat(getPosixFilePermissions(path)).as("Permissions must survive parsing").isEqualTo(permissions);
      PosixFileAttributes actualAttributes = readAttributes(path, PosixFileAttributes.class);
      assertThat(actualAttributes.owner()).as("Owner must survive parsing").isEqualTo(expectedAttributes.owner());
      assertThat(actualAttributes.group()).as("Group must survive parsing").isEqualTo(expectedAttributes.group());
    }
  }

  @Test
  @EnabledOnOs(WINDOWS)
  public void givenWindowsFile_whenParsed_aclAndOwnerArePreserved() throws Exception {
    Path path = file;
    AclFileAttributeView aclView = getFileAttributeView(path, AclFileAttributeView.class);
    assumeTrue(aclView != null, "ACL file attributes are required");

    List<AclEntry> originalAcl = aclView.getAcl();
    UserPrincipal expectedOwner = aclView.getOwner();
    // Add an explicit entry that a new temporary file would not inherit.
    // Denying execution leaves reading, rewriting and cleanup available.
    AclEntry denyExecute = newBuilder()
        .setType(AclEntryType.DENY)
        .setPrincipal(aclView.getOwner())
        .setPermissions(AclEntryPermission.EXECUTE)
        .build();
    List<AclEntry> customAcl = new ArrayList<>(originalAcl);
    customAcl.add(0, denyExecute);
    try {
      aclView.setAcl(customAcl);
      List<AclEntry> expectedAcl = aclView.getAcl();
      assertThat(expectedAcl).as("The explicit ACL entry must be present").contains(denyExecute);

      Variables variables = new DefaultVariables();
      variables.set("INSTALL_PATH", "C:/Example");
      ScriptParser scriptParser = new ScriptParser(new VariableSubstitutorImpl(variables),
          new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS));
      writeString(file, "${INSTALL_PATH}/bin/java.exe", UTF_8);
      ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(),
          SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

      scriptParser.parse(parsable);

      assertThat(readString(file, UTF_8)).isEqualTo("C:/Example/bin/java.exe");
      assertThat(getFileAttributeView(path, AclFileAttributeView.class).getAcl()).as("ACL must survive parsing").isEqualTo(expectedAcl);
      assertThat(getOwner(path)).as("Owner must survive parsing").isEqualTo(expectedOwner);
    } finally {
      getFileAttributeView(path, AclFileAttributeView.class).setAcl(originalAcl);
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

    writeString(file, "${pippo}\n${pluto}\n", defaultCharset());

    ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(), SubstitutionType.TYPE_PLAIN, null, new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = readString(file, defaultCharset());
    assertThat(content).contains("PIPPO");
    assertThat(content).contains("plutò");
  }

  @Test
  public void givenPlainTextFileAndSpecialCharactersInVariables_whenParseWithUtf8Encoding_contentIsWrittenWithUtf8Encoding() throws Exception {
    Variables variables = new DefaultVariables();
    variables.set("pippo", "PIPPO");
    variables.set("pluto", "plutò");
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    writeString(file, "${pippo}\n${pluto}\n", UTF_8);

    ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(), SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = readString(file, UTF_8);
    assertThat(content).contains("PIPPO");
    assertThat(content).contains("plutò");
  }

  @Test
  public void givenPlainTextFileAndSpecialCharactersInContent_whenParseWithUtf8Encoding_contentIsWrittenWithUtf8Encoding() throws Exception {
    Variables variables = new DefaultVariables();
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    writeString(file, "PIPPO\nplutò\n", UTF_8);

    ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(), SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = readString(file, UTF_8);
    assertThat(content).contains("PIPPO");
    assertThat(content).contains("plutò");
  }

  @Test
  public void givenPlainTextFile_whenParseWithNullEncoding_contentIsWrittenWithDefaultEncoding() throws Exception {
    Variables variables = new DefaultVariables();
    VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);
    PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
    ScriptParser scriptParser = new ScriptParser(replacer, matcher);

    String content = "Simple ASCII content";
    writeString(file, content, defaultCharset());

    ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(), SubstitutionType.TYPE_PLAIN, null, new ArrayList<>());

    scriptParser.parse(parsable);

    String readContent = readString(file, defaultCharset());
    assertThat(readContent).isEqualTo(content);
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

    writeString(file, cyrillicContent, UTF_8);

    ParsableFile parsable = new ParsableFile(file.toAbsolutePath().toString(), SubstitutionType.TYPE_PLAIN, "UTF-8", new ArrayList<OsModel>());

    scriptParser.parse(parsable);

    String content = readString(file, UTF_8);
    assertThat(content).isEqualTo(cyrillicContent);
  }

}
