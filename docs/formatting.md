# IDE setup for code formatting

The build formats Java with [Spotless](https://diffplug.github.io/spotless/), using the
checked-in Eclipse profile `src/eclipse-code-formatter.xml` as the only style definition.
That same file is what you import into your IDE, so there is no per-IDE style file in the
repository and none needs to be created locally.

Both recipes below are one-time, per-developer steps. Re-run them whenever the profile
changes.

## Eclipse

1. Open **Preferences** (macOS: **Eclipse** menu → **Settings**; Windows/Linux:
   **Window** → **Preferences**).
2. Go to **Java** → **Code Style** → **Formatter**.
3. Click **Import...** and select `src/eclipse-code-formatter.xml` from your clone.
4. Select the imported **IzPack** profile in the list and click **OK** so it becomes the
   active profile.

## IntelliJ IDEA

1. Open **Settings** (macOS: **IntelliJ IDEA** → **Settings**; Windows/Linux: **File** →
   **Settings**).
2. Go to **Editor** → **Code Style** → **Java**.
3. Click the gear icon next to the **Scheme** selector and choose **Import Scheme** →
   **Eclipse XML Profile**.
4. Select `src/eclipse-code-formatter.xml` from your clone, then apply the imported scheme.

IntelliJ's import maps JDT settings onto its own model, so the result approximates the
Eclipse profile rather than reproducing it exactly — in particular, check that the right
margin is 100 after importing. If you want the actual Eclipse formatter running inside
IntelliJ instead of a mapped approximation, the third-party "Adapter for Eclipse Code
Formatter" plugin does that; it is optional, unversioned here, and not required by the
build.

## Which one wins

Your IDE is a convenience, not the contract. Formatting is enforced by the build against
the pinned Eclipse version, so run

```sh
./mvnw spotless:apply
```

before you push, and treat its result — not your IDE's — as canonical. See the
`Code formatting` section of `README.md` for the enforcement, escape-hatch and pre-push
hook details.
