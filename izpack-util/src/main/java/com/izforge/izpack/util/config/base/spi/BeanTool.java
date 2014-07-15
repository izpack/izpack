/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2014 René Krell
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

package com.izforge.izpack.util.config.base.spi;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.TimeZone;

public class BeanTool
{
    private static final String PARSE_METHOD = "valueOf";
    private static final BeanTool INSTANCE = ServiceFinder.findService(BeanTool.class);

    public static final BeanTool getInstance()
    {
        return INSTANCE;
    }

    public void inject(Object bean, BeanAccess props)
    {
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass()))
        {
            try
            {
                Method method = pd.getWriteMethod();
                String name = pd.getName();

                if ((method != null) && (props.propLength(name) != 0))
                {
                    Object value;

                    if (pd.getPropertyType().isArray())
                    {
                        value = Array.newInstance(pd.getPropertyType().getComponentType(), props.propLength(name));
                        for (int i = 0; i < props.propLength(name); i++)
                        {
                            Array.set(value, i, parse(props.propGet(name, i), pd.getPropertyType().getComponentType()));
                        }
                    }
                    else
                    {
                        value = parse(props.propGet(name), pd.getPropertyType());
                    }

                    method.invoke(bean, value);
                }
            }
            catch (Exception x)
            {
                throw (IllegalArgumentException) (new IllegalArgumentException("Failed to set property: " + pd.getDisplayName()).initCause(
                        x));
            }
        }
    }

    public void inject(BeanAccess props, Object bean)
    {
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass()))
        {
            try
            {
                Method method = pd.getReadMethod();

                if ((method != null) && !"class".equals(pd.getName()))
                {
                    Object value = method.invoke(bean, (Object[]) null);

                    if (value != null)
                    {
                        if (pd.getPropertyType().isArray())
                        {
                            for (int i = 0; i < Array.getLength(value); i++)
                            {
                                Object v = Array.get(value, i);

                                if ((v != null) && !v.getClass().equals(String.class))
                                {
                                    v = v.toString();
                                }

                                props.propAdd(pd.getName(), (String) v);
                            }
                        }
                        else
                        {
                            props.propSet(pd.getName(), value.toString());
                        }
                    }
                }
            }
            catch (Exception x)
            {
                throw new IllegalArgumentException("Failed to set property: " + pd.getDisplayName(), x);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(String value, Class<T> clazz) throws IllegalArgumentException
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException("null argument");
        }

        Object o = null;

        if (value == null)
        {
            o = zero(clazz);
        }
        else if (clazz.isPrimitive())
        {
            o = parsePrimitiveValue(value, clazz);
        }
        else
        {
            if (clazz == String.class)
            {
                o = value;
            }
            else if (clazz == Character.class)
            {
                o = new Character(value.charAt(0));
            }
            else
            {
                o = parseSpecialValue(value, clazz);
            }
        }

        return (T) o;
    }

    public <T> T proxy(Class<T> clazz, BeanAccess props)
    {
        return clazz.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { clazz },
                    new BeanInvocationHandler(props)));
    }

    @SuppressWarnings("unchecked")
    public <T> T zero(Class<T> clazz)
    {
        Object o = null;

        if (clazz.isPrimitive())
        {
            if (clazz == Boolean.TYPE)
            {
                o = Boolean.FALSE;
            }
            else if (clazz == Byte.TYPE)
            {
                o = Byte.valueOf((byte) 0);
            }
            else if (clazz == Character.TYPE)
            {
                o = new Character('\0');
            }
            else if (clazz == Double.TYPE)
            {
                o = new Double(0.0);
            }
            else if (clazz == Float.TYPE)
            {
                o = new Float(0.0f);
            }
            else if (clazz == Integer.TYPE)
            {
                o = Integer.valueOf(0);
            }
            else if (clazz == Long.TYPE)
            {
                o = Long.valueOf(0L);
            }
            else if (clazz == Short.TYPE)
            {
                o = Short.valueOf((short) 0);
            }
        }

        return (T) o;
    }

    protected <T> Object parseSpecialValue(String value, Class<T> clazz) throws IllegalArgumentException
    {
        Object o;

        try
        {
            if (clazz == File.class)
            {
                o = new File(value);
            }
            else if (clazz == URL.class)
            {
                o = new URL(value);
            }
            else if (clazz == URI.class)
            {
                o = new URI(value);
            }
            else if (clazz == Class.class)
            {
                o = Class.forName(value);
            }
            else if (clazz == TimeZone.class)
            {
                o = TimeZone.getTimeZone(value);
            }
            else
            {

                // TODO handle constructor with String arg as converter from String
                // look for "valueOf" converter method
                Method parser = clazz.getMethod(PARSE_METHOD, new Class[] { String.class });

                o = parser.invoke(null, new Object[] { value });
            }
        }
        catch (Exception x)
        {
            throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
        }

        return o;
    }

    private <T> PropertyDescriptor[] getPropertyDescriptors(Class<T> clazz)
    {
        try
        {
            return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        }
        catch (IntrospectionException x)
        {
            throw new IllegalArgumentException(x);
        }
    }

    private <T> Object parsePrimitiveValue(String value, Class<T> clazz) throws IllegalArgumentException
    {
        Object o = null;

        try
        {
            if (clazz == Boolean.TYPE)
            {
                o = Boolean.valueOf(value);
            }
            else if (clazz == Byte.TYPE)
            {
                o = Byte.valueOf(value);
            }
            else if (clazz == Character.TYPE)
            {
                o = new Character(value.charAt(0));
            }
            else if (clazz == Double.TYPE)
            {
                o = Double.valueOf(value);
            }
            else if (clazz == Float.TYPE)
            {
                o = Float.valueOf(value);
            }
            else if (clazz == Integer.TYPE)
            {
                o = Integer.valueOf(value);
            }
            else if (clazz == Long.TYPE)
            {
                o = Long.valueOf(value);
            }
            else if (clazz == Short.TYPE)
            {
                o = Short.valueOf(value);
            }
        }
        catch (Exception x)
        {
            throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
        }

        return o;
    }

    static class BeanInvocationHandler extends AbstractBeanInvocationHandler
    {
        private final BeanAccess _backend;

        BeanInvocationHandler(BeanAccess backend)
        {
            _backend = backend;
        }

        @Override protected Object getPropertySpi(String property, Class<?> clazz)
        {
            Object ret = null;

            if (clazz.isArray())
            {
                int length = _backend.propLength(property);

                if (length != 0)
                {
                    String[] all = new String[length];

                    for (int i = 0; i < all.length; i++)
                    {
                        all[i] = _backend.propGet(property, i);
                    }

                    ret = all;
                }
            }
            else
            {
                ret = _backend.propGet(property);
            }

            return ret;
        }

        @Override protected void setPropertySpi(String property, Object value, Class<?> clazz)
        {
            if (clazz.isArray())
            {
                _backend.propDel(property);
                for (int i = 0; i < Array.getLength(value); i++)
                {
                    _backend.propAdd(property, Array.get(value, i).toString());
                }
            }
            else
            {
                _backend.propSet(property, value.toString());
            }
        }

        @Override protected boolean hasPropertySpi(String property)
        {
            return _backend.propLength(property) != 0;
        }
    }
}
