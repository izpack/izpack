/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.panels.sage;

import java.awt.LayoutManager2;

import javax.swing.JLabel;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * Panel which asks for the JDK path.
 *
 * @author Klaus Bartz
 */
public class JDKPathPanelJavaHome extends IzPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 4732940488753888334L;

	/**
	 * The constructor.
	 *
	 * @param parent
	 *            The parent.
	 * @param idata
	 *            The installation data.
	 */
	public JDKPathPanelJavaHome(InstallerFrame parent, InstallData idata) {
		this(parent, idata, new IzPanelLayout());
	}

	public JDKPathPanelJavaHome(InstallerFrame parent, InstallData idata, LayoutManager2 layout) {
		// Layout handling. This panel was changed from a mixed layout handling
		// with GridBagLayout and BoxLayout to IzPanelLayout. It can be used as
		// an
		// example how to use the IzPanelLayout. For this there are some
		// comments
		// which are excrescent for a "normal" panel.
		// Set a IzPanelLayout as layout for this panel.
		// This have to be the first line during layout if IzPanelLayout will be
		// used.
		super(parent, idata, layout); // TODO Auto-generated constructor stub

		// We create and put the labels
		final String str = idata.langpack.getString("JDKPathPanelJavaHome.javaHomeToSet");
		final JLabel welcomeLabel = LabelFactory.create(str, parent.icons.getImageIcon("OptionPane.errorIcon"),
				LEADING);
		// IzPanelLayout is a constraint orientated layout manager. But if no
		// constraint is
		// given, a default will be used. It starts in the first line.
		// NEXT_LINE have to insert also in the first line!!
		add(welcomeLabel, NEXT_LINE);
		add(IzPanelLayout.createParagraphGap());
		getLayoutHelper().completeLayout();

	}

	@Override
	public boolean isValidated() {
		boolean ret = super.isValidated();
		if (ret) {
			// check java_home is defined on the system
			ret = System.getenv().containsKey("JAVA_HOME");
			if (!ret) {
				emitError("", idata.langpack.getString("JDKPathPanelJavaHome.javaHomeNotSet"));

			}
		}

		return ret;

	}

	/**
	 * Called when the panel becomes active.
	 */
	@Override
	public void panelActivate() {
		// Resolve the default for chosenPath
		super.panelActivate();
		// display message to set the JAVA_HOME variable. if already set we skip
		// this panel
		final boolean ret = System.getenv().containsKey("JAVA_HOME");
		if (ret) {
			parent.skipPanel();
		} else {
			parent.lockNextButton();
		}

	}
}
