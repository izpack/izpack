/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.panels.userinput.gui.rule;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.izforge.izpack.panels.userinput.field.rule.FieldSpec;

/*---------------------------------------------------------------------------*/

/**
 * One line synopsis. <BR>
 * <BR>
 * Enter detailed class description here.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 10/20/02
 * @see com.izforge.izpack.panels.userinput.UserInputPanel
 */
/*---------------------------------------------------------------------------*/
public class RuleTextField extends JTextField
{

    private final FieldSpec spec;

    public RuleTextField(FieldSpec spec)
    {
        super(spec.getColumns() + 1);
        this.spec = spec;

        setColumns(spec.getColumns());
        Rule rule = new Rule(spec);
        setDocument(rule);
    }

    protected Document createDefaultModel()
    {
        Rule rule = new Rule(spec);
        return (rule);
    }

    public int getColumns()
    {
        return spec.getColumns();
    }

    public int getEditLength()
    {
        return spec.getLength();
    }

    public boolean unlimitedEdit()
    {
        return spec.isUnlimitedLength();
    }

    public void setColumns(int columns)
    {
        super.setColumns(columns + 1);
    }

    // --------------------------------------------------------------------------
    //
    // --------------------------------------------------------------------------

    class Rule extends PlainDocument
    {

        /**
         *
         */
        private static final long serialVersionUID = 3258134643651063862L;

        private final FieldSpec spec;


        public Rule(FieldSpec spec)
        {
            this.spec = spec;
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
        {
            // --------------------------------------------------
            // don't process if we get a null reference
            // --------------------------------------------------
            if (str == null)
            {
                return;
            }

            // --------------------------------------------------
            // Compute the total length the string would become
            // if the insert request were be honored. If this
            // size is within the specified limits, apply further
            // rules, otherwise give an error signal and return.
            // --------------------------------------------------
            int totalSize = getLength() + str.length();

            if (spec.isUnlimitedLength() || totalSize <= spec.getLength())
            {
                if (spec.validate(str))
                {
                    super.insertString(offs, str, a);
                }
                else
                {
                    getToolkit().beep();
                }
            }
            else
            {
                getToolkit().beep();
            }
        }
    }
}
/*---------------------------------------------------------------------------*/
