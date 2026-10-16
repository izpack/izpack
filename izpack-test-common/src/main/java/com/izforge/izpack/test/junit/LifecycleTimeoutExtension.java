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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;

/** Retains the cumulative lifecycle deadline of the former JUnit timeout rule. */
public class LifecycleTimeoutExtension implements BeforeEachCallback, AfterEachCallback
{
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(LifecycleTimeoutExtension.class);

    @Override
    public void beforeEach(ExtensionContext context)
    {
        long millis = context.getRequiredTestClass().getAnnotation(TestTimeout.class).value();
        if (millis <= 0)
        {
            throw new IllegalArgumentException("TestTimeout must be positive");
        }
        context.getStore(NAMESPACE).put(Deadline.class, new Deadline(millis));
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        Deadline deadline = context.getStore(NAMESPACE).get(Deadline.class, Deadline.class);
        if (deadline != null)
        {
            deadline.close();
            if (deadline.expired)
            {
                AssertionFailedError timeout = new AssertionFailedError("Test lifecycle exceeded " + deadline.millis + " ms");
                if (context.getExecutionException().isPresent())
                {
                    context.getExecutionException().get().addSuppressed(timeout);
                }
                else
                {
                    throw timeout;
                }
            }
        }
    }

    private static final class Deadline implements AutoCloseable
    {
        private final Thread thread = Thread.currentThread();
        private final long millis;
        private final ScheduledExecutorService timer;
        private final ScheduledFuture<?> interrupt;
        private volatile boolean expired;
        private boolean closed;

        Deadline(long millis)
        {
            this.millis = millis;
            timer = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread worker = new Thread(task, "izpack-test-timeout");
                worker.setDaemon(true);
                return worker;
            });
            interrupt = timer.schedule(() -> {
                synchronized (this)
                {
                    if (!closed)
                    {
                        expired = true;
                        thread.interrupt();
                    }
                }
            }, millis, TimeUnit.MILLISECONDS);
        }

        @Override
        public synchronized void close()
        {
            if (!closed)
            {
                closed = true;
                interrupt.cancel(false);
                timer.shutdownNow();
                if (expired && Thread.currentThread() == thread)
                {
                    Thread.interrupted();
                }
            }
        }
    }
}
