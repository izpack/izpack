/*
 * IzPack - Copyright 2001-2026 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.test.junit;

import static com.izforge.izpack.test.junit.PlatformExtension.matches;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.List.of;
import static javax.swing.SwingUtilities.isEventDispatchThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.core.LauncherFactory.create;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

class ExtensionsTest
{
    private static final List<String> events = new ArrayList<>();
    private static String failure;
    private static ClassLoader loader;

    @BeforeEach
    void reset()
    {
        events.clear();
        failure = "";
        loader = currentThread().getContextClassLoader();
    }

    private TestExecutionSummary run(Class<?> type)
    {
        return run(type, "");
    }

    private TestExecutionSummary run(Class<?> type, String deactivatedConditions)
    {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        create().execute(request()
                .configurationParameter("junit.jupiter.conditions.deactivate", deactivatedConditions)
                .selectors(selectClass(type)).build(), listener);
        assertThat(currentThread().getContextClassLoader()).isSameAs(loader);
        return listener.getSummary();
    }

    @Test
    void annotationOnlyInheritedAndOverriddenContainers()
    {
        for (Class<?> type : of(AnnotationOnly.class, InheritedContainer.class, OverriddenContainer.class))
        {
            events.clear();
            assertThat(run(type).getTestsSucceededCount()).isEqualTo(1);
            assertThat(events).containsExactly(type == OverriddenContainer.class ? "replacement" : "original",
                    "instance", "constructor", "body", "dispose");
        }
    }

    @Test
    void nativeOsConditionsAvoidConstructionAndHonorDeactivation()
    {
        TestExecutionSummary summary = run(NativeOsInjected.class);
        assertThat(summary.getFailures()).isEmpty();
        assertThat(summary.getTestsSucceededCount()).isEqualTo(2);
        assertThat(summary.getTestsSkippedCount()).isEqualTo(4);
        assertThat(events.stream().filter("original"::equals).count()).isEqualTo(2);
        assertThat(events.stream().filter("constructor"::equals).count()).isEqualTo(2);
        assertThat(events.stream().filter("dispose"::equals).count()).isEqualTo(2);

        for (String pattern : of("*", "org.junit.jupiter.api.condition.*", "*EnabledOnOsCondition,*DisabledOnOsCondition"))
        {
            events.clear();
            summary = run(NativeOsInjected.class, pattern);
            assertThat(summary.getFailures()).isEmpty();
            assertThat(summary.getTestsSucceededCount()).isEqualTo(6);
            assertThat(events.stream().filter("original"::equals).count()).isEqualTo(6);
            assertThat(events.stream().filter("constructor"::equals).count()).isEqualTo(6);
            assertThat(events.stream().filter("dispose"::equals).count()).isEqualTo(6);
        }
    }

    @Test
    void classOsConditionsAndInheritedMethodsUseJupiterSemantics()
    {
        assertThat(run(ExcludedClass.class).getTestsSkippedCount()).isEqualTo(1);
        assertThat(events).isEmpty();
        // Native class OS annotations are not @Inherited, unlike @Container.
        assertThat(run(ChildOfExcludedClass.class).getTestsSucceededCount()).isEqualTo(1);
        assertThat(events).containsExactly("original", "instance", "constructor", "body", "dispose");
        events.clear();
        assertThat(run(InheritedOsMethods.class).getTestsSkippedCount()).isEqualTo(4);
        assertThat(events.stream().filter("original"::equals).count()).isEqualTo(2);
    }

    @Test
    void platformFamiliesAndInheritance() throws Exception
    {
        assertThat(matches(Restrictions.class.getMethod("unrestricted"), Platforms.LINUX)).isTrue();
        assertThat(matches(Restrictions.class.getMethod("family"), Platforms.LINUX)).isTrue();
        assertThat(matches(Restrictions.class.getMethod("empty"), Platforms.LINUX)).isFalse();
        assertThat(matches(Inherited.class.getMethod("allowed"), Platforms.LINUX)).isTrue();
        assertThat(matches(Inherited.class.getMethod("restricted"), Platforms.LINUX)).isFalse();
    }

    @Test
    void methodOverridesClassAndSkipsWithoutConstruction()
    {
        TestExecutionSummary summary = run(Injected.class);
        assertThat(summary.getTestsSucceededCount()).as(summary.getFailures().toString()).isEqualTo(2);
        assertThat(summary.getTestsSkippedCount()).isEqualTo(2);
        assertThat(events).isEqualTo(of("container:first", "instance", "setup", "first", "teardown", "dispose",
                "container:second", "instance", "setup", "second", "teardown", "dispose"));
    }

    @Test
    void classAndNoArgumentFallback()
    {
        assertThat(run(ClassOnly.class).getTestsSucceededCount()).isEqualTo(1);
        assertThat(run(NoArguments.class).getTestsSucceededCount()).isEqualTo(1);
        assertThat(events.stream().filter("dispose"::equals).count()).isEqualTo(2);
    }

    @Test
    void rejectsPerClassLifecycle()
    {
        assertThat(run(PerClass.class).getFailures().isEmpty()).isFalse();
        assertThat(events).isEmpty();
    }

    @Test
    void cleansUpEveryFailurePhase()
    {
        for (String phase : of("construction", "setup", "body", "teardown", "dispose"))
        {
            events.clear();
            failure = phase;
            TestExecutionSummary summary = run(Failing.class);
            assertThat(summary.getFailures().size()).as(phase).isEqualTo(1);
            assertThat(events.stream().filter("dispose"::equals).count()).as(phase).isEqualTo(1);
        }
    }

    @Test
    void preservesBodyAndDisposalFailures()
    {
        failure = "body+dispose";
        Throwable thrown = run(Failing.class).getFailures().get(0).getException();
        assertThat(thrown.getMessage()).isEqualTo("body");
        assertThat(thrown.getSuppressed()[0].getMessage()).isEqualTo("dispose");
    }

    @Test
    void timeoutIncludesSetupBodyAndTeardown()
    {
        TestExecutionSummary summary = run(CumulativeTimeout.class);
        assertThat(summary.getFailures().size()).isEqualTo(1);
        assertThat(currentThread().isInterrupted()).isFalse();
        assertThat(run(QuickTimeout.class).getTestsSucceededCount()).isEqualTo(1);
    }

    @TestTimeout(400)
    public static class CumulativeTimeout
    {
        @BeforeEach void setup() throws InterruptedException { sleep(150); }
        @Test void test() throws InterruptedException { sleep(150); }
        @AfterEach void teardown() throws InterruptedException { sleep(150); }
    }

    @TestTimeout(2000)
    public static class QuickTimeout
    {
        @Test void test() { }
    }

    public static class Restrictions
    {
        public void unrestricted() { }
        @RunOn(Platform.Name.UNIX) public void family() { }
        @RunOn({}) public void empty() { }
    }

    @RunOn(Platform.Name.WINDOWS)
    public static class Parent
    {
        public void restricted() { }
        @RunOn(Platform.Name.UNIX) public void allowed() { }
    }
    public static class Inherited extends Parent { }

    @ExtendWith(PicoExtension.class)
    @com.izforge.izpack.test.Container(MethodContainer.class)
    @RunOn({})
    @TestMethodOrder(MethodName.class)
    public static class Injected
    {
        public Injected(String dependency) { assertThat(dependency).isEqualTo("injected"); }
        @BeforeEach void setup() { events.add("setup"); }
        @AfterEach void teardown() { events.add("teardown"); }
        @Test @RunOn({Platform.Name.UNIX, Platform.Name.WINDOWS}) void first() { events.add("first"); }
        @Test @RunOn({Platform.Name.UNIX, Platform.Name.WINDOWS}) void second() { events.add("second"); }
        @Test void skipped() { fail("Excluded test executed"); }
        @Test @Disabled void disabled() { fail("Disabled test executed"); }
    }

    @ExtendWith(PicoExtension.class)
    @com.izforge.izpack.test.Container(ClassContainer.class)
    public static class ClassOnly
    {
        public ClassOnly(String dependency) { }
        @Test void test() { }
    }

    @ExtendWith(PicoExtension.class)
    @com.izforge.izpack.test.Container(NoArgumentContainer.class)
    public static class NoArguments
    {
        public NoArguments(String dependency) { }
        @Test void test() { }
    }

    @ExtendWith(PicoExtension.class)
    @com.izforge.izpack.test.Container(MethodContainer.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public static class PerClass
    {
        public PerClass(String dependency) { }
        @Test void test() { }
    }

    @ExtendWith(PicoExtension.class)
    @com.izforge.izpack.test.Container(MethodContainer.class)
    public static class Failing
    {
        public Failing(String dependency) { }
        @BeforeEach void setup() { if (failure.equals("setup")) throw new IllegalStateException("setup"); }
        @Test void test() { if (failure.startsWith("body")) throw new IllegalStateException("body"); }
        @AfterEach void teardown() { if (failure.equals("teardown")) throw new IllegalStateException("teardown"); }
    }

    public static class MethodContainer extends StubContainer
    {
        public MethodContainer(Class<?> type, Method method)
        {
            assertThat(isEventDispatchThread()).isFalse();
            events.add("container:" + method.getName());
            assertThat(method.getDeclaringClass()).isSameAs(type);
        }
    }
    public static class ClassContainer extends StubContainer
    {
        public ClassContainer(Class<?> type) { assertThat(type).isEqualTo(ClassOnly.class); }
    }
    public static class NoArgumentContainer extends StubContainer { }

    @com.izforge.izpack.test.Container(OriginalContainer.class)
    public static class AnnotationOnly
    {
        public AnnotationOnly(String dependency) { events.add("constructor"); }
        @Test void test() { events.add("body"); }
    }

    public static class InheritedContainer extends AnnotationOnly
    {
        public InheritedContainer(String dependency) { super(dependency); }
    }

    @com.izforge.izpack.test.Container(ReplacementContainer.class)
    public static class OverriddenContainer extends AnnotationOnly
    {
        public OverriddenContainer(String dependency) { super(dependency); }
    }

    public static class OriginalContainer extends StubContainer
    {
        public OriginalContainer() { events.add("original"); }
    }

    public static class ReplacementContainer extends StubContainer
    {
        public ReplacementContainer() { events.add("replacement"); }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @DisabledOnOs({OS.AIX, OS.FREEBSD, OS.LINUX, OS.MAC, OS.OPENBSD, OS.SOLARIS, OS.WINDOWS, OS.OTHER})
    public @interface ExcludedOnEveryOs { }

    @com.izforge.izpack.test.Container(OriginalContainer.class)
    public static class NativeOsInjected
    {
        public NativeOsInjected(String dependency) { events.add("constructor"); }
        @Test void eligible() { }
        @Test @ExcludedOnEveryOs void composedExcluded() { }
        @Test @EnabledOnOs(architectures = "unsupported-test-architecture") void architectureExcluded() { }
        @Test @DisabledOnOs({OS.AIX, OS.FREEBSD, OS.LINUX, OS.MAC, OS.OPENBSD, OS.SOLARIS, OS.WINDOWS, OS.OTHER})
        void directExcluded() { }
        @Test @EnabledOnOs(value = {OS.AIX, OS.FREEBSD, OS.LINUX, OS.MAC, OS.OPENBSD, OS.SOLARIS, OS.WINDOWS, OS.OTHER},
                architectures = "unsupported-test-architecture") void osAndArchitectureExcluded() { }
        @Test @DisabledOnOs(value = {OS.AIX, OS.FREEBSD, OS.LINUX, OS.MAC, OS.OPENBSD, OS.SOLARIS, OS.WINDOWS, OS.OTHER},
                architectures = "unsupported-test-architecture") void osAndArchitectureEligible() { }
    }

    public static class InheritedOsMethods extends NativeOsInjected
    {
        public InheritedOsMethods(String dependency) { super(dependency); }
    }

    @com.izforge.izpack.test.Container(OriginalContainer.class)
    @EnabledOnOs(architectures = "unsupported-test-architecture")
    public static class ExcludedClass
    {
        public ExcludedClass(String dependency) { events.add("constructor"); }
        @Test void test() { events.add("body"); }
    }

    public static class ChildOfExcludedClass extends ExcludedClass
    {
        public ChildOfExcludedClass(String dependency) { super(dependency); }
    }

    public static class StubContainer implements Container
    {
        public StubContainer()
        {
            assertThat(isEventDispatchThread()).isFalse();
            currentThread().setContextClassLoader(new ClassLoader(loader) { });
        }
        public <T> void addComponent(Class<T> type) { }
        public void addComponent(Object key, Object value) { }
        public <T> T getComponent(Class<T> type)
        {
            assertThat(isEventDispatchThread()).isTrue();
            events.add("instance");
            if (failure.equals("construction")) throw new IllegalStateException("construction");
            try { return type.getConstructor(String.class).newInstance("injected"); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
        public Object getComponent(Object key) { throw new UnsupportedOperationException(); }
        public Container createChildContainer() { throw new UnsupportedOperationException(); }
        public boolean removeChildContainer(Container child) { return false; }
        public void dispose()
        {
            events.add("dispose");
            if (failure.endsWith("dispose")) throw new IllegalStateException("dispose");
        }
        public <T> Class<T> getClass(String name, Class<T> type) { throw new UnsupportedOperationException(); }
    }
}
