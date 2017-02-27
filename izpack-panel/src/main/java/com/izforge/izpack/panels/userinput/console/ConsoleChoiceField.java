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
package com.izforge.izpack.panels.userinput.console;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceField;
import com.izforge.izpack.util.Console;

/**
 * Console presentation of {@link ChoiceField}.
 *
 * @author Tim Anderson
 */
public abstract class ConsoleChoiceField<T extends Choice> extends ConsoleField
{

    /**
     * Constructs a {@link ConsoleChoiceField}.
     *
     * @param field the field
     * @param console the console
     * @param prompt the prompt
     */
    public ConsoleChoiceField(ChoiceField field, Console console, Prompt prompt)
    {
        super(field, console, prompt);
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public ChoiceField getField()
    {
        return (ChoiceField) super.getField();
    }

    /**
     * Displays the field.
     * <p/>
     * For fields that update variables, this collects input and validates it.
     *
     * @return {@code true} if the field was displayed and validated
     * successfully
     */
    @Override
    public boolean display()
    {
        ChoiceField field = getField();
        printDescription();
        List<Choice> choices = field.getChoices();
        final int selectedRealIndex = field.getSelectedIndex();
        MappedSelection visibleToRealMapping = listChoices(choices, selectedRealIndex);
        if (isReadonly())
        {
            field.setValue(choices.get(field.getSelectedIndex() == -1 ? 0 : field.getSelectedIndex()).getKey());
            return true;
        }
        else
        {
            int selectedVisibleIndex = getConsole().prompt(getMessage("ConsoleInstaller.inputSelection"), 0,
                    visibleToRealMapping.size() - 1, visibleToRealMapping.getDefaultVisibleIndex(), -1);
            if (selectedVisibleIndex == -1)
            {
                return false;
            }
            field.setValue(choices.get(visibleToRealMapping.getRealFromVisible(selectedVisibleIndex)).getKey());
            return true;
        }
    }

    /**
     * Displays the choices.
     *
     * @param choices the choices
     * @param selectedRealIndex the selected choice, or {@code -1} if no choice
     * is selected
     */
    private MappedSelection listChoices(List<Choice> choices, int selectedRealIndex)
    {
        int visibleIndex = 0;
        MappedSelection visibleToRealMapping = new MappedSelection();
        for (int i = 0; i < choices.size(); ++i)
        {
            Choice choice = choices.get(i);
            String conditionId = choice.getConditionId();
            if (conditionId == null || getField().getInstallData().getRules().isConditionTrue(conditionId))
            {
                boolean isSelected = (i == selectedRealIndex);
                println(visibleIndex + "  [" + (isSelected ? "x" : " ") + "] " + choice.getValue());
                visibleToRealMapping.put(Integer.valueOf(visibleIndex), Integer.valueOf(i));
                if (isSelected)
                {
                    // The default when the user hits just ENTER without entering an explicit value
                    visibleToRealMapping.setDefaultVisibleIndex(visibleIndex);
                }
                visibleIndex++;
            }
        }
        return visibleToRealMapping;
    }

    private static class MappedSelection
    {

        private Map<Integer, Integer> visibleToRealIndexes = new HashMap<Integer, Integer>();
        private int defaultVisibleIndex = -1;

        public Integer put(Integer visibleIndex, Integer realIndex)
        {
            return visibleToRealIndexes.put(visibleIndex, realIndex);
        }

        public int size()
        {
            return visibleToRealIndexes.size();
        }

        public Integer getRealFromVisible(int visibleIndex)
        {
            return visibleToRealIndexes.get(visibleIndex);
        }

        public void setDefaultVisibleIndex(int defaultVisibleIndex)
        {
            this.defaultVisibleIndex = defaultVisibleIndex;
        }

        public int getDefaultVisibleIndex()
        {
            return defaultVisibleIndex;
        }
    }
}
