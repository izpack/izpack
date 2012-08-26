package com.izforge.izpack.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.izforge.izpack.test.util.ClassUtils;

/**
 * Rule for unloading last jar
 *
 * @author Anthonin Bonnefoy
 */
public class UnloadJarRule implements TestRule
{
    /**
     * Modifies the method-running {@link Statement} to implement this test-running rule.
     *
     * @param base        The {@link org.junit.runners.model.Statement} to be modified
     * @param description A {@link org.junit.runner.Description} of the test implemented in {@code base}
     * @return a new statement, which may be the same as {@code base}, a wrapper around {@code base}, or a completely
     *         new Statement.
     */
    @Override
    public Statement apply(Statement base, Description description)
    {
        return statement(base);
    }

    private Statement statement(final Statement base)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                try
                {
                    base.evaluate();
                }
                finally
                {
                    ClassUtils.unloadLastJar();
                }
            }
        };
    }

}
