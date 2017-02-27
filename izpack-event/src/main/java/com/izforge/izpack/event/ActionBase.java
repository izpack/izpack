/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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
package com.izforge.izpack.event;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Base class for action listeners.
 *
 * @author Klaus Bartz
 */
public class ActionBase implements Serializable
{
    // --- String constants for parsing the XML specification -----------------
    // --- These definitions are placed here because the const strings are -----
    // --- used by more than one InstallerListener and UninstallerListener -----
    // --- class. --------------------------------------------------------------

    private static final long serialVersionUID = 3690478013149884728L;

    public static final String BEFOREPACK = "beforepack";
    public static final String AFTERPACK = "afterpack";
    public static final String BEFOREPACKS = "beforepacks";
    public static final String AFTERPACKS = "afterpacks";
    public static final String BEFOREDELETION = "beforedeletion";
    public static final String AFTERDELETION = "afterdeletion";

    public static final String ORDER = "order";
    public static final String UNINSTALL_ORDER = "uninstall_order";

    private static final HashSet<String> INSTALL_ORDERS = new HashSet<String>();
    private static final HashSet<String> UNINSTALL_ORDERS = new HashSet<String>();

    protected String uninstallOrder = BEFOREDELETION;
    protected String order = null;
    protected String messageID = null;

    static
    {
        INSTALL_ORDERS.add(BEFOREPACK);
        INSTALL_ORDERS.add(AFTERPACK);
        INSTALL_ORDERS.add(BEFOREPACKS);
        INSTALL_ORDERS.add(AFTERPACKS);
        UNINSTALL_ORDERS.add(BEFOREDELETION);
        UNINSTALL_ORDERS.add(AFTERDELETION);
    }

    /**
     * Default constructor
     */
    public ActionBase()
    {
        super();
    }

    /**
     * Returns the order.
     *
     * @return the order
     */
    public String getOrder()
    {
        return order;
    }

    /**
     * Sets the order to the given string. Valid values are "beforepacks",
     * "beforepack", "afterpack" and "afterpacks".
     *
     * @param order order to be set
     */
    public void setOrder(String order) throws Exception
    {
        if (!INSTALL_ORDERS.contains(order))
        {
            throw new Exception("Bad value for order.");
        }
        this.order = order;
    }

    /**
     * Returns the order for uninstallation.
     *
     * @return the order for uninstallation
     */
    public String getUninstallOrder()
    {
        return uninstallOrder;
    }

    /**
     * Sets the order to the given string for uninstallation. Valid values are
     * "beforedeletion" and "afterdeletion".
     *
     * @param order order to be set
     */
    public void setUninstallOrder(String order) throws Exception
    {
        if (!UNINSTALL_ORDERS.contains(order))
        {
            throw new Exception("Bad value for order.");
        }
        this.uninstallOrder = order;
    }

    /**
     * Returns the defined message ID for this action.
     *
     * @return the defined message ID
     */
    public String getMessageID()
    {
        return messageID;
    }

    /**
     * Sets the message ID to the given string.
     *
     * @param messageID string to be used as message ID
     */
    public void setMessageID(String messageID)
    {
        this.messageID = messageID;
    }

}
