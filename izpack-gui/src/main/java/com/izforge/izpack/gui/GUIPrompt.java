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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.izforge.izpack.api.handler.AbstractPrompt;


/**
 * Displays a dialog prompting users for a value or informs them of something.
 *
 * @author Tim Anderson
 */
public class GUIPrompt extends AbstractPrompt
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
     * @param title   the message title. If {@code null}, the title will be determined from the type
     * @param message the message to display
     */
    @Override
    public void message(Type type, String title, String message)
    {
        if (title == null)
        {
            title = getTitle(type);
        }
        showMessageDialog(getMessageType(type), title, message);
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
    public Option confirm(Type type, String title, final String message, Options options, Option defaultOption)
    {
        final int messageType = getMessageType(type);
        final int optionType;
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
            selected = showConfirmDialog(messageType, title, message, optionType);
        }
        else
        {
            // jump through some hoops to select the default option
            List<Object> opts = new ArrayList<Object>();
            Object initialValue;
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
                default:
                    initialValue = null;
                    break;
            }
            selected = showOptionDialog(messageType, title, message, optionType, opts, initialValue);
        }

        return getSelected(options, selected);
    }

    /**
     * Displays a message dialog, ensuring that it is displayed from the event dispatch thread.
     *
     * @param type    the dialog type
     * @param title   the title
     * @param message the message
     */
    @SuppressWarnings("MagicConstant")
    private void showMessageDialog(final int type, final String title, final String message)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            JOptionPane.showMessageDialog(parent, message, title, type);
        }
        else
        {
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        JOptionPane.showMessageDialog(parent, message, title, type);
                    }
                });
            }
            catch (Throwable exception)
            {
                throw new IllegalStateException(exception);
            }
        }
    }

    /**
     * Displays an option dialog, ensuring that it is displayed from the event dispatch thread.
     *
     * @param type         the dialog type
     * @param title        the title
     * @param message      the message
     * @param optionType   the option type
     * @param opts         the options
     * @param initialValue the initial value
     * @return the selected option
     */
    @SuppressWarnings("MagicConstant")
    private int showOptionDialog(final int type, final String title, final String message, final int optionType,
                                 final List<Object> opts, final Object initialValue)
    {
        int selected;
        if (SwingUtilities.isEventDispatchThread())
        {
            selected = JOptionPane.showOptionDialog(parent, message, title, optionType, type, null,
                                                    opts.toArray(), initialValue);
        }
        else
        {
            final int[] handle = new int[1];
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handle[0] = JOptionPane.showOptionDialog(parent, message, title, optionType, type,
                                                                 null, opts.toArray(), initialValue);
                    }
                });
            }
            catch (Throwable exception)
            {
                throw new IllegalStateException(exception);
            }
            selected = handle[0];
        }
        return selected;
    }

    /**
     * Displays an option dialog, ensuring that it is displayed from the event dispatch thread.
     *
     * @param type       the dialog type
     * @param title      the title
     * @param message    the message
     * @param optionType the option type
     * @return the selected option
     */
    @SuppressWarnings("MagicConstant")
    private int showConfirmDialog(final int type, final String title, final String message, final int optionType)
    {
        int selected;
        if (SwingUtilities.isEventDispatchThread())
        {
            selected = JOptionPane.showConfirmDialog(parent, message, title, optionType, type);
        }
        else
        {
            final int[] handle = new int[1];
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handle[0] = JOptionPane.showConfirmDialog(parent, message, title, optionType, type);
                    }
                });
            }
            catch (Throwable exception)
            {
                throw new IllegalStateException(exception);
            }
            selected = handle[0];
        }
        return selected;
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
