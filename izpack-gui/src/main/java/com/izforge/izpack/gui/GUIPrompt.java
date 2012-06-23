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

package com.izforge.izpack.gui;

import static com.izforge.izpack.api.handler.Prompt.Option.CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Option.NO;
import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Option.YES;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.izforge.izpack.api.handler.Prompt;


/**
 * Displays a dialog prompting users for a value or informs them of something.
 *
 * @author Tim Anderson
 */
public class GUIPrompt implements Prompt
{

    /**
     * The parent component. May be {@code null}.
     */
    private final JComponent parent;

    /**
     * Default JOptionPane OK button key.
     */
    private static final String OK_BUTTON = "OptionPane.okButtonText";

    /**
     * Default JOptionPane Cancel button key.
     */
    private static final String CANCEL_BUTTON = "OptionPane.cancelButtonText";

    /**
     * Default JOptionPane Yes button key.
     */
    private static final String YES_BUTTON = "OptionPane.yesButtonText";

    /**
     * Default JOptionPane No button key.
     */
    private static final String NO_BUTTON = "OptionPane.noButtonText";

    /**
     * Default constructor.
     */
    public GUIPrompt()
    {
        this(null);
    }

    /**
     * Constructs a {@code GUIPrompt} with a parent component.
     *
     * @param parent the parent component. This determines the {@code Frame} in which the dialog is displayed;
     *               if {@code null} or if the {@code parent} has no {@code Frame}, a default {@code Frame} is used
     */
    public GUIPrompt(JComponent parent)
    {
        this.parent = parent;
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    @Override
    public void message(Type type, String message)
    {
        message(type, null, message);
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param title   the message title. If {@code null}, the title will be determined from the type
     * @param message the message to display
     */
    @Override
    @SuppressWarnings("MagicConstant")
    public void message(Type type, String title, String message)
    {
        if (title == null)
        {
            title = getTitle(type);
        }
        JOptionPane.showMessageDialog(parent, message, title, getMessageType(type));
    }

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options)
    {
        return confirm(type, message, options, null);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select. May be {@code null}
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options, Option defaultOption)
    {
        return confirm(type, getTitle(type), message, options, defaultOption);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param title   the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String title, String message, Options options)
    {
        return confirm(type, title, message, options, null);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param title   the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    @SuppressWarnings("MagicConstant")
    public Option confirm(Type type, String title, String message, Options options, Option defaultOption)
    {
        int messageType = getMessageType(type);
        int optionType;
        switch (options)
        {
            case OK_CANCEL:
                optionType = JOptionPane.OK_CANCEL_OPTION;
                break;
            case YES_NO_CANCEL:
                optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                break;
            default:
                optionType = JOptionPane.YES_NO_OPTION;
                break;
        }
        if (title == null)
        {
            title = getTitle(type);
        }
        int selected;
        if (defaultOption == null)
        {
            selected = JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
        }
        else
        {
            // jump through some hoops to select the default option
            List<Object> opts = new ArrayList<Object>();
            Object initialValue = null;
            switch (optionType)
            {
                case JOptionPane.OK_CANCEL_OPTION:
                {
                    String ok = UIManager.getString(OK_BUTTON);
                    String cancel = UIManager.getString(CANCEL_BUTTON);
                    opts.add(ok);
                    opts.add(cancel);
                    initialValue = (defaultOption == OK) ? ok : (defaultOption == CANCEL) ? cancel : null;
                    break;
                }
                case JOptionPane.YES_NO_OPTION:
                {
                    String yes = UIManager.getString(YES_BUTTON);
                    String no = UIManager.getString(NO_BUTTON);
                    opts.add(yes);
                    opts.add(no);
                    initialValue = (defaultOption == YES) ? yes : (defaultOption == NO) ? no : null;
                    break;
                }
                case JOptionPane.YES_NO_CANCEL_OPTION:
                {
                    String yes = UIManager.getString(YES_BUTTON);
                    String no = UIManager.getString(NO_BUTTON);
                    opts.add(yes);
                    opts.add(no);
                    String cancel = UIManager.getString(CANCEL_BUTTON);
                    initialValue = (defaultOption == YES) ? yes : (defaultOption == NO) ? no
                            : (defaultOption == CANCEL) ? cancel : null;
                    break;
                }
            }
            selected = JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, null,
                                                    opts.toArray(), initialValue);
        }

        return getSelected(options, selected);
    }

    /**
     * Maps a {@code JOptionPane} selection to an {@link Option}.
     *
     * @param options  the options
     * @param selected the selected value
     * @return the corresponding {@link Option}
     */
    private Option getSelected(Options options, int selected)
    {
        Option result;
        switch (selected)
        {
            case JOptionPane.YES_OPTION:
                result = (options == Options.OK_CANCEL) ? OK : YES;
                break;
            case JOptionPane.NO_OPTION:
                result = NO;
                break;
            case JOptionPane.CANCEL_OPTION:
                result = CANCEL;
                break;
            default:
                result = (options == Options.YES_NO_CANCEL) ? CANCEL : NO;
        }
        return result;
    }

    /**
     * Returns the dialog title based on the type of the message.
     *
     * @param type the message type
     * @return the title
     */
    private String getTitle(Type type)
    {
        String result;
        switch (type)
        {
            case INFORMATION:
                result = "Info";
                break;
            case QUESTION:
                result = "Question";
                break;
            case WARNING:
                result = "Warning";
                break;
            default:
                result = "Error";
        }
        return result;
    }

    /**
     * Maps a {@link Type} to a JOptionPane message type.
     *
     * @param type the message type
     * @return the JOptionPane equivalent
     */
    private int getMessageType(Type type)
    {
        int result;
        switch (type)
        {
            case INFORMATION:
                result = JOptionPane.INFORMATION_MESSAGE;
                break;
            case WARNING:
                result = JOptionPane.WARNING_MESSAGE;
                break;
            default:
                result = JOptionPane.ERROR_MESSAGE;
                break;
        }
        return result;
    }
}
