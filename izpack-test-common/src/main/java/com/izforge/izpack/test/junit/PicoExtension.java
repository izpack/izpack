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

import com.izforge.izpack.api.container.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

/** Creates each test instance through Pico and releases its invocation-owned container. */
public class PicoExtension extends PlatformExtension implements TestInstanceFactory, AfterEachCallback
{
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PicoExtension.class);

    @Override
    public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext context)
    {
        return ExtensionContextScope.TEST_METHOD;
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context)
    {
        if (context.getTestInstanceLifecycle().orElse(TestInstance.Lifecycle.PER_METHOD)
                != TestInstance.Lifecycle.PER_METHOD)
        {
            throw new TestInstantiationException("PicoExtension requires PER_METHOD test lifecycle");
        }
        if (SwingUtilities.isEventDispatchThread())
        {
            throw new TestInstantiationException("Pico containers must be created outside the Swing EDT");
        }
        Class<?> testClass = factoryContext.getTestClass();
        com.izforge.izpack.test.Container annotation = testClass.getAnnotation(com.izforge.izpack.test.Container.class);
        if (annotation == null)
        {
            throw new TestInstantiationException("PicoExtension requires @Container on " + testClass.getName());
        }
        Invocation invocation = new Invocation();
        context.getStore(NAMESPACE).put(Invocation.class, invocation);
        try
        {
            Method method = context.getRequiredTestMethod();
            if (!matches(method, new com.izforge.izpack.util.Platforms().getCurrentPlatform())
                    || !OsEligibility.matches(context)
                    || method.isAnnotationPresent(org.junit.jupiter.api.Disabled.class))
            {
                // Jupiter requires an instance before checking method conditions. Avoid running
                // either a test constructor or a Pico container for an ineligible method.
                return new org.objenesis.ObjenesisStd().newInstance(testClass);
            }
            invocation.container = createContainer(annotation.value(), testClass, method);
            invocation.container.addComponent(testClass);
            Object[] instance = new Object[1];
            SwingUtilities.invokeAndWait(() -> instance[0] = invocation.container.getComponent(testClass));
            if (instance[0] == null)
            {
                throw new TestInstantiationException("Container returned no instance of " + testClass.getName());
            }
            return instance[0];
        }
        catch (Throwable failure)
        {
            Throwable cause = failure instanceof InvocationTargetException && failure.getCause() != null
                    ? failure.getCause() : failure;
            try
            {
                invocation.close();
            }
            catch (Throwable cleanup)
            {
                cause.addSuppressed(cleanup);
            }
            throw new TestInstantiationException("Cannot construct " + testClass.getName(), cause);
        }
    }

    private Container createContainer(Class<? extends Container> type, Class<?> testClass, Method method)
            throws ReflectiveOperationException
    {
        try
        {
            return type.getConstructor(Class.class, Method.class).newInstance(testClass, method);
        }
        catch (NoSuchMethodException absent)
        {
            try
            {
                return type.getConstructor(Class.class).newInstance(testClass);
            }
            catch (NoSuchMethodException alsoAbsent)
            {
                return type.getConstructor().newInstance();
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception
    {
        Invocation invocation = context.getStore(NAMESPACE).get(Invocation.class, Invocation.class);
        if (invocation != null)
        {
            try
            {
                invocation.close();
            }
            catch (Exception cleanup)
            {
                if (context.getExecutionException().isPresent())
                {
                    context.getExecutionException().get().addSuppressed(cleanup);
                }
                else
                {
                    throw cleanup;
                }
            }
        }
    }

    private static final class Invocation implements AutoCloseable
    {
        private final Thread thread = Thread.currentThread();
        private final ClassLoader classLoader = thread.getContextClassLoader();
        private Container container;
        private boolean closed;

        @Override
        public void close()
        {
            if (!closed)
            {
                closed = true;
                try
                {
                    if (container != null)
                    {
                        container.dispose();
                    }
                }
                finally
                {
                    thread.setContextClassLoader(classLoader);
                }
            }
        }
    }
}
