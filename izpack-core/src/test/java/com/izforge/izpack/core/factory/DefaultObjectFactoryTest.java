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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.core.container.DefaultContainer;
import org.junit.jupiter.api.Test;

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
        assertThat(a1).isNotNull();
        assertThat(a1).isNotInstanceOf(C.class);

        B b1 = factory.create(B.class);
        assertThat(b1).isNotNull();

        // verify create() returns a new instance for the same type
        A a2 = factory.create(A.class);
        assertThat(a2).isNotInstanceOf(C.class);
        assertThat(a2).isNotNull();
        assertThat(a1).isNotSameAs(a2);
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
        assertThat(c).isNotNull();
        assertThat(c.a).isSameAs(a1); // verify A instance was injected

        A a2 = factory.create(A.class);
        assertThat(a2).isNotNull();
        assertThat(a2).isNotSameAs(a1);
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
        assertThat(d1).isNotNull();
        assertThat(d1.a).isSameAs(a);
        assertThat(d1.b).isSameAs(b);

        // verify order is unimportant for parameters
        D d2 = factory.create(D.class, b, a);
        assertThat(d2).isNotNull();
        assertThat(d1).isNotSameAs(d2);
        assertThat(d2.a).isSameAs(a);
        assertThat(d2.b).isSameAs(b);
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
        assertThat(a1).isNotNull();
        assertThat(a2).isNotNull();
        assertThat(a2).isNotSameAs(a1);

        container.addComponent(A.class, new A());
        A c1 = factory.create(C.class.getName(), A.class);
        assertThat(c1).isNotNull();
        assertThat(c1).isInstanceOf(C.class);

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
        assertThat(d1).isNotNull();
        assertThat(((D) d1).a).isSameAs(a);
        assertThat(((D) d1).b).isSameAs(b);

        // verify order is unimportant for parameters
        Object d2 = factory.create(D.class.getName(), Object.class, b, a);
        assertThat(d2).isNotNull();
        assertThat(d1).isNotSameAs(d2);
        assertThat(((D) d2).a).isSameAs(a);
        assertThat(((D) d2).b).isSameAs(b);
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
        assertThat(c).isNotNull();
        assertThat(c).isInstanceOf(C.class);
        assertThat(((C) c).a).isSameAs(a1); // verify A instance was injected
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
