package com.izforge.izpack.test.junit;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;


/**
 * A JUnit test class runner that supports running tests on specific platforms via the {@link RunOn} annotation.
 *
 * @author Tim Anderson
 */
public class PlatformRunner extends BlockJUnit4ClassRunner
{

    /**
     * Constructs an {@code PlatformRunner}.
     *
     * @param klass the test class
     * @throws InitializationError if the test class is malformed
     */
    public PlatformRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
    }

    /**
     * Runs the test corresponding to {@code method}, unless it is ignored, or is not intended to be run on
     * the current platform.
     *
     * @param method   the test method
     * @param notifier the run notifier
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier)
    {
        RunOn runOn = method.getAnnotation(RunOn.class);
        if (runOn == null)
        {
            runOn = method.getMethod().getDeclaringClass().getAnnotation(RunOn.class);
        }
        boolean ignore = false;
        if (runOn != null)
        {
            Platform platform = new Platforms().getCurrentPlatform();
            boolean found = false;
            for (Platform.Name name : runOn.value())
            {
                if (platform.isA(name))
                {
                    found = true;
                    break;
                }
            }
            ignore = !found;
        }

        if (!ignore)
        {
            super.runChild(method, notifier);
        }
        else
        {
            notifier.fireTestIgnored(describeChild(method));
        }
    }
}
