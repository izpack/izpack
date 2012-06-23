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

package com.izforge.izpack.api.resource;


import java.text.MessageFormat;
import java.util.Map;

/**
 * Locale-specific messages.
 *
 * @author Tim Anderson
 */
public interface Messages
{

    /**
     * Formats the message with the specified identifier, replacing placeholders with the supplied arguments.
     * <p/>
     * This uses {@link MessageFormat} to format the message.
     *
     * @param id   the message identifier
     * @param args message arguments to replace placeholders in the message with
     * @return the corresponding message, or {@code id} if the message does not exist
     */
    String get(String id, Object... args);

    /**
     * Adds messages.
     * <p/>
     * This merges the supplied messages with the current messages. If an existing message exists with the same
     * identifier as that supplied, it will be replaced.
     *
     * @param messages the messages to add
     */
    void add(Messages messages);

    /**
     * Returns the messages.
     *
     * @return the message identifiers, and their corresponding formats
     */
    Map<String, String> getMessages();

    /**
     * Creates a new messages instance from the named resource that inherits the current messages.
     *
     * @param name the messages resource name
     * @return the messages
     */
    Messages newMessages(String name);
}