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

package com.izforge.izpack.panels.userinput.field;


import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * Determines if a field is selected for display.
 *
 * @author Tim Anderson
 */
public class FieldHelper
{

    /**
     * Determines if a field is required if the field is required for the current platform and selected packs.
     *
     * @param field       the field
     * @param installData the installation data
     * @param matcher     the platform-model matcher
     * @return {@code true} if the field is required for the current platform and selected packs
     */
    public static boolean isRequired(Field field, InstallData installData, PlatformModelMatcher matcher)
    {
        return isRequiredForPacks(field.getPacks(), installData.getSelectedPacks())
                && matcher.matchesCurrentPlatform(field.getOsModels());

    }

    /**
     * Determine if an item is required by any of the selected packs.
     * <p/>
     * An item is required if it has no pack constraints, or names one or more selected packs.
     *
     * @param packNames     the item's pack constraints. Each name corresponds to a pack for which the item should be
     *                      created if the pack is selected for installation
     * @param selectedPacks the selected packs
     * @return {@code true} if {@code packs} is empty, or one of the packs appears in {@code selectedPacks}
     */
    public static boolean isRequiredForPacks(List<String> packNames, List<Pack> selectedPacks)
    {
        boolean result = packNames.isEmpty();
        if (!result)
        {
            for (Pack selectedPack : selectedPacks)
            {
                String selected = selectedPack.getName();
                for (String pack : packNames)
                {
                    if (selected.equals(pack))
                    {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determine if an item is required by any of the unselected packs.
     * <p/>
     * An item is required if it has no pack constraints, or doesn't name any selected packs.
     *
     * @param packNames     the item's pack constraints. Each name corresponds to a pack for which the item should be
     *                      created if the pack is not selected for installation
     * @param selectedPacks the selected packs
     * @return {@code true} if {@code packs} is empty, or none of the packs appears in {@code selectedPacks}
     */
    public static boolean isRequiredForUnselectedPacks(List<String> packNames, List<Pack> selectedPacks)
    {
        boolean result = packNames.isEmpty();
        if (!result)
        {
            for (Pack selectedPack : selectedPacks)
            {
                String selected = selectedPack.getName();
                for (String pack : packNames)
                {
                    if (selected.equals(pack))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
