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

import com.izforge.izpack.Info;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * The Hello panel class.
 *
 * @author Julien Ponge
 */
public class HelloPanel extends IzPanel 
{

    /**
     *
     */
    private static final long serialVersionUID = 3257848774955905587L;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation data.
     */
    public HelloPanel(InstallerFrame parent, InstallData idata)
    {
        this(parent, idata, new IzPanelLayout());
    }

    /**
     * Creates a new HelloPanel object with the given layout manager. Valid layout manager are the
     * IzPanelLayout and the GridBagLayout. New panels should be use the IzPanelLaout. If lm is
     * null, no layout manager will be created or initialized.
     *
     * @param parent The parent IzPack installer frame.
     * @param idata  The installer internal data.
     * @param layout layout manager to be used with this IzPanel
     */

    public HelloPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout)
    {
        // Layout handling. This panel was changed from a mixed layout handling
        // with GridBagLayout and BoxLayout to IzPanelLayout. It can be used as an
        // example how to use the IzPanelLayout. For this there are some comments
        // which are excrescent for a "normal" panel.
        // Set a IzPanelLayout as layout for this panel.
        // This have to be the first line during layout if IzPanelLayout will be used.
        super(parent, idata, layout);
        // We create and put the labels
        String str;
        str = parent.langpack.getString("HelloPanel.welcome1") + idata.info.getAppName() + " "
                + idata.info.getAppVersion() + parent.langpack.getString("HelloPanel.welcome2");
        JLabel welcomeLabel = LabelFactory.create(str, parent.icons.getImageIcon("host"), LEADING);
        // IzPanelLayout is a constraint orientated layout manager. But if no constraint is
        // given, a default will be used. It starts in the first line.
        // NEXT_LINE have to insert also in the first line!!
        add(welcomeLabel, NEXT_LINE);
        // Yes, there exist also a strut for the IzPanelLayout.
        // But the strut will be only used for one cell. A vertical strut will be use
        // NEXT_ROW, a horizontal NEXT_COLUMN. For more information see the java doc.
        // add(IzPanelLayout.createVerticalStrut(20));
        // But for a strut you have to define a fixed height. Alternative it is possible
        // to create a paragraph gap which is configurable.
        add(IzPanelLayout.createParagraphGap());

        ArrayList<Info.Author> authors = idata.info.getAuthors();
        int size = authors.size();
        if (size > 0)
        {
            str = parent.langpack.getString("HelloPanel.authors");
            JLabel appAuthorsLabel = LabelFactory.create(str, parent.icons
                    .getImageIcon("information"), LEADING);
            // If nothing will be sad to the IzPanelLayout the position of an add will be
            // determined in the default constraint. For labels it is CURRENT_ROW, NEXT_COLUMN.
            // But at this point we would place the label in the next row. It is possible
            // to create an IzPanelConstraint with this options, but it is also possible to
            // use simple the NEXT_LINE object as constraint. Attention!! Do not use
            // LayoutConstants.NEXT_ROW else LayoutConstants.NEXT_LINE because NEXT_ROW is an
            // int and with it an other add method will be used without any warning (there the
            // parameter will be used as position of the component in the panel, not the
            // layout manager.
            add(appAuthorsLabel, LayoutConstants.NEXT_LINE);

            JLabel label;
            for (int i = 0; i < size; i++)
            {
                Info.Author a = authors.get(i);
                String email = (a.getEmail() != null && a.getEmail().length() > 0) ? (" <"
                        + a.getEmail() + ">") : "";
                label = LabelFactory.create(" - " + a.getName() + email, parent.icons
                        .getImageIcon("empty"), LEADING);
                add(label, NEXT_LINE);
            }
            add(IzPanelLayout.createParagraphGap());
        }

        if (idata.info.getAppURL() != null)
        {
            str = parent.langpack.getString("HelloPanel.url") + idata.info.getAppURL();
            JLabel appURLLabel = LabelFactory.create(str, parent.icons.getImageIcon("bookmark"),
                    LEADING);
            add(appURLLabel, LayoutConstants.NEXT_LINE);
        }
        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
    
    public void panelActivate()
    {
        if (idata.info.needAdxAdmin())
        {
            try
            {
                // vérifier la présence d'un adxadmin
                RegistryHandler rh = RegistryDefaultHandler.getInstance();
                if (rh != null)
                {
    
                    rh.verify(idata);
    
                    // test adxadmin déjà installé avec registry
                    if (!rh.adxadminProductRegistered())
                    {
                        // pas d'adxadmin
                        JOptionPane.showMessageDialog(null, parent.langpack.getString( "adxadminNotRegistered"), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                        parent.lockNextButton();
                    }
                }
                else
                {
    
                    // else we are on a os which has no registry or the
                    // needed dll was not bound to this installation. In
                    // both cases we forget the "already exist" check.
                    
                    // test adxadmin sous unix avec /adonix/adxadm ?
                        if (OsVersion.IS_UNIX)
                        {
                            java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                            if (!adxadmFile.exists())
                            {
                                adxadmFile = new java.io.File ("/sage/adxadm");
                                if (!adxadmFile.exists())
                                {
                                    // pas d'adxadmin
                                    JOptionPane.showMessageDialog(null, parent.langpack.getString( "adxadminNotRegistered"), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                                    parent.lockNextButton();
                                }
                            }
                        }
                }
            }
            catch (Exception e)
            { // Will only be happen if registry handler is good, but an
                // exception at performing was thrown. This is an error...
                Debug.log(e);
                JOptionPane.showMessageDialog(null, e.getMessage(), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                parent.lockNextButton();
            }
        }
        else if (idata.info.isAdxAdmin())
        {
            // unix case
            // search for /sage/adxadm
            
            File adxadmFile = new File ("/sage/adxadm");
            if (adxadmFile.exists())
            {
                // il semble que ce soit une mise a jour
                // on positionne update mode
                // puis on charge .installinformation
                
                try
                {
                    String adxadmPath = readFile (OsVersion.IS_UNIX?"/sage/adxadm":"c:\\sage\\adxadm", Charset.defaultCharset());
                    adxadmPath = adxadmPath.replace("\r\n", "").replace("\n", "").trim();
                    String installInformation = adxadmPath+"/"+AutomatedInstallData.INSTALLATION_INFORMATION;
                    File installInformationFile = new File (installInformation);
                    if (!installInformationFile.exists())
                    {
                        JOptionPane.showMessageDialog(null, parent.langpack.getString( "adxadminNoAdxDir"), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                        parent.lockNextButton();
                    }
                    else
                    {
                        // relecture installInformation
                        idata.setInstallPath(adxadmPath);
                        // positionnement update
                        Debug.trace("modification installation");
                        idata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
                        
                    }
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(null, parent.langpack.getString( "adxadminNoAdxDir"), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                    parent.lockNextButton();
                }
                
            }
            
        }
    }    
    
    static String readFile(String path, Charset encoding) 
            throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
}
