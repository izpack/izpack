/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.installer.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.ISummarisable;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.gui.MultiLineLabel;
import com.izforge.izpack.installer.data.GUIInstallData;

/**
 * Defines the base class for the IzPack panels. Any panel should be a subclass of it and should
 * belong to the <code>com.izforge.izpack.panels</code> package. Since IzPack version 3.9 the
 * layout handling will be delegated to the class LayoutHelper which can be accessed by
 * <code>getLayoutHelper</code>. There are some layout helper methods in this class which will be
 * exist some time longer, but they are deprecated. At a redesign or new panel use the layout
 * helper. There is a special layout manager for IzPanels. This layout manager will be supported by
 * the layout helper. There are some points which should be observed at layouting. One point e.g. is
 * the anchor. All IzPanels have to be able to use different anchors, as minimum CENTER and
 * NORTHWEST. To use a consistent appearance use this special layout manger and not others.
 *
 * @author Julien Ponge
 * @author Klaus Bartz
 */
public abstract class IzPanel extends JPanel implements AbstractUIHandler, LayoutConstants, ISummarisable
{
    private static final long serialVersionUID = 3256442495255786038L;


    /**
     * The helper object which handles layout
     */
    protected LayoutHelper layoutHelper;

    /**
     * The component which should get the focus at activation
     */
    protected Component initialFocus = null;

    /**
     * The installer internal data (actually a melting-pot class with all-public fields.
     */
    protected final GUIInstallData installData;

    /**
     * The parent IzPack installer frame.
     */
    protected final InstallerFrame parent;

    /**
     * internal headline Label
     */
    protected JLabel headLineLabel;

    /**
     * Is this panel general hidden or not
     */
    private boolean hidden;

    /**
     * HEADLINE = "headline"
     */
    public final static String HEADLINE = "headline";

    private DataValidator validationService = null;

    /**
     * DELIMITER = "." ( dot )
     */
    public final static String DELIMITER = ".";

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The panel meta-data.
     */
    private Panel metadata;


    private String helpUrl = null;

    /**
     * Constructs an <tt>IzPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param resources   the resources
     */
    public IzPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources)
    {
        this(panel, parent, installData, (LayoutManager2) null, resources);
    }

    /**
     * Constructs an <tt>IzPanel</tt> with the given layout manager.
     * <p/>
     * Valid layout manager are the  {@link IzPanelLayout} and <tt>GridBagLayout</tt>.
     * New panels should be use IzPanelLayout. If layoutManager is
     * null, no layout manager will be created or initialized.
     *
     * @param panel         the panel meta-data
     * @param parent        the parent IzPack installer frame
     * @param installData   the installation data
     * @param layoutManager layout manager to be used with this IzPanel
     * @param resources     the resources
     */
    public IzPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, LayoutManager2 layoutManager,
                   Resources resources)
    {
        super();
        this.metadata = panel;
        this.parent = parent;
        this.installData = installData;
        this.resources = resources;
        initLayoutHelper();
        if (layoutManager != null)
        {
            getLayoutHelper().startLayout(layoutManager);
        }
    }

    /**
     * Constructs an <tt>IzPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param iconName    the Headline icon name
     * @param instance    an instance counter
     * @param resources   the resources
     */
    @Deprecated
    // FIXME: This constructor is used just in ShortcutPanel, move it there
    public IzPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, String iconName,
                   Resources resources)
    {
        this(panel, parent, installData, resources);
        buildHeadline(iconName);
    }

    /**
     * Build the IzPanel internal Headline. If an external headline# is used, this method returns
     * immediately with false. Allows also to display a leading Icon for the PanelHeadline. This
     * Icon can also be different if the panel has more than one Instances. The UserInputPanel is
     * one of these Candidates. <p/> by marc.eppelmann&#064;gmx.de
     *
     * @param imageIconName  an Iconname
     * @param instanceNumber an panel instance
     * @return true if successful build
     */
    @Deprecated
    private void buildHeadline(String imageIconName)
    {
        if (parent.isHeading(this))
        {
            return;
        }

        String headline = null;
        String searchkey = getMetadata().getPanelId() + DELIMITER + "headline";
        if (getMetadata().hasPanelId() && installData.getMessages().getMessages().containsKey(searchkey))
        {
            headline = getString(getMetadata().getPanelId() + DELIMITER + "headline");
        }
        else {
            searchkey = getClass().getSimpleName() + DELIMITER + "headline";
            if (installData.getMessages().getMessages().containsKey(searchkey))
            {
                headline = getString(searchkey);
            }
        }
        if (headline != null)
        {
            if ((imageIconName != null) && !"".equals(imageIconName))
            {
                headLineLabel = new JLabel(headline, getImageIcon(imageIconName), SwingConstants.LEADING);
            }
            else
            {
                headLineLabel = new JLabel(headline);
            }

            Font font = headLineLabel.getFont();
            float size = font.getSize();
            int style = 0;
            font = font.deriveFont(style, (size * 1.5f));
            headLineLabel.setFont(font);

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 0);
            headLineLabel.setName(HEADLINE);
            ((GridBagLayout) getLayout()).addLayoutComponent(headLineLabel, gbc);

            add(headLineLabel);
        }
    }

    /**
     * Helper to return a language resource string.
     *
     * @param key the search key
     * @return the corresponding string, or {@code key} if the string is not found
     */
    public String getString(String key)
    {
        return installData.getMessages().get(key);
    }

    /**
     * Gets a named image icon
     *
     * @param iconName a valid image icon
     * @return the icon
     */
    public ImageIcon getImageIcon(String iconName)
    {
        return parent.getIcons().get(iconName);
    }

    /**
     * Inits and sets the internal layout helper object.
     */
    protected void initLayoutHelper()
    {
        layoutHelper = new LayoutHelper(this, installData);
    }

    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated. Default behaviour is
     * to return <code>true</code>.
     *
     * @return A boolean stating whether the panel has been validated or not.
     */
    protected boolean isValidated()
    {
        return true;
    }

    protected void saveData()
    {
        //Save Data
    }

    public boolean panelValidated()
    {
        return isValidated();
    }

    /**
     * This method is called when the panel becomes active. Default is to do nothing : feel free to
     * implement what you need in your subclasses. A panel becomes active when the user reaches it
     * during the installation process.
     */
    public void panelActivate()
    {
    }

    /**
     * This method is called when the panel gets desactivated, when the user switches to the next
     * panel. By default it doesn't do anything.
     */
    public void panelDeactivate()
    {
    }

    public void createInstallationRecord(IXMLElement rootElement)
    {
        // Default method, override to record panel contents
    };

    /**
     * Ask the user a question.
     *
     * @param title    Message title.
     * @param question The question.
     * @param choices  The set of choices to present.
     * @return The user's choice.
     * @see AbstractUIHandler#askQuestion(String, String, int)
     */
    @Override
    public int askQuestion(String title, String question, int choices)
    {
        return askQuestion(title, question, choices, -1);
    }

    /**
     * Ask the user a question.
     *
     * @param title         Message title.
     * @param question      The question.
     * @param choices       The set of choices to present.
     * @param defaultChoice The default choice. (-1 = no default choice)
     * @return The user's choice.
     * @see AbstractUIHandler#askQuestion(String, String, int, int)
     */
    @Override
    public int askQuestion(final String title, final String question, int choices, int defaultChoice)
    {
        return new PromptUIHandler(new GUIPrompt(this)).askQuestion(title, question, choices, defaultChoice);
    }

    @Override
    public int askWarningQuestion(final String title, final String question, int choices, int defaultChoice)
    {
        return new PromptUIHandler(new GUIPrompt(this)).askWarningQuestion(title, question, choices, defaultChoice);
    }

    public boolean emitNotificationFeedback(final String message)
    {
        return emitWarning(getString("installer.Message"), message);
    }

    /**
     * Notify the user about something.
     *
     * @param message The notification.
     */
    @Override
    public void emitNotification(final String message)
    {
        new PromptUIHandler(new GUIPrompt(this)).emitNotification(message);
    }

    /**
     * Warn the user about something.
     *
     * @param message The warning message.
     */
    @Override
    public boolean emitWarning(final String title, final String message)
    {
        return new PromptUIHandler(new GUIPrompt(this)).emitWarning(title, message);
    }

    /**
     * Notify the user of some error.
     *
     * @param message The error message.
     */
    @Override
    public void emitError(final String title, final String message)
    {
        new PromptUIHandler(new GUIPrompt(this)).emitError(title, message);
    }

    /**
     * Notify the user of some error and block the next button.
     *
     * @param message The error message.
     */
    @Override
    public void emitErrorAndBlockNext(String title, String message)
    {
        parent.lockNextButton();
        emitError(title, message);
    }

    /**
     * Returns the component which should be get the focus at activation of this panel.
     *
     * @return the component which should be get the focus at activation of this panel
     */
    public Component getInitialFocus()
    {
        return initialFocus;
    }

    /**
     * Sets the component which should be get the focus at activation of this panel.
     *
     * @param component which should be get the focus at activation of this panel
     */
    public void setInitialFocus(Component component)
    {
        initialFocus = component;
    }

    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method. If
     * <tt>RuntimeClassName.subkey</tt> is not found, the super class name will be used until it
     * is <tt>IzPanel</tt>. If no key will be found, null returns.
     *
     * @param subkey the subkey for the string which should be returned
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey)
    {
        return getI18nStringForClass(subkey, null);
    }

    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method. If no key will be
     * found the key or - if alternate class is null - null returns.
     *
     * @param subkey         the subkey for the string which should be returned
     * @param alternateClass the short name of the class which should be used if no string is
     *                       present with the runtime class name
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey, String alternateClass)
    {
        String retval = null;

        List<String> prefixes = new ArrayList<String>();
        String panelId = getMetadata().getPanelId();
        Class<?> clazz = this.getClass();

        String fullClassname = alternateClass==null?clazz.getName():alternateClass;
        String simpleClassname = alternateClass==null?clazz.getSimpleName():alternateClass;

        do
        {
            prefixes.add(fullClassname + "." + panelId);
            prefixes.add(simpleClassname + "." + panelId);
            prefixes.add(fullClassname);
            prefixes.add(simpleClassname);

            clazz = clazz.getSuperclass();
            fullClassname = clazz.getName();
            simpleClassname = clazz.getSimpleName();
        } while (alternateClass == null && !clazz.equals(IzPanel.class));
        prefixes.add(2, panelId);

        for (String prefix : prefixes)
        {
            String searchkey = prefix + "." + subkey;
            if (installData.getMessages().getMessages().containsKey(searchkey))
            {
                retval = getString(searchkey);
            }
            if (retval != null)
            {
                break;
            }
        }

        if (retval != null && retval.indexOf('$') > -1)
        {
            retval = installData.getVariables().replace(retval);
        }
        return (retval);
    }

    /**
     * Returns the parent of this IzPanel (which is a InstallerFrame).
     *
     * @return the parent of this IzPanel
     */
    public InstallerFrame getInstallerFrame()
    {
        return (parent);
    }

    // ------------- Helper for common used components ----- START ---

    /**
     * Creates a label via LabelFactory using iconId, pos and method getI18nStringForClass for
     * resolving the text to be used. If the icon id is null, the label will be created also.
     *
     * @param subkey         the subkey which should be used for resolving the text
     * @param alternateClass the short name of the class which should be used if no string is
     *                       present with the runtime class name
     * @param iconId         id string for the icon
     * @param pos            horizontal alignment
     * @return the newly created label
     */
    public JLabel createLabel(String subkey, String alternateClass, String iconId, int pos)
    {
        ImageIcon imageIcon = (iconId != null) ? parent.getIcons().get(iconId) : null;
        String msg = getI18nStringForClass(subkey, alternateClass);
        JLabel label = LabelFactory.create(msg, imageIcon, pos);
        if (label != null)
        {
            label.setFont(getControlTextFont());
        }
        return (label);

    }

    /**
     * Creates a label via LabelFactory using iconId, pos and method getI18nStringForClass for
     * resolving the text to be used. If the icon id is null, the label will be created also. If
     * isFullLine true a LabelFactory.FullLineLabel will be created instead of a JLabel. The
     * difference between both classes are a different layout handling.
     *
     * @param subkey         the subkey which should be used for resolving the text
     * @param alternateClass the short name of the class which should be used if no string is
     *                       present with the runtime class name
     * @param iconId         id string for the icon
     * @param pos            horizontal alignment
     * @param isFullLine     determines whether a FullLineLabel or a JLabel should be created
     * @return the newly created label
     */
    public JLabel createLabel(String subkey, String alternateClass, String iconId, int pos,
                              boolean isFullLine)
    {
        ImageIcon imageIcon = (iconId != null) ? parent.getIcons().get(iconId) : null;
        String msg = getI18nStringForClass(subkey, alternateClass);
        JLabel label = LabelFactory.create(msg, imageIcon, pos, isFullLine);
        if (label != null)
        {
            label.setFont(getControlTextFont());
        }
        return (label);

    }

    /**
     * Creates a label via LabelFactory with the given ids and the given horizontal alignment. If
     * the icon id is null, the label will be created also. The strings are the ids for the text in
     * langpack and the icon in icons of the installer frame.
     *
     * @param textId id string for the text
     * @param iconId id string for the icon
     * @param pos    horizontal alignment
     * @return the newly created label
     */
    public JLabel createLabel(String textId, String iconId, int pos)
    {
        return (createLabel(textId, iconId, pos, false));
    }

    /**
     * Creates a label via LabelFactory with the given ids and the given horizontal alignment. If
     * the icon id is null, the label will be created also. The strings are the ids for the text in
     * langpack and the icon in icons of the installer frame. If isFullLine true a
     * LabelFactory.FullLineLabel will be created instead of a JLabel. The difference between both
     * classes are a different layout handling.
     *
     * @param textId     id string for the text
     * @param iconId     id string for the icon
     * @param pos        horizontal alignment
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return the newly created label
     */
    public JLabel createLabel(String textId, String iconId, int pos, boolean isFullLine)
    {
        ImageIcon imageIcon = (iconId != null) ? parent.getIcons().get(iconId) : null;
        JLabel label = LabelFactory.create(getString(textId), imageIcon, pos, isFullLine);
        if (label != null)
        {
            label.setFont(getControlTextFont());
        }
        return (label);

    }

    /**
     * Creates a multi line label with the language dependent text given by the text id. The strings
     * is the id for the text in langpack of the installer frame. The horizontal alignment will be
     * LEFT.
     *
     * @param textId id string for the text
     * @return the newly created multi line label
     */
    public MultiLineLabel createMultiLineLabelLang(String textId)
    {
        return createMultiLineLabel(getString(textId));
    }

    /**
     * Creates a multi line label with the given text. The horizontal alignment will be LEFT.
     *
     * @param text text to be used in the label
     * @return the newly created multi line label
     */
    public MultiLineLabel createMultiLineLabel(String text)
    {
        return createMultiLineLabel(text, null, SwingConstants.LEFT);
    }

    /**
     * Creates a label via LabelFactory with the given text, the given icon id and the given
     * horizontal alignment. If the icon id is null, the label will be created also. The strings are
     * the ids for the text in langpack and the icon in icons of the installer frame.
     *
     * @param text   text to be used in the label
     * @param iconId id string for the icon
     * @param pos    horizontal alignment
     * @return the created multi line label
     */
    public MultiLineLabel createMultiLineLabel(String text, String iconId, int pos)
    {
        MultiLineLabel multiLineLabel = new MultiLineLabel(text, 0, 0);
        multiLineLabel.setFont(getControlTextFont());
        return multiLineLabel;
    }

    /**
     * The Font of Labels in many cases
     */
    public Font getControlTextFont()
    {
        Font fontObj = (getLAF() != null) ?
                MetalLookAndFeel.getControlTextFont() : getFont();
        //if guiprefs 'labelFontSize' multiplier value
        // has been setup then apply it to the font:
        final float val;
        if ((val = LabelFactory.getLabelFontSize()) != 1.0f)
        {
            fontObj = fontObj.deriveFont(fontObj.getSize2D() * val);
        }
        return fontObj;
    }

    protected static MetalLookAndFeel getLAF()
    {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        if (lookAndFeel instanceof MetalLookAndFeel)
        {
            return ((MetalLookAndFeel) lookAndFeel);
        }
        return (null);
    }

    // ------------- Helper for common used components ----- END ---
    // ------------------- Layout stuff -------------------- START ---

    /**
     * Returns the default GridBagConstraints of this panel.
     *
     * @return the default GridBagConstraints of this panel
     * @deprecated use <code>getLayoutHelper().getDefaulConstraints</code> instead
     */
    @Deprecated
    public GridBagConstraints getDefaultGridBagConstraints()
    {
        return (GridBagConstraints) (layoutHelper.getDefaultConstraints());
    }

    /**
     * Sets the default GridBagConstraints of this panel to the given object.
     *
     * @param constraints which should be set as default for this object
     * @deprecated use <code>getLayoutHelper().setDefaultConstraints</code> instead
     */
    @Deprecated
    public void setDefaultGridBagConstraints(GridBagConstraints constraints)
    {
        layoutHelper.setDefaultConstraints(constraints);
    }

    /**
     * Resets the grid counters which are used at getNextXGridBagConstraints and
     * getNextYGridBagConstraints.
     *
     * @deprecated use <code>getLayoutHelper().resetGridCounter</code> instead
     */
    @Deprecated
    public void resetGridCounter()
    {
        layoutHelper.resetGridCounter();
    }

    /**
     * Returns a newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     *
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @return newly created GridBagConstraints with the given values and the values from the
     *         defaultGridBagConstraints for the other parameters
     * @deprecated use <code>getLayoutHelper().getNewConstraints</code> instead
     */
    @Deprecated
    public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy)
    {
        return (GridBagConstraints) (layoutHelper.getNewConstraints(gridx, gridy));
    }

    /**
     * Returns a newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     *
     * @param gridx      value to be used for the new constraint
     * @param gridy      value to be used for the new constraint
     * @param gridwidth  value to be used for the new constraint
     * @param gridheight value to be used for the new constraint
     * @return newly created GridBagConstraints with the given values and the values from the
     *         defaultGridBagConstraints for the other parameters
     * @deprecated use <code>getLayoutHelper().getNewConstraints</code> instead
     */
    @Deprecated
    public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy, int gridwidth,
                                                       int gridheight)
    {
        return (GridBagConstraints) (layoutHelper.getNewConstraints(gridx, gridy, gridwidth,
                                                                    gridheight));
    }

    /**
     * Returns a newly created GridBagConstraints for the next column of the current layout row.
     *
     * @return a newly created GridBagConstraints for the next column of the current layout row
     * @deprecated use <code>getLayoutHelper().getNextXConstraints</code> instead
     */
    @Deprecated
    public GridBagConstraints getNextXGridBagConstraints()
    {
        return (GridBagConstraints) (layoutHelper.getNextXConstraints());
    }

    /**
     * Returns a newly created GridBagConstraints with column 0 for the next row.
     *
     * @return a newly created GridBagConstraints with column 0 for the next row
     * @deprecated use <code>getLayoutHelper().getNextYConstraints</code> instead
     */
    @Deprecated
    public GridBagConstraints getNextYGridBagConstraints()
    {
        return (GridBagConstraints) (layoutHelper.getNextYConstraints());
    }

    /**
     * Returns a newly created GridBagConstraints with column 0 for the next row using the given
     * parameters.
     *
     * @param gridwidth  width for this constraint
     * @param gridheight height for this constraint
     * @return a newly created GridBagConstraints with column 0 for the next row using the given
     *         parameters
     * @deprecated use <code>getLayoutHelper().getNextYConstraints</code> instead
     */
    @Deprecated
    public GridBagConstraints getNextYGridBagConstraints(int gridwidth, int gridheight)
    {
        return (GridBagConstraints) (layoutHelper.getNextYConstraints(gridwidth, gridheight));
    }

    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     *
     * @deprecated use <code>getLayoutHelper().startLayout</code> instead
     */
    @Deprecated
    public void startGridBagLayout()
    {
        layoutHelper.startLayout(new GridBagLayout());
    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     *
     * @deprecated use <code>getLayoutHelper().completeLayout</code> instead
     */
    @Deprecated
    public void completeGridBagLayout()
    {
        layoutHelper.completeLayout();
    }

    // ------------------- Layout stuff -------------------- END ---

    // ------------------- Summary stuff -------------------- START ---

    /**
     * This method will be called from the SummaryPanel to get the summary of this class which
     * should be placed in the SummaryPanel. The returned text should not contain a caption of this
     * item. The caption will be requested from the method getCaption. If <code>null</code>
     * returns, no summary for this panel will be generated. Default behaviour is to return
     * <code>null</code>.
     *
     * @return the summary for this class
     */
    @Override
    public String getSummaryBody()
    {
        return null;
    }

    /**
     * This method will be called from the SummaryPanel to get the caption for this class which
     * should be placed in the SummaryPanel. If <code>null</code> returns, no summary for this
     * panel will be generated. Default behaviour is to return the string given by langpack for the
     * key <code>&lt;current class name>.summaryCaption&gt;</code> if exist, else the string
     * &quot;summaryCaption.&lt;ClassName&gt;&quot;.
     *
     * @return the caption for this class
     */
    @Override
    public String getSummaryCaption()
    {
        String caption;
        if (parent.isHeading(this) && this.installData.guiPrefs.modifier.containsKey("useHeadingForSummary")
                && (this.installData.guiPrefs.modifier.get("useHeadingForSummary")).equalsIgnoreCase("yes"))
        {
            caption = getI18nStringForClass("headline", null);
        }
        else
        {
            caption = getI18nStringForClass("summaryCaption", null);
        }

        return (caption);
    }

    // ------------------- Summary stuff -------------------- END ---

    // ------------------- Inner classes ------------------- START ---

    public static class Filler extends JComponent
    {

        private static final long serialVersionUID = 3258416144414095153L;

    }

    // ------------------- Inner classes ------------------- END ---

    /**
     * Returns whether this panel will be hidden general or not. A hidden panel will be not counted
     * in the step counter and for panel icons.
     *
     * @return whether this panel will be hidden general or not
     */
    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * Set whether this panel should be hidden or not. A hidden panel will be not counted in the
     * step counter and for panel icons.
     *
     * @param hidden flag to be set
     */
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    /**
     * Returns the used layout helper. Can be used in a derived class to create custom layout.
     *
     * @return the used layout helper
     */
    public LayoutHelper getLayoutHelper()
    {
        return layoutHelper;
    }

    /**
     * Returns the panel metadata.
     *
     * @return the panel metadata
     */
    public Panel getMetadata()
    {
        return metadata;
    }

    @Deprecated
    public DataValidator getValidationService()
    {
        return validationService;
    }

    @Deprecated
    public void setValidationService(DataValidator validationService)
    {
        this.validationService = validationService;
    }

    /**
     * Parses the text for special variables.
     */
    protected String parseText(String string_to_parse)
    {
        string_to_parse = installData.getVariables().replace(string_to_parse);
        return string_to_parse;
    }

    /**
     * Indicates wether the panel can display help. The installer will hide Help button if current
     * panel does not support help functions. Default behaviour is to return <code>false</code>.
     *
     * @return A boolean stating wether the panel supports Help function.
     */
    public boolean canShowHelp()
    {
        return helpUrl != null;
    }

    /**
     * This method is called when Help button has been clicked. By default it doesn't do anything.
     */
    public void showHelp()
    {
        // System.out.println("Help function called, helpName: " + helpName);
        if (helpUrl != null)
        {
            URL resourceUrl = resources.getURL(helpUrl);
            getHelpWindow().showHelp(getString("installer.help"), resourceUrl);
        }
    }

    private HelpWindow helpWindow = null;

    private HelpWindow getHelpWindow()
    {
        if (this.helpWindow != null)
        {
            return this.helpWindow;
        }

        this.helpWindow = new HelpWindow(parent, getString("installer.help.close"));
        helpWindow.setName(GuiId.HELP_WINDOWS.id);
        return this.helpWindow;
    }

    public void setHelpUrl(String helpUrl)
    {
        this.helpUrl = helpUrl;
    }

    @Override
    public String toString()
    {
        return "IzPanel{" +
                "class=" + getClass().getSimpleName() +
                ", hidden=" + hidden +
                '}';
    }

    /**
     * Returns the resources.
     *
     * @return the resources
     */
    protected Resources getResources()
    {
        return resources;
    }
}
