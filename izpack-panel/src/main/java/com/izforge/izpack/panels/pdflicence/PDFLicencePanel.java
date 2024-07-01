/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.pdflicence;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.licence.AbstractLicencePanel;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.util.ViewerPropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Properties;
import java.util.ResourceBundle;

import static org.icepdf.ri.util.ViewerPropertiesManager.*;

/**
 * The IzPack PDF license panel.
 */
public class PDFLicencePanel extends AbstractLicencePanel implements ActionListener {

	private static final long serialVersionUID = 1907880984181722205L;

	/**
	 * The radio buttons.
	 */
	private final JRadioButton yesRadio;
	private final JRadioButton noRadio;

	/**
	 * Constructs an <tt>PDFLicencePanel</tt>.
	 *
	 * @param panel
	 *            the panel
	 * @param parent
	 *            the parent window
	 * @param installData
	 *            the installation data
	 * @param resources
	 *            the resources
	 * @param log
	 *            the log
	 */
	public PDFLicencePanel(Panel panel, final InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log)
	{
		super(panel, parent, installData, new IzPanelLayout(log), resources);

		// We put our components
		add(LabelFactory.create(getString(titleMessageKey), parent.getIcons().get("history"), LEADING), NEXT_LINE);

		final SwingController controller = new SwingController();
		final SwingViewBuilder builder = new SwingViewBuilder(controller, createProperties());
		final JPanel viewerComponentPanel = builder.buildViewerPanel();

		ComponentKeyBinding.install(controller, viewerComponentPanel);

		ActionListener fireDefault = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton defaultButton = parent.getRootPane().getDefaultButton();
				if (defaultButton != null && defaultButton.isEnabled()) {
					defaultButton.doClick();
				}
			}
		};
		viewerComponentPanel.registerKeyboardAction(fireDefault, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_FOCUSED);

		JPanel toolbar = new JPanel();
		toolbar.setLayout(new FlowLayout(FlowLayout.CENTER));
		toolbar.add(builder.buildFitToolBar());
		toolbar.add(builder.buildPageNavigationToolBar());
		toolbar.add(builder.buildZoomToolBar());

		add(toolbar, NEXT_LINE);

		add(controller.getDocumentViewController().getViewContainer(), NEXT_LINE);

		ButtonGroup group = new ButtonGroup();

		yesRadio = new JRadioButton(getString("LicencePanel.agree"), false);
		yesRadio.setName(GuiId.LICENCE_YES_RADIO.id);
		group.add(yesRadio);
		add(yesRadio, NEXT_LINE);
		yesRadio.addActionListener(this);

		noRadio = new JRadioButton(getString("LicencePanel.notagree"), true);
		noRadio.setName(GuiId.LICENCE_NO_RADIO.id);
		group.add(noRadio);
		add(noRadio, NEXT_LINE);
		noRadio.addActionListener(this);

		setInitialFocus(viewerComponentPanel);
		getLayoutHelper().completeLayout();

		// Open a PDF document to view
		controller.openDocument(loadLicence());

	}

	/**
	 * Actions-handling method (here it launches the installation).
	 *
	 * @param e
	 *            The event.
	 */
	public void actionPerformed(ActionEvent e) {
		if (yesRadio.isSelected()) {
			parent.unlockNextButton();
		} else {
			parent.lockNextButton();
		}
	}

	/**
	 * Indicates whether the panel has been validated or not.
	 *
	 * @return true if the user agrees with the license, false otherwise.
	 */
	public boolean isValidated() {
		if (noRadio.isSelected()) {
			parent.exit();
			return false;
		}
		return (yesRadio.isSelected());
	}

	/**
	 * Called when the panel becomes active.
	 */
	public void panelActivate() {
		if (!yesRadio.isSelected()) {
			parent.lockNextButton();
		}
	}

	private ViewerPropertiesManager createProperties() {

		final ViewerPropertiesManager propertiesManager = ViewerPropertiesManager.getInstance();

		// General
		propertiesManager.set(PROPERTY_DEFAULT_PAGEFIT, Integer.toString(DocumentViewController.PAGE_FIT_WINDOW_WIDTH));

		propertiesManager.set(PROPERTY_VIEWPREF_HIDEMENUBAR, "false");
		// Hides the menu bar. Default value false.
		propertiesManager.set(PROPERTY_VIEWPREF_HIDETOOLBAR, "false");
		// Hides the top toolbar. Default value false.
		propertiesManager.set(PROPERTY_VIEWPREF_FORM_HIGHLIGHT, "false");
		propertiesManager.set(PROPERTY_VIEWPREF_FITWINDOW, "true");
		// When enabled shows the document with fit to width if the document
		// does not already specify a default view. Default value, true.
		propertiesManager.set(PROPERTY_SHOW_KEYBOARD_SHORTCUTS, "false");
		// Enables/disables menu bar key events, default is true.
		propertiesManager.set("application.alwaysShowImageSplashWindow", "no");
		// Enables/disables the splash window, default is no
		propertiesManager.set("application.chromeOnStartup", "no");
		// Start with with chrome look and feel, default value is yes.
		propertiesManager.set("application.showLocalStorageDialogs", "no");
		// Yes is default value. Show dialog to use before writing properties to
		// disk.
		// "application.datadir - Directory to write properties file to in users
		// home, default .icesoft/icepdf-viewer.
		// Toolbar - General
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_PAGENAV, "false");
		// Show page navigation controls; first, previous, next and last.
		// Default is true.
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_FIT, "false");
		// Shows page fit controls; normal, height and width. Default is true.
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_ZOOM, "false");
		// Shows page zoom controls. Default is true.
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_ROTATE, "false");
		// Shows page rotation controls. Default is true.
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_TOOL, "false");
		// Hide all toolbars. Default is false.
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_ANNOTATION, "false");
		// Shows annotation creation controls. Default is true.
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_FORMS, "false");
		// Shows utility pane control to show/hide utility pane. Default is
		// true.
		// Toolbar - Zoom
		propertiesManager.set(PROPERTY_DEFAULT_ZOOM_LEVEL, "1.3");
		// Float value of default page zoom. Default if 1.0
		// Toolbar - Utility
		propertiesManager.set(PROPERTY_SHOW_TOOLBAR_UTILITY, "false");
		// show utility toolbar and children. Default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITY_SAVE, "false");
		// Show the save control, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITY_OPEN, "false");
		// Show the open control, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITY_SEARCH, "false");
		// Show the search control, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITY_UPANE, "false");
		// Show the utility pane control, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITY_PRINT, "false");
		// Show the print control, default is true.

		// Utility Pane
		propertiesManager.set(PROPERTY_HIDE_UTILITYPANE, "true");
		propertiesManager.set(PROPERTY_SHOW_UTILITYPANE_BOOKMARKS, "false");
		// Show the document outline panel tab, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITYPANE_SEARCH, "false");
		// Show the search panel tab, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITYPANE_ANNOTATION, "false");
		// Show the annotation properties pane, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITYPANE_THUMBNAILS, "false");
		// Show the thumbnail view pane, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITYPANE_LAYERS, "false");
		// Show the layers view pane, default is true.
		propertiesManager.set(PROPERTY_SHOW_UTILITYPANE_ANNOTATION_FLAGS, "false");
		// Status Bar
		propertiesManager.set(PROPERTY_SHOW_STATUSBAR, "false");
		// Show the status bar and child component, default is true.
		propertiesManager.set(PROPERTY_SHOW_STATUSBAR_STATUSLABEL, "false");
		// Show the status label, default is true.
		propertiesManager.set(PROPERTY_SHOW_STATUSBAR_VIEWMODE, "false");
		// Show the view mode controls, default is true.

		return propertiesManager;
	}

}
