/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright 2004, 2007 IBM Corporation and others.
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

/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

/**
 * Instances of this class represent entry points into Java which can be invoked
 * from operating system level callback routines.
 * <p>
 * IMPORTANT: A callback is only valid when invoked on the thread which created
 * it. The results are undefined (and typically bad) when a callback is passed
 * out to the operating system (or other code) in such a way that the callback
 * is called from a different thread.
 */

public class Callback {

    Object object;

    String method, signature;

    int argCount;

    long /* int */ address, errorResult;

    boolean isStatic, isArrayBased;

    static final String PTR_SIGNATURE = "J"; /* C.PTR_SIZEOF == 4 ? "I" : "J"; */

    static final String SIGNATURE_0 = getSignature(0);
    static final String SIGNATURE_1 = getSignature(1);
    static final String SIGNATURE_2 = getSignature(2);
    static final String SIGNATURE_3 = getSignature(3);
    static final String SIGNATURE_4 = getSignature(4);

    static final String SIGNATURE_N = "([" + PTR_SIGNATURE + ")" + PTR_SIGNATURE;

    /**
     * Constructs a new instance of this class given an object to send the
     * message to, a string naming the method to invoke and an argument count.
     * Note that, if the object is an instance of <code>Class</code> it is
     * assumed that the method is a static method on that class.
     *
     * @param object
     *            the object to send the message to
     * @param method
     *            the name of the method to invoke
     * @param argCount
     *            the number of arguments that the method takes
     */
    public Callback(Object object, String method, int argCount) {
        this(object, method, argCount, false);
    }

    /**
     * Constructs a new instance of this class given an object to send the
     * message to, a string naming the method to invoke, an argument count and a
     * flag indicating whether or not the arguments will be passed in an array.
     * Note that, if the object is an instance of <code>Class</code> it is
     * assumed that the method is a static method on that class.
     *
     * @param object
     *            the object to send the message to
     * @param method
     *            the name of the method to invoke
     * @param argCount
     *            the number of arguments that the method takes
     * @param isArrayBased
     *            <code>true</code> if the arguments should be passed in an
     *            array and false otherwise
     */
    public Callback(Object object, String method, int argCount, boolean isArrayBased) {
        this(object, method, argCount, isArrayBased, 0);
    }

    /**
     * Constructs a new instance of this class given an object to send the
     * message to, a string naming the method to invoke, an argument count, a
     * flag indicating whether or not the arguments will be passed in an array
     * and a value to return when an exception happens. Note that, if the object
     * is an instance of <code>Class</code> it is assumed that the method is a
     * static method on that class.
     *
     * @param object
     *            the object to send the message to
     * @param method
     *            the name of the method to invoke
     * @param argCount
     *            the number of arguments that the method takes
     * @param isArrayBased
     *            <code>true</code> if the arguments should be passed in an
     *            array and false otherwise
     * @param errorResult
     *            the return value if the java code throws an exception
     */
    public Callback(Object object, String method, int argCount, boolean isArrayBased, long /* int */errorResult) {

        /* Set the callback fields */
        this.object = object;
        this.method = method;
        this.argCount = argCount;
        this.isStatic = object instanceof Class<?>;
        this.isArrayBased = isArrayBased;
        this.errorResult = errorResult;

        /* Inline the common cases */
        if (isArrayBased) {
            signature = SIGNATURE_N;
        } else {
            switch (argCount) {
            case 0:
                signature = SIGNATURE_0;
                break; 
            case 1:
                signature = SIGNATURE_1;
                break; 
            case 2:
                signature = SIGNATURE_2;
                break; 
            case 3:
                signature = SIGNATURE_3;
                break; 
            case 4:
                signature = SIGNATURE_4;
                break; 
            default:
                signature = getSignature(argCount);
            }
        }

        /* Bind the address */
        address = bind(this, object, method, signature, argCount, isStatic, isArrayBased, errorResult);
    }

    /**
     * Allocates the native level resources associated with the callback. This
     * method is only invoked from within the constructor for the argument.
     *
     * @param callback
     *            the callback to bind
     * @param object
     *            the callback's object
     * @param method
     *            the callback's method
     * @param signature
     *            the callback's method signature
     * @param argCount
     *            the callback's method argument count
     * @param isStatic
     *            whether the callback's method is static
     * @param isArrayBased
     *            whether the callback's method is array based
     * @param errorResult
     *            the callback's error result
     */
    static native synchronized long /* int */ bind(Callback callback, Object object, String method, String signature, int argCount, boolean isStatic, boolean isArrayBased,
            long /* int */errorResult);

    /**
     * Releases the native level resources associated with the callback, and
     * removes all references between the callback and other objects. This helps
     * to prevent (bad) application code from accidentally holding onto
     * extraneous garbage.
     */
    public void dispose() {
        if (object == null)
            return;
        unbind(this);
        object = method = signature = null;
        address = 0;
    }

    /**
     * Returns the address of a block of machine code which will invoke the
     * callback represented by the receiver.
     *
     * @return the callback address
     */
    public long /* int */getAddress() {
        return address;
    }

    /**
     * Returns the SWT platform name.
     *
     * @return the platform name of the currently running SWT
     */
    public static native String getPlatform();

    /**
     * Returns the number of times the system has been recursively entered
     * through a callback.
     * <p>
     * Note: This should not be called by application code.
     * </p>
     *
     * @return the entry count
     *
     * @since 2.1
     */
    public static native int getEntryCount();

    static String getSignature(int argCount) {
        String signature = "("; //$NON-NLS-1$
        for (int i = 0; i < argCount; i++)
            signature += PTR_SIGNATURE;
        signature += ")" + PTR_SIGNATURE; //$NON-NLS-1$
        return signature;
    }

    /**
     * Indicates whether or not callbacks which are triggered at the native
     * level should cause the messages described by the matching
     * <code>Callback</code> objects to be invoked. This method is used to
     * safely shut down SWT when it is run within environments which can
     * generate spurious events.
     * <p>
     * Note: This should not be called by application code.
     * </p>
     *
     * @param enable
     *            true if callbacks should be invoked
     */
    public static final native synchronized void setEnabled(boolean enable);

    /**
     * Returns whether or not callbacks which are triggered at the native level
     * should cause the messages described by the matching <code>Callback</code>
     * objects to be invoked. This method is used to safely shut down SWT when
     * it is run within environments which can generate spurious events.
     * <p>
     * Note: This should not be called by application code.
     * </p>
     *
     * @return true if callbacks should not be invoked
     */
    public static final native synchronized boolean getEnabled();

    /**
     * Immediately wipes out all native level state associated with <em>all</em>
     * callbacks.
     * <p>
     * <b>WARNING:</b> This operation is <em>extremely</em> dangerous, and
     * should never be performed by application code.
     * </p>
     */
    public static final native synchronized void reset();

    /**
     * Releases the native level resources associated with the callback.
     *
     * @see #dispose
     */
    static final native synchronized void unbind(Callback callback);

}
