package com.izforge.izpack.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p><i>A copy of java.lang.Runtime.Version class from JDK 10 adopted to use with JDK < 9.
 * <br/>Two methods was added {@link #parseToJdk9Schema(String)} and {@link #current()}
 * </i></p>
 *
 * A representation of a version string for an implementation of the
 * Java&nbsp;SE Platform.  A version string consists of a version number
 * optionally followed by pre-release and build information.
 *
 * <h2><a id="verNum">Version numbers</a></h2>
 *
 * <p> A <em>version number</em>, {@code $VNUM}, is a non-empty sequence of
 * elements separated by period characters (U+002E).  An element is either
 * zero, or an unsigned integer numeral without leading zeros.  The final
 * element in a version number must not be zero.  When an element is
 * incremented, all subsequent elements are removed.  The format is: </p>
 *
 * <blockquote><pre>
 * [1-9][0-9]*((\.0)*\.[1-9][0-9]*)*
 * </pre></blockquote>
 *
 * <p> The sequence may be of arbitrary length but the first four elements
 * are assigned specific meanings, as follows:</p>
 *
 * <blockquote><pre>
 * $FEATURE.$INTERIM.$UPDATE.$PATCH
 * </pre></blockquote>
 *
 * <ul>
 *
 * <li><p> <a id="FEATURE">{@code $FEATURE}</a> &#x2014; The
 * feature-release counter, incremented for every feature release
 * regardless of release content.  Features may be added in a feature
 * release; they may also be removed, if advance notice was given at least
 * one feature release ahead of time.  Incompatible changes may be made
 * when justified. </p></li>
 *
 * <li><p> <a id="INTERIM">{@code $INTERIM}</a> &#x2014; The
 * interim-release counter, incremented for non-feature releases that
 * contain compatible bug fixes and enhancements but no incompatible
 * changes, no feature removals, and no changes to standard APIs.
 * </p></li>
 *
 * <li><p> <a id="UPDATE">{@code $UPDATE}</a> &#x2014; The update-release
 * counter, incremented for compatible update releases that fix security
 * issues, regressions, and bugs in newer features. </p></li>
 *
 * <li><p> <a id="PATCH">{@code $PATCH}</a> &#x2014; The emergency
 * patch-release counter, incremented only when it's necessary to produce
 * an emergency release to fix a critical issue. </p></li>
 *
 * </ul>
 *
 * <p> The fifth and later elements of a version number are free for use by
 * platform implementors, to identify implementor-specific patch
 * releases. </p>
 *
 * <p> A version number never has trailing zero elements.  If an element
 * and all those that follow it logically have the value zero then all of
 * them are omitted. </p>
 *
 * <p> The sequence of numerals in a version number is compared to another
 * such sequence in numerical, pointwise fashion; <em>e.g.</em>, {@code
 * 10.0.4} is less than {@code 10.1.2}.  If one sequence is shorter than
 * another then the missing elements of the shorter sequence are considered
 * to be less than the corresponding elements of the longer sequence;
 * <em>e.g.</em>, {@code 10.0.2} is less than {@code 10.0.2.1}. </p>
 *
 * <h2><a id="verStr">Version strings</a></h2>
 *
 * <p> A <em>version string</em>, {@code $VSTR}, is a version number {@code
 * $VNUM}, as described above, optionally followed by pre-release and build
 * information, in one of the following formats: </p>
 *
 * <blockquote><pre>
 *     $VNUM(-$PRE)?\+$BUILD(-$OPT)?
 *     $VNUM-$PRE(-$OPT)?
 *     $VNUM(+-$OPT)?
 * </pre></blockquote>
 *
 * <p> where: </p>
 *
 * <ul>
 *
 * <li><p> <a id="pre">{@code $PRE}</a>, matching {@code ([a-zA-Z0-9]+)}
 * &#x2014; A pre-release identifier.  Typically {@code ea}, for a
 * potentially unstable early-access release under active development, or
 * {@code internal}, for an internal developer build. </p></li>
 *
 * <li><p> <a id="build">{@code $BUILD}</a>, matching {@code
 * (0|[1-9][0-9]*)} &#x2014; The build number, incremented for each promoted
 * build.  {@code $BUILD} is reset to {@code 1} when any portion of {@code
 * $VNUM} is incremented. </p></li>
 *
 * <li><p> <a id="opt">{@code $OPT}</a>, matching {@code ([-a-zA-Z0-9.]+)}
 * &#x2014; Additional build information, if desired.  In the case of an
 * {@code internal} build this will often contain the date and time of the
 * build. </p></li>
 *
 * </ul>
 *
 * <p> A version string {@code 10-ea} matches {@code $VNUM = "10"} and
 * {@code $PRE = "ea"}.  The version string {@code 10+-ea} matches
 * {@code $VNUM = "10"} and {@code $OPT = "ea"}. </p>
 *
 * <p> When comparing two version strings, the value of {@code $OPT}, if
 * present, may or may not be significant depending on the chosen
 * comparison method.  The comparison methods {@link #compareTo(JavaVersion)
 * compareTo()} and {@link #compareToIgnoreOptional(JavaVersion)
 * compareToIgnoreOptional()} should be used consistently with the
 * corresponding methods {@link #equals(Object) equals()} and {@link
 * #equalsIgnoreOptional(Object) equalsIgnoreOptional()}.  </p>
 *
 * <p> A <em>short version string</em>, {@code $SVSTR}, often useful in
 * less formal contexts, is a version number optionally followed by a
 * pre-release identifier:</p>
 *
 * <blockquote><pre>
 *     $VNUM(-$PRE)?
 * </pre></blockquote>
 *
 * <p>This is a <a href="./doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code Version} may have unpredictable results and should be avoided.
 * </p>
 *
 */
public final class JavaVersion implements Comparable<JavaVersion> {
    private final List<Integer> version;
    private final Optional<String> pre;
    private final Optional<Integer> build;
    private final Optional<String>  optional;

    /*
     * List of version number components passed to this constructor MUST
     * be at least unmodifiable (ideally immutable). In the case on an
     * unmodifiable list, the caller MUST hand the list over to this
     * constructor and never change the underlying list.
     */
    private JavaVersion(List<Integer> unmodifiableListOfVersions,
            Optional<String> pre,
            Optional<Integer> build,
            Optional<String> optional)
    {
        this.version = unmodifiableListOfVersions;
        this.pre = pre;
        this.build = build;
        this.optional = optional;
    }

    /**
     * Parses the given string as a valid
     * <a href="#verStr">version string</a> containing a
     * <a href="#verNum">version number</a> followed by pre-release and
     * build information.
     *
     * @param  s
     *         A string to interpret as a version
     *
     * @throws  IllegalArgumentException
     *          If the given string cannot be interpreted as a valid
     *          version
     *
     * @throws  NullPointerException
     *          If the given string is {@code null}
     *
     * @throws  NumberFormatException
     *          If an element of the version number or the build number
     *          cannot be represented as an {@link Integer}
     *
     * @return  The Version of the given string
     */
    public static JavaVersion parse(String s) {
        if (s == null)
            throw new NullPointerException();

        // Shortcut to avoid initializing VersionPattern when creating
        // feature-version constants during startup
        if (isSimpleNumber(s)) {
            return new JavaVersion(Collections.singletonList(Integer.parseInt(s)),
                    Optional.empty(), Optional.empty(), Optional.empty());
        }
        Matcher m = VersionPattern.VSTR_PATTERN.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("Invalid version string: '"
                    + s + "'");

        // $VNUM is a dot-separated list of integers of arbitrary length
        String[] split = m.group(VersionPattern.VNUM_GROUP).split("\\.");
        Integer[] version = new Integer[split.length];
        for (int i = 0; i < split.length; i++) {
            version[i] = Integer.parseInt(split[i]);
        }

        Optional<String> pre = Optional.ofNullable(
                m.group(VersionPattern.PRE_GROUP));

        String b = m.group(VersionPattern.BUILD_GROUP);
        // $BUILD is an integer
        Optional<Integer> build = (b == null)
                ? Optional.empty()
                : Optional.of(Integer.parseInt(b));

        Optional<String> optional = Optional.ofNullable(
                m.group(VersionPattern.OPT_GROUP));

        // empty '+'
        if (!build.isPresent()) {
            if (m.group(VersionPattern.PLUS_GROUP) != null) {
                if (optional.isPresent()) {
                    if (pre.isPresent())
                        throw new IllegalArgumentException("'+' found with"
                                + " pre-release and optional components:'" + s
                                + "'");
                } else {
                    throw new IllegalArgumentException("'+' found with neither"
                            + " build or optional components: '" + s + "'");
                }
            } else {
                if (optional.isPresent() && !pre.isPresent()) {
                    throw new IllegalArgumentException("optional component"
                            + " must be preceeded by a pre-release component"
                            + " or '+': '" + s + "'");
                }
            }
        }
        return new JavaVersion(Arrays.asList(version), pre, build, optional);
    }

    /**
     * Converts old java version schema to new one and parse it.
     * It actually cut "1." prefix form version string if present. E.g. version '1.8' is treated as '8'.
     * Version schema was changed since JDK 9 (see <a href='https://openjdk.org/jeps/223'>JEP 223: New Version-String Scheme</a>)
     *
     * @param s
     *          A string to interpret as a version
     *
     * @throws  IllegalArgumentException
     *          If the given string cannot be interpreted as a valid
     *          version
     *
     * @throws  NullPointerException
     *          If the given string is {@code null}
     *
     * @throws  NumberFormatException
     *          If an element of the version number or the build number
     *          cannot be represented as an {@link Integer}
     *
     * @return  The Version of the given string
     *
     */
    public static JavaVersion parseToJdk9Schema(String s) {
        if (s.startsWith("1.")) {
            s = s.substring(2);
        }
        return parse(s);
    }

    /**
     * Current runtime JDK version in Java 9 version schema (see <a href='https://openjdk.org/jeps/223'>JEP 223: New Version-String Scheme</a>)
     *
     * @return  The version of runtime JDK
     */
    public static JavaVersion current(){
        return parseToJdk9Schema(System.getProperty("java.version"));
    }

    private static boolean isSimpleNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char lowerBound = (i > 0) ? '0' : '1';
            if (c < lowerBound || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the value of the <a href="#FEATURE">feature</a> element of
     * the version number.
     *
     * As of Java&nbsp;SE 10, the first element of a version
     * number is not the major-release number but the feature-release
     * counter, incremented for every time-based release.
     *
     * @return The value of the feature element
     *
     */
    public int feature() {
        return version.get(0);
    }

    /**
     * Returns the value of the <a href="#INTERIM">interim</a> element of
     * the version number, or zero if it is absent.
     *
     * @return The value of the interim element, or zero
     *
     */
    public int interim() {
        return (version.size() > 1 ? version.get(1) : 0);
    }

    /**
     * Returns the value of the <a href="#UPDATE">update</a> element of the
     * version number, or zero if it is absent.
     *
     * @return The value of the update element, or zero
     *
     */
    public int update() {
        return (version.size() > 2 ? version.get(2) : 0);
    }

    /**
     * Returns the value of the <a href="#PATCH">patch</a> element of the
     * version number, or zero if it is absent.
     *
     * @return The value of the patch element, or zero
     *
     */
    public int patch() {
        return (version.size() > 3 ? version.get(3) : 0);
    }


    /**
     * Returns an unmodifiable {@link java.util.List List} of the integers
     * represented in the <a href="#verNum">version number</a>.
     * The {@code List} always contains at least one element corresponding to
     * the <a href="#FEATURE">feature version number</a>.
     *
     * @return  An unmodifiable list of the integers
     *          represented in the version number
     */
    public List<Integer> version() {
        return version;
    }

    /**
     * Returns the optional <a href="#pre">pre-release</a> information.
     *
     * @return  The optional pre-release information as a String
     */
    public Optional<String> pre() {
        return pre;
    }

    /**
     * Returns the <a href="#build">build number</a>.
     *
     * @return  The optional build number.
     */
    public Optional<Integer> build() {
        return build;
    }

    /**
     * Returns <a href="#opt">optional</a> additional identifying build
     * information.
     *
     * @return  Additional build information as a String
     */
    public Optional<String> optional() {
        return optional;
    }

    /**
     * Compares this version to another.
     *
     * <p> Each of the components in the <a href="#verStr">version</a> is
     * compared in the following order of precedence: version numbers,
     * pre-release identifiers, build numbers, optional build information.
     * </p>
     *
     * <p> Comparison begins by examining the sequence of version numbers.
     * If one sequence is shorter than another, then the missing elements
     * of the shorter sequence are considered to be less than the
     * corresponding elements of the longer sequence. </p>
     *
     * <p> A version with a pre-release identifier is always considered to
     * be less than a version without one.  Pre-release identifiers are
     * compared numerically when they consist only of digits, and
     * lexicographically otherwise.  Numeric identifiers are considered to
     * be less than non-numeric identifiers.  </p>
     *
     * <p> A version without a build number is always less than one with a
     * build number; otherwise build numbers are compared numerically. </p>
     *
     * <p> The optional build information is compared lexicographically.
     * During this comparison, a version with optional build information is
     * considered to be greater than a version without one. </p>
     *
     * @param  obj
     *         The object to be compared
     *
     * @return  A negative integer, zero, or a positive integer if this
     *          {@code Version} is less than, equal to, or greater than the
     *          given {@code Version}
     *
     * @throws  NullPointerException
     *          If the given object is {@code null}
     */
    @Override
    public int compareTo(JavaVersion obj) {
        return compare(obj, false);
    }

    /**
     * Compares this version to another disregarding optional build
     * information.
     *
     * <p> Two versions are compared by examining the version string as
     * described in {@link #compareTo(JavaVersion)} with the exception that the
     * optional build information is always ignored. </p>
     *
     * <p> This method provides ordering which is consistent with
     * {@code equalsIgnoreOptional()}. </p>
     *
     * @param  obj
     *         The object to be compared
     *
     * @return  A negative integer, zero, or a positive integer if this
     *          {@code Version} is less than, equal to, or greater than the
     *          given {@code Version}
     *
     * @throws  NullPointerException
     *          If the given object is {@code null}
     */
    public int compareToIgnoreOptional(JavaVersion obj) {
        return compare(obj, true);
    }


    private int compare(JavaVersion obj, boolean ignoreOpt) {
        if (obj == null)
            throw new NullPointerException();

        int ret = compareVersion(obj);
        if (ret != 0)
            return ret;

        ret = comparePre(obj);
        if (ret != 0)
            return ret;

        ret = compareBuild(obj);
        if (ret != 0)
            return ret;

        if (!ignoreOpt)
            return compareOptional(obj);

        return 0;
    }

    private int compareVersion(JavaVersion obj) {
        int size = version.size();
        int oSize = obj.version().size();
        int min = Math.min(size, oSize);
        for (int i = 0; i < min; i++) {
            int val = version.get(i);
            int oVal = obj.version().get(i);
            if (val != oVal)
                return val - oVal;
        }
        return size - oSize;
    }

    private int comparePre(JavaVersion obj) {
        Optional<String> oPre = obj.pre();
        if (!pre.isPresent()) {
            if (oPre.isPresent())
                return 1;
        } else {
            if (!oPre.isPresent())
                return -1;
            String val = pre.get();
            String oVal = oPre.get();
            if (val.matches("\\d+")) {
                return (oVal.matches("\\d+")
                        ? (new BigInteger(val)).compareTo(new BigInteger(oVal))
                        : -1);
            } else {
                return (oVal.matches("\\d+")
                        ? 1
                        : val.compareTo(oVal));
            }
        }
        return 0;
    }

    private int compareBuild(JavaVersion obj) {
        Optional<Integer> oBuild = obj.build();
        if (oBuild.isPresent()) {
            return (build.isPresent()
                    ? build.get().compareTo(oBuild.get())
                    : -1);
        } else if (build.isPresent()) {
            return 1;
        }
        return 0;
    }

    private int compareOptional(JavaVersion obj) {
        Optional<String> oOpt = obj.optional();
        if (!optional.isPresent()) {
            if (oOpt.isPresent())
                return -1;
        } else {
            if (!oOpt.isPresent())
                return 1;
            return optional.get().compareTo(oOpt.get());
        }
        return 0;
    }

    /**
     * Returns a string representation of this version.
     *
     * @return  The version string
     */
    @Override
    public String toString() {
        StringBuilder sb
                = new StringBuilder(version.stream()
                .map(Object::toString)
                .collect(Collectors.joining(".")));

        pre.ifPresent(v -> sb.append("-").append(v));

        if (build.isPresent()) {
            sb.append("+").append(build.get());
            if (optional.isPresent())
                sb.append("-").append(optional.get());
        } else {
            if (optional.isPresent()) {
                sb.append(pre.isPresent() ? "-" : "+-");
                sb.append(optional.get());
            }
        }

        return sb.toString();
    }

    /**
     * Determines whether this {@code Version} is equal to another object.
     *
     * <p> Two {@code Version}s are equal if and only if they represent the
     * same version string.
     *
     * @param  obj
     *         The object to which this {@code Version} is to be compared
     *
     * @return  {@code true} if, and only if, the given object is a {@code
     *          Version} that is identical to this {@code Version}
     *
     */
    @Override
    public boolean equals(Object obj) {
        boolean ret = equalsIgnoreOptional(obj);
        if (!ret)
            return false;

        JavaVersion that = (JavaVersion)obj;
        return (this.optional().equals(that.optional()));
    }

    /**
     * Determines whether this {@code Version} is equal to another
     * disregarding optional build information.
     *
     * <p> Two {@code Version}s are equal if and only if they represent the
     * same version string disregarding the optional build information.
     *
     * @param  obj
     *         The object to which this {@code Version} is to be compared
     *
     * @return  {@code true} if, and only if, the given object is a {@code
     *          Version} that is identical to this {@code Version}
     *          ignoring the optional build information
     *
     */
    public boolean equalsIgnoreOptional(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof JavaVersion))
            return false;

        JavaVersion that = (JavaVersion)obj;
        return (this.version().equals(that.version())
                && this.pre().equals(that.pre())
                && this.build().equals(that.build()));
    }

    /**
     * Returns the hash code of this version.
     *
     * @return  The hashcode of this version
     */
    @Override
    public int hashCode() {
        int h = 1;
        int p = 17;

        h = p * h + version.hashCode();
        h = p * h + pre.hashCode();
        h = p * h + build.hashCode();
        h = p * h + optional.hashCode();

        return h;
    }

    private static class VersionPattern {
        // $VNUM(-$PRE)?(\+($BUILD)?(\-$OPT)?)?
        // RE limits the format of version strings
        // ([1-9][0-9]*(?:(?:\.0)*\.[1-9][0-9]*)*)(?:-([a-zA-Z0-9]+))?(?:(\+)(0|[1-9][0-9]*)?)?(?:-([-a-zA-Z0-9.]+))?

        private static final String VNUM
                = "(?<VNUM>[1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)";
        private static final String PRE      = "(?:-(?<PRE>[a-zA-Z0-9]+))?";
        private static final String BUILD
                = "(?:(?<PLUS>\\+)(?<BUILD>0|[1-9][0-9]*)?)?";
        private static final String OPT      = "(?:-(?<OPT>[-a-zA-Z0-9.]+))?";
        private static final String VSTR_FORMAT = VNUM + PRE + BUILD + OPT;

        static final Pattern VSTR_PATTERN = Pattern.compile(VSTR_FORMAT);

        static final String VNUM_GROUP  = "VNUM";
        static final String PRE_GROUP   = "PRE";
        static final String PLUS_GROUP  = "PLUS";
        static final String BUILD_GROUP = "BUILD";
        static final String OPT_GROUP   = "OPT";
    }
}

