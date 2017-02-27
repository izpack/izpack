/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Klaus Bartz
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
package com.coi.tools.os.win;

import java.util.ArrayList;

/**
 * Data container for access control lists used by the registry stuff in the
 * java and in the native part. DO NOT CHANGE METHODE SIGNATURES etc. without
 * addapt the native methods RegistryImpl.modifyKeyACL and
 * RegistryImpl.getKeyACL.
 *
 * @author Klaus Bartz
 */
public class AccessControlList extends java.util.ArrayList<AccessControlEntry>
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = -5350586385078554562L;
    private ArrayList<AccessControlEntry> permissions = new ArrayList<AccessControlEntry>();

    /**
     * Default constructor.
     */
    public AccessControlList()
    {
        super();
    }

    /**
     * Creates an ACE entry in the permission array with the given values.
     *
     * @param owner owner of the ACE
     * @param allowed access allowed mask
     * @param denied access denied mask
     */
    public void setACE(String owner, int allowed, int denied)
    {
        AccessControlEntry ace = new AccessControlEntry(owner, allowed, denied);
        permissions.add(ace);
    }

    /**
     * Returns the access control entry related to the given id.
     *
     * @param num id in the internal permisson array.
     * @return the access control entry for the given id
     */
    public AccessControlEntry getACE(int num)
    {
        return ((AccessControlEntry) ((permissions.get(num)).clone()));
    }

    /**
     * Returns number of access control entries.
     *
     * @return number of access control entries
     */
    public int getACECount()
    {
        return (permissions.size());
    }

}
