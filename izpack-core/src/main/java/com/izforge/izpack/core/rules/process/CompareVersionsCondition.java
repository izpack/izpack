/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

package com.izforge.izpack.core.rules.process;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.CompareCondition;
import com.izforge.izpack.api.rules.ComparisonOperator;
import com.izforge.izpack.core.variable.utils.ValueUtils;

import java.util.*;
import java.util.logging.Logger;

public class CompareVersionsCondition extends CompareCondition
{
    private static final long serialVersionUID = 5605592864539142416L;

    private static final transient Logger logger = Logger.getLogger(CompareVersionsCondition.class.getName());

    private static final Set<String> EMPTY_STRINGS = Collections.singleton("");
    private static final String VERSION_DELIMITER = "[^\\d]+";

    /**
     * Don't assume missing minor parts of some operand as 0 during comparison.
     *
     * Example:
     * Version 1 = 1.8
     * Version 2 = 1.8.0_72
     * <ul>
     * <li>Without {@code NOT_ASSUME_MISSING_MINOR_PARTS_AS_0}:<br>
     *     1.8.0_0 vs. 1.8.0_72 - LESS</li>
     * <li>With {@code NOT_ASSUME_MISSING_MINOR_PARTS_AS_0}:<br>
     *     1.8 vs. 1.8[.0_72] - EQUALS</li>
     * </ul>
     */
    protected static final int NOT_ASSUME_MISSING_MINOR_PARTS_AS_0 = 0x01;

    public CompareVersionsCondition()
    {
        this(0);
    }

    public CompareVersionsCondition(int flags)
    {
        this.flags |= flags;
    }

    /**
     * Version comparison flags.
     */
    protected int flags = 0;

    /**
     * Indicates whether a particular version comparison flag is set or not.
     */
    protected boolean hasFlag(int f) {
        return (flags & f) != 0;
    }

    @Override
    public boolean isTrue()
    {
        logger.fine("Version comparison: " + operand1 + " " + operator + " " + operand2 + " (flags: " + flags + ")");
        boolean result = false;
        InstallData installData = getInstallData();
        if (installData != null && operand1 != null && operand2 != null)
        {
            Variables variables = installData.getVariables();
            String arg1 = variables.replace(operand1);
            String arg2 = variables.replace(operand2);
            if (operator == null)
            {
                operator = ComparisonOperator.EQUAL;
            }
            int res = 0;
            try
            {
                res = new Version(arg1).compareTo(new Version(arg2));
            }
            catch (IllegalArgumentException e)
            {
                logger.warning("[" + getClass().getSimpleName() + "] " + e.getMessage());
                return false;
            }
            logger.finer("Raw version comparison result: " + res);
            switch (operator)
            {
                case EQUAL:
                    result = (res == 0);
                    break;
                case NOTEQUAL:
                    result = (res != 0);
                    break;
                case GREATER:
                    result = (res > 0);
                    break;
                case GREATEREQUAL:
                    result = (res >= 0);
                    break;
                case LESS:
                    result = (res < 0);
                    break;
                case LESSEQUAL:
                    result = (res <= 0);
                    break;
                default:
                    break;
            }
        }
        logger.fine(operand1 + " " + operator.getAttribute() + " " + operand2 + ": " + result);
        return result;
    }

    @Override
    public Set<String> getNeededVariableNames() {
        HashSet<String> vars = new HashSet<String>(4);
        vars.addAll(ValueUtils.parseUnresolvedVariableNames(this.operand1));
        vars.addAll(ValueUtils.parseUnresolvedVariableNames(this.operand2));
        return vars;
    }

    private class Version implements Comparable<Version> {

        private String version;

        /**
         * Get the version as string
         *
         * @return the version string
         */
        public final String get() {
            return this.version;
        }

        public Version(String version) {
            if(version == null)
                throw new IllegalArgumentException("Version can not be null");
            if(!version.matches("[^\\d]*[\\d]+([^\\d]+[\\d]+)*[^\\d]*"))
                throw new IllegalArgumentException("Invalid version format: '" + version + "'");
            this.version = version;
        }

        @Override
        public int compareTo(Version version) {
            if(version == null)
                return 1;
            String[] parts1 = this.get().split(VERSION_DELIMITER);
            List<String> leftOps =  new ArrayList<String>(Arrays.asList(parts1));
            leftOps.removeAll(EMPTY_STRINGS); // avoid NumberFormatException
            String[] parts2 = version.get().split(VERSION_DELIMITER);
            List<String> rightOps = new ArrayList<String>(Arrays.asList(parts2));
            rightOps.removeAll(EMPTY_STRINGS); // avoid NumberFormatException
            int length = hasFlag(NOT_ASSUME_MISSING_MINOR_PARTS_AS_0)
                    ? Math.min(leftOps.size(), rightOps.size())
                    : Math.max(leftOps.size(), rightOps.size());
            logger.finer("Effective number of version parts: " + length);
            for(int i = 0; i < length; i++) {
                int part1 = i < leftOps.size() ? Integer.parseInt(leftOps.get(i)) : 0;
                int part2 = i < rightOps.size() ? Integer.parseInt(rightOps.get(i)) : 0;
                logger.finer("Compare version parts: " + part1 + " <-> " + part2);
                if(part1 < part2)
                    return -1;
                if(part1 > part2)
                    return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object version) {
            if(this == version)
                return true;
            if(version == null)
                return false;
            if(this.getClass() != version.getClass())
                return false;
            return this.compareTo((Version) version) == 0;
        }

    }
}