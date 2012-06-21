package com.izforge.izpack.test.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;

/**
 * Custom runner for getting dependencies injected in test with PicoContainer
 *
 * @author Anthonin Bonnefoy
 */
public class PicoRunner extends PlatformRunner
{
    private Class<?> klass;
    private FrameworkMethod method;
    private Object currentTestInstance;
    private Class<? extends Container> containerClass;
    private Container containerInstance;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PicoRunner.class.getName());

    public PicoRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
        this.klass = klass;
    }

    @Override
    protected void validateConstructor(List<Throwable> errors)
    {
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method)
    {
        this.method = method;
        Statement statement = super.methodBlock(method);
        try
        {
            for (Field field : containerClass.getFields())
            {
                Annotation annotation = field.getAnnotation(Rule.class);
                if (annotation != null)
                {
                    TestRule rule = (TestRule) field.get(containerInstance);
                    Description description = Description.createTestDescription(
                            method.getMethod().getDeclaringClass(), method.getName());
                    statement = rule.apply(statement, description);

                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new IzPackException(e);
        }
        return statement;
    }


    @Override
    protected Object createTest() throws Exception
    {
        containerClass = getTestClass().getJavaClass().getAnnotation(com.izforge.izpack.test.Container.class).value();
        logger.info("Creating test=" + getTestClass().getName());
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    containerInstance = createContainer(containerClass);
                    containerInstance.addComponent(klass);
                    currentTestInstance = containerInstance.getComponent(klass);
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new IzPackException(e);
                }
            }
        });
        return currentTestInstance;
    }

    private Container createContainer(Class<? extends Container> containerClass)
            throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
    {
        Constructor<? extends Container> constructor;
        Container result;
        try
        {
            constructor = containerClass.getConstructor(klass.getClass(), method.getClass());
            result = constructor.newInstance(klass, method);
        }
        catch (NoSuchMethodException exception)
        {
            try
            {
                constructor = containerClass.getConstructor(klass.getClass());
                result = constructor.newInstance(klass);
            }
            catch (NoSuchMethodException nested)
            {
                constructor = containerClass.getConstructor();
                result = constructor.newInstance();
            }
        }
        return result;
    }

}