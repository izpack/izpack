/*
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

package com.izforge.izpack.panels.install;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * The install panel class. Launches the actual installation job.
 *
 * @author Julien Ponge
 */
public class InstallPanel extends IzPanel implements ProgressListener
{

    private static final long serialVersionUID = 3257282547959410992L;

    /**
     * The tip label.
     */
    protected JLabel tipLabel;

    /**
     * The operation label .
     */
    protected JLabel packOpLabel;

    /**
     * The operation label .
     */
    protected JLabel overallOpLabel;

    /**
     * The icon used.
     */
    protected String iconName = "preferences";

    /**
     * The pack progress bar.
     */
    protected JProgressBar packProgressBar;

    /**
     * The progress bar.
     */
    protected JProgressBar overallProgressBar;

    /**
     * True if the installation has been done.
     */
    private volatile boolean validated = false;

    /**
     * How many packs we are going to install.
     */
    private int noOfPacks = 0;

    /**
     * The current step.
     */
    private int currentStep = 0;

    /**
     * Constructs an <tt>InstallPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public InstallPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        this.tipLabel = LabelFactory.create(getI18nStringForClass("tip"), parent.getIcons().get(iconName), LEADING);
        add(this.tipLabel, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
        packOpLabel = LabelFactory.create(" ", LEADING);
        add(packOpLabel, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));

        packProgressBar = new JProgressBar();
        packProgressBar.setStringPainted(true);
        packProgressBar.setString(getI18nStringForClass("begin"));
        packProgressBar.setValue(0);
        add(packProgressBar, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
        // make sure there is some space between the progress bars
        add(IzPanelLayout.createVerticalStrut(5));
        //add(IzPanelLayout.createParagraphGap());

        overallOpLabel = LabelFactory.create(getI18nStringForClass("progress"), parent.getIcons().get(iconName),
                                             LEADING);
        add(this.overallOpLabel, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));

        overallProgressBar = new JProgressBar();
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setString("");
        overallProgressBar.setValue(0);
        add(this.overallProgressBar, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
        getLayoutHelper().completeLayout();
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return The validation state.
     */
    public boolean isValidated()
    {
        return this.validated;
    }

    /**
     * The unpacker starts.
     */
    public void startAction(String name, int noOfJobs)
    {
        this.noOfPacks = noOfJobs;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                parent.blockGUI();

                // figure out how many packs there are to install
                overallProgressBar.setMinimum(0);
                overallProgressBar.setMaximum(noOfPacks);
                if (noOfPacks == 1)
                {
                    overallProgressBar.setIndeterminate(true);
                }
                overallProgressBar.setString("0 / " + Integer.toString(noOfPacks));
            }
        });
    }

    /**
     * The unpacker stops.
     */
    public void stopAction()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                parent.releaseGUI();
                parent.lockPrevButton();

                // With custom actions it is possible, that the current value
                // is not max - 1. Therefore we use always max for both
                // progress bars to signal finish state.
                overallProgressBar.setValue(overallProgressBar.getMaximum());
                int ppbMax = packProgressBar.getMaximum();
                if (ppbMax < 1)
                {
                    ppbMax = 1;
                    packProgressBar.setMaximum(ppbMax);
                }
                packProgressBar.setValue(ppbMax);

                if (installData.isInstallSuccess())
                {
                    packProgressBar.setString(getI18nStringForClass("finished"));
                }
                else
                {
                    packProgressBar.setString(getString("installer.error"));
                }
                packProgressBar.setEnabled(false);
                String no_of_packs = Integer.toString(noOfPacks);
                if (noOfPacks == 1)
                {
                    overallProgressBar.setIndeterminate(false);
                }
                overallProgressBar.setString(no_of_packs + " / " + no_of_packs);
                overallProgressBar.setEnabled(false);
                packOpLabel.setText(" ");
                packOpLabel.setEnabled(false);
                installData.setCanClose(true);
                validated = true;
                if (installData.isInstallSuccess() &&
                        installData.getPanels().indexOf(InstallPanel.this) != (installData.getPanels().size() - 1))
                {
                    parent.unlockNextButton();
                }
            }
        });
    }

    /**
     * Normal progress indicator.
     *
     * @param val The progression value.
     * @param msg The progression message.
     */
    public void progress(final int val, final String msg)
    {
        currentStep++;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                packProgressBar.setValue(val + 1);
                packOpLabel.setText(msg);
            }
        });
    }

    /**
     * Pack changing.
     *
     * @param packName The pack name.
     * @param stepno   The number of the pack.
     * @param max      The new maximum progress.
     */
    public void nextStep(final String packName, final int stepno, final int max)
    {
        currentStep = 0;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                packProgressBar.setValue(0);
                packProgressBar.setMinimum(0);
                packProgressBar.setMaximum(max);
                packProgressBar.setString(packName);
                overallProgressBar.setValue(stepno - 1);
                overallProgressBar.setString(Integer.toString(stepno) + " / "
                                                     + Integer.toString(noOfPacks));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void setSubStepNo(final int no_of_substeps)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                packProgressBar.setMaximum(no_of_substeps);
            }
        });
    }

    /**
     * Invoked when an action restarts.
     *
     * @param name           the name of the action
     * @param overallMessage a message describing the overall progress
     * @param tip            a tip describing the current progress
     * @param steps          the number of steps the action consists of
     */
    @Override
    public void restartAction(String name, String overallMessage, String tip, int steps)
    {
        overallOpLabel.setText(overallMessage);
        tipLabel.setText(tip);
        currentStep = 0;
        startAction(name, steps);
    }

    /**
     * Invoked to notify progress.
     * <p/>
     * This increments the current step.
     *
     * @param message a message describing the step
     */
    @Override
    public void progress(String message)
    {
        packOpLabel.setText(message);
        currentStep++;
        packProgressBar.setValue(currentStep);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // We clip the panel
        Dimension dimension = parent.getPanelsContainerSize();
        dimension.width -= (dimension.width / 4);
        dimension.height = 150;
        setMinimumSize(dimension);
        setMaximumSize(dimension);
        setPreferredSize(dimension);
        parent.lockNextButton();
        parent.lockPrevButton();

        parent.install(this);
    }

}
