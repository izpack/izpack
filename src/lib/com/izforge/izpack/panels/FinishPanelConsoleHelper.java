/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Jan Blok
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
package com.izforge.izpack.panels;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.VariableSubstitutor;
/**
 * Finish Panel console helper
 *
 * @author Mounir el hajj
 */
public class FinishPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {
	public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter) {
		return true;
	}
	
	public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p){
		return true;
	}

	public boolean runConsole(AutomatedInstallData idata) {
		if (idata.installSuccess) {
		    System.out.println();
			System.out.println(idata.langpack.getString("FinishPanel.success"));
			System.out.println(idata.langpack.getString("FinishPanel.installed.on") + " " + idata.getInstallPath());

			if (idata.uninstallOutJar != null)
            {
			    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());			    
                // We prepare a message for the uninstaller feature
                String path = idata.info.getUninstallerPath();
                path = vs.substitute(path, null);

                // Convert the file separator characters
                path = path.replace('/', File.separatorChar);
               
                System.out.println(idata.langpack
                        .getString("FinishPanel.uninst.info") + " " + path);
            }			
		} else {
            System.out.println();
			System.out.println(idata.langpack.getString("FinishPanel.fail"));
		}
        System.out.println();
		return true;
	}
}
