# Testing IzPack

Tests use JUnit 6.1.0 Jupiter on the JUnit Platform. The parent JUnit BOM aligns the API, engine, parameterized tests, and launcher. Java 17 and 21 are the tested build runtimes. Surefire and Failsafe retain the existing test selections and report directories.

Run the default unit and integration selections with:

```sh
./mvnw verify -Ddependency-check.skip=true
```

Run the GUI selection with a working display (CI uses Xvfb):

```sh
./mvnw verify -Pwith-gui-tests -Ddependency-check.skip=true
```

The GUI profile selects its existing GUI tests; its includes do not mean that every default unit test also runs. Platform restrictions and administrative privileges still determine which native tests can execute. Disabled tests remain disabled.

## Migrating shared test helpers

Replace `@RunWith(PlatformRunner.class)` with `@ExtendWith(PlatformExtension.class)` and `@RunWith(PicoRunner.class)` with the existing `@Container(...)` declaration, which now registers `PicoExtension` automatically. Both extensions are in `com.izforge.izpack.test.junit`; `ExtendWith` is from `org.junit.jupiter.api.extension`. The old runner classes are removed.

`RunOn` retains its platform-family matching and method restrictions override class restrictions. For inherited methods, the fallback class annotation is read from the method's declaring class, as with the previous runner. PicoExtension includes platform filtering automatically.

```java
@Container(TestCompilerContainer.class)
public class CompilerFixtureTest {
    public CompilerFixtureTest(CompilerConfig config) {
        // Dependencies are supplied by the configured Pico container.
    }

    @Test
    @InstallFile("samples/helloAndFinish.xml")
    public void compileInstallation() {
        // The container receives this method's InstallFile annotation.
    }
}
```

Existing explicit `@ExtendWith(PicoExtension.class)` registrations remain compatible and are deduplicated by Jupiter; new tests need only `@Container`. The container declaration is inherited, and a subclass can select a different container.

Pico tests require the default `PER_METHOD` lifecycle and sequential execution. Each eligible method gets a fresh container and injected test instance. Containers are created outside the Swing EDT; test instances are resolved on the EDT. Setup, body, and teardown keep their execution-thread behavior. Containers are disposed after user teardown or construction failure, and the prior context classloader is restored even if disposal fails. Cleanup failures are attached to a primary test failure.

Replace container constructors taking `(Class<?>, FrameworkMethod)` with `(Class<?>, java.lang.reflect.Method)` and use `Method.getAnnotation(...)`. Class-only and no-argument constructor fallbacks remain supported. These shared test APIs are source-breaking for downstream test-common consumers.

Jupiter constructs instances before evaluating method conditions. For platform-excluded, native OS-excluded, or `@Disabled` Pico methods, Objenesis provides an inert instance without invoking the test constructor; no container is created. That instance is never used to run setup or the test body.

Ordinary assertions use AssertJ with explicit static imports (`assertThat`, `assertThatThrownBy`, `fail`). Keep narrow exception execution scopes, identity checks, numeric types, and assertion descriptions. Jupiter `assertThrows` remains useful when the asserted exception is returned for further inspection; `assertAll`, assumptions, and timeouts remain Jupiter execution utilities. Specialized ZIP, merge, serialized-object, and whitespace matchers retain qualified Hamcrest assertions when an equivalent conversion is unavailable. Former JUnit 3 convention-only methods now have explicit `@Test` annotations. The Maven plugin tests use a Jupiter-owned Plexus fixture with real project/session setup, without inheriting a legacy test case.

For the former whole-lifecycle timeout rules, `@TestTimeout(milliseconds)` retains a cumulative deadline over setup, body, and teardown. It interrupts the execution thread on expiry and clears its timeout interrupt during cleanup. Code must respond to interruption; test-container construction remains outside the deadline, as with the old rule. Native Jupiter `@Timeout` can be used for ordinary test-method-only limits.


## Native OS eligibility

Use `@EnabledOnOs` or `@DisabledOnOs` for exact host OS restrictions, with static imports of Jupiter's OS constants. Script permission checks use `@EnabledOnOs({LINUX, MAC})`; Windows ACL checks use `@EnabledOnOs(WINDOWS)`. Keep filesystem capability and administrator assumptions: an eligible OS does not guarantee those capabilities. Conditions report exclusions as disabled before method setup, rather than passing through an early return.

Native class and method conditions are cumulative, and native class OS annotations are not inherited. Keep `@RunOn` when inheritance, method-over-class overrides, or IzPack's broader UNIX platform family matters. Do not annotate tests that merely model a target OS independent of the host. Pico's pre-construction guard handles the pinned Jupiter `EnabledOnOs`/`DisabledOnOs` attributes and composed annotations using public OS detection; it honors `junit.jupiter.conditions.deactivate` patterns. Jupiter owns disabled reporting. Other Jupiter conditions are not allocation guards; parity tests must be revisited on Jupiter upgrades.

## Filesystem fixtures

Prefer `Path`, `Files`, and `@TempDir Path` for test-owned filesystem fixtures:

```java
@TempDir
Path directory;

@Test
void writesFixture() throws IOException {
    Path fixture = directory.resolve("fixture.txt");
    Files.writeString(fixture, "contents", StandardCharsets.UTF_8);
    assertThat(fixture).exists();
    assertThat(Files.readString(fixture, StandardCharsets.UTF_8)).isEqualTo("contents");
}
```

Use `resolve` for child paths and `Path.of` for host path text. Preserve explicit encodings and `Charset.defaultCharset()` where default encoding is under test. Close `Files.list`, `Files.walk`, and `Files.lines` streams with try-with-resources before cleanup. Restore modified ACLs in `finally`; Jupiter cleans up test-owned temporary directories. Container-owned archives and outputs retain their existing ownership.

Convert to `File` at production, compiler, Maven harness, or archive APIs that require it. Shared `TestHelper` has Path overloads and compatible File adapters; its comparison contract still uses distinct absolute path strings, length, and CRC32. Keep literal installer paths, ZIP entry names, and modeled paths as strings where host filesystem interpretation would change the test. Retain Commons IO for recursive deletion, checksum comparison, or encoding/EOL operations whose behavior is not replaced equivalently. Do not substitute `Files.notExists` for negated existence, normalize lexical paths, follow symlinks during cleanup, or change open/copy options as incidental cleanup.
