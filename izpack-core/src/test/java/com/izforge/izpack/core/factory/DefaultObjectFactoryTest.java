/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.core.factory;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.core.container.DefaultContainer;


/**
 * Tests the {@link DefaultObjectFactory} class.
 *
 * @author Tim Anderson
 */
public class DefaultObjectFactoryTest
{

    /**
     * The container.
     */
    private final Container container;

    /**
     * The factory.
     */
    private final ObjectFactory factory;


    /**
     * Constructs a <tt>DefaultObjectFactoryTest</tt>.
     */
    public DefaultObjectFactoryTest()
    {
        container = new DefaultContainer();
        factory = new DefaultObjectFactory(container);
    }

    /**
     * Tests the {@link DefaultObjectFactory#create(Class, Object...)} method with no <tt>parameters</tt> arguments.
     */
    @Test
    public void testCreateNoParameters()
    {
        container.addComponent(C.class, new C(new A())); // should not be returned by the factory

        A a1 = factory.create(A.class);
        assertNotNull(a1);
        assertFalse(a1 instanceof C);

        B b1 = factory.create(B.class);
        assertNotNull(b1);

        // verify create() returns a new instance for the same type
        A a2 = factory.create(A.class);
        assertFalse(a2 instanceof C);
        assertNotNull(a2);
        assertNotSame(a2, a1);
    }

    /**
     * Tests the {@link DefaultObjectFactory#create(Class, Object...)} method with dependency injection.
     */
    @Test
    public void testCreateWithInjection()
    {
        A a1 = new A();
        container.addComponent(A.class, a1);

        C c = factory.create(C.class);
        assertNotNull(c);
        assertSame(a1, c.a); // verify A instance was injected

        A a2 = factory.create(A.class);
        assertNotNull(a2);
        assertNotSame(a1, a2);
    }

    /**
     * Tests the {@link DefaultObjectFactory#create(Class, Object...)} method with parameters.
     */
    @Test
    public void testCreateWithParameters()
    {
        A a = new A();
        B b = new B();

        D d1 = factory.create(D.class, a, b);
        assertNotNull(d1);
        assertSame(a, d1.a);
        assertSame(b, d1.b);

        // verify order is unimportant for parameters
        D d2 = factory.create(D.class, b, a);
        assertNotNull(d2);
        assertNotSame(d2, d1);
        assertSame(a, d2.a);
        assertSame(b, d2.b);
    }

    /**
     * Tests the {@link DefaultObjectFactory#create(String, Class, Object...)} method with no
     * <tt>parameters</tt> arguments.
     */
    @Test
    public void testCreateByClassNameNoParameters()
    {
        A a1 = factory.create(A.class.getName(), A.class);
        A a2 = factory.create(A.class.getName(), A.class);
        assertNotNull(a1);
        assertNotNull(a2);
        assertNotSame(a1, a2);

        container.addComponent(A.class, new A());
        A c1 = factory.create(C.class.getName(), A.class);
        assertNotNull(c1);
        assertTrue(c1 instanceof C);

        // now try and create an instance  which doesn't extend the specified superType
        try
        {
            factory.create(B.class.getName(), A.class);
            fail("Expected ClassCastException");
        }
        catch (ClassCastException expected)
        {
            // do nothing
        }
    }

    /**
     * Tests the {@link DefaultObjectFactory#create(String, Class, Object...)} method with parameters.
     */
    @Test
    public void testCreateByClassNameWithParameters()
    {
        A a = new A();
        B b = new B();

        Object d1 = factory.create(D.class.getName(), Object.class, a, b);
        assertNotNull(d1);
        assertSame(a, ((D) d1).a);
        assertSame(b, ((D) d1).b);

        // verify order is unimportant for parameters
        Object d2 = factory.create(D.class.getName(), Object.class, b, a);
        assertNotNull(d2);
        assertNotSame(d2, d1);
        assertSame(a, ((D) d2).a);
        assertSame(b, ((D) d2).b);
    }

    /**
     * Tests the {@link DefaultObjectFactory#create(String, Class, Object...)} method with dependency injection.
     */
    @Test
    public void testCreateByClassNameWithInjection()
    {
        A a1 = new A();
        container.addComponent(A.class, a1);

        A c = factory.create(C.class.getName(), A.class);
        assertNotNull(c);
        assertTrue(c instanceof C);
        assertSame(a1, ((C) c).a); // verify A instance was injected
    }

    public static class A
    {

    }

    public static class B
    {

    }

    public static class C extends A
    {

        public final A a;

        public C(A a)
        {
            this.a = a;
        }
    }

    public static class D
    {
        public final A a;
        public final B b;

        public D(A a, B b)
        {
            this.a = a;
            this.b = b;
        }

    }

}
