package com.izforge.izpack.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;


public class InstallTypePanel extends IzPanel implements ActionListener, ListSelectionListener
{

    public static String ADX_NODE_TYPE = "component.node.type";  
    public static String ADX_NODE_FAMILY = "component.node.family";  
    private JRadioButton normalinstall;
    private JRadioButton modifyinstall;
    private DefaultListModel listItems;
    private JList installedComponents;
    private HashMap lstCompProps;
    private String selectedKey;
    
    public InstallTypePanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        buildGUI();
        
    }

    private void loadComponents()
    {
        if (idata.info.needAdxAdmin())
        {
            // Component is registered in adxadmin service
            // we can read pathes from adxinstalls.xml
            
            loadListFromAdxadmin ();
        }
        else
        {
            if (OsVersion.IS_WINDOWS)
            {
                // we can read from registry
                loadListFromRegistry ();
                
            }
        }
    }

    private void loadListFromRegistry()
    {
        try
        {
            // need to process prefix
            
            String uninstallName = idata.getVariable("UNINSTALL_NAME");
            String uninstallKeySuffix = idata.getVariable("UninstallKeySuffix");
            String uninstallKeyPrefix = new String (uninstallName);
            
            if (uninstallKeySuffix!=null && !"".equals(uninstallKeySuffix))
            {
                uninstallKeyPrefix = uninstallKeyPrefix.substring(0, uninstallKeyPrefix.length() -  uninstallKeySuffix.length());
            }
            
            // load registry
            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh == null)
            {
                // nothing to do
                return;
            }
                
            rh.verify(idata);

            String UninstallKeyName = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
            int oldVal = rh.getRoot();
            rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
            
            List<String> lstSubKeys = Arrays.asList( rh.getSubkeys(UninstallKeyName));
            

            for (String uninstallKey : lstSubKeys) 
            {
                if (uninstallKey.startsWith(uninstallKeyPrefix))
                {
                    // read path from uninstall string :((
                    String productPath =  rh.getValue(UninstallKeyName+"\\"+uninstallKey, "UninstallString").getStringData();
                    String productVersion = rh.getValue(UninstallKeyName+"\\"+uninstallKey, "DisplayVersion").getStringData();

                    productPath = productPath.substring(productPath.lastIndexOf("\"", productPath.length()-2)+1, productPath.length()-29);
                    String name = uninstallKey;
                    if (name.indexOf(" - ")>0)
                    {
                        name=name.substring(name.indexOf(" - ")+3);
                    }
                    
                    File installInformation = new File(productPath + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                    
                    if (installInformation.exists())
                    {
                        String key = name+" "+ productVersion +" ("+productPath+")";
                        listItems.addElement(key);
                        //listItems.addElement(new String[] {name+""+ productVersion +" ("+productPath+")", productPath, productVersion});
                        lstCompProps.put(key, new String[] {name, productPath, productVersion});
                    }
                    
                }
            }
            
            // free RegistryHandler
            rh.setRoot(oldVal);            
            
            
        }
        catch (Exception ex)
        {
            Debug.trace(ex); 
        }
        
    }

    private void loadListFromAdxadmin()
    {
        try
        {
            
            String strAdxAdminPath = "";
            
            if (OsVersion.IS_UNIX)
            {
                java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                if (!adxadmFile.exists())
                {
                    adxadmFile = new java.io.File ("/adonix/adxadm");
                }

                if (!adxadmFile.exists())
                {
                    // nothing to do
                    return;
                }
                
                FileReader readerAdxAdmFile = new FileReader(adxadmFile);
                BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
                strAdxAdminPath = buffread.readLine();

                
            }
            else
            {
                RegistryHandler rh = RegistryDefaultHandler.getInstance();
                if (rh == null)
                {
                    // nothing to do
                    return;
                }
                    
                rh.verify(idata);

                // test adxadmin déjà installé avec registry
                if (!rh.adxadminProductRegistered())
                {
                    // nothing to do
                    return;
                }

                String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
                int oldVal = rh.getRoot();
                rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                if (!rh.valueExist(keyName, "ADXDIR")) keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
                if (!rh.valueExist(keyName, "ADXDIR")) 
                {
                    // nothing to do
                    return;
                }
                
                // récup path
                strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

                // free RegistryHandler
                rh.setRoot(oldVal);
                
            }
    
            
            // check strAdxAdminPath
            
            if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) 
            {
                // nothing to do
                return;
            }
            
            java.io.File dirAdxDir = new java.io.File (strAdxAdminPath);
            
            if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) 
            {
                // nothing to do
                return;
            }
            
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(dirAdxDir.getAbsolutePath());
            strBuilder.append(dirAdxDir.separator);
            strBuilder.append("inst");
            strBuilder.append(dirAdxDir.separator);
            strBuilder.append("adxinstalls.xml");
            
            java.io.File fileAdxinstalls = new java.io.File (strBuilder.toString());
            
            if (!fileAdxinstalls.exists())
            {
                // nothing to do
                return;
            }
        
            
            // we need to know type and family
            String strComponentType = idata.getVariable(ADX_NODE_TYPE);
            String strComponentFamily = idata.getVariable(ADX_NODE_FAMILY);
            
            // do nothing if we don't know family
            if (strComponentFamily == null) return;
            
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fileAdxinstalls);

            XPath xPath =  XPathFactory.newInstance().newXPath();
            String expression = "//module[@family='"+strComponentFamily+"'";
            
            if (strComponentType!=null ) expression+=" and @type='"+strComponentType+"'";
            
            expression+="]";
            
            
            NodeList nodeLst = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            
            //NodeList nodeLst = doc.getElementsByTagName("module");
             
            for (int i = 0; i < nodeLst.getLength(); i++) 
            {
             
                Element moduleNode = (Element) nodeLst.item(i);
                String path = xPath.evaluate("./component."+strComponentFamily.toLowerCase()+".path", moduleNode);
                String strversion = xPath.evaluate("./component."+strComponentFamily.toLowerCase()+".version", moduleNode);
                String name = moduleNode.getAttribute("name");
                
                File installInformation = new File(path + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                
                if (installInformation.exists())
                {
                    String key = name + " " + strversion +" ("+path+")";
                    listItems.addElement(key);
                    lstCompProps.put(key, new String[] {name, path, strversion});
                    //listItems.addElement(new String[] {moduleNode.getAttribute("name")+" "+ strversion +" ("+path+")", path, strversion});
                    
                }
            }
        
        }
        catch (Exception ex)
        {
            Debug.trace(ex); 
        }
        
    }

    private void buildGUI()
    {
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout( new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));        
        
        // We put our components

//        add(LabelFactory.create(parent.langpack.getString("InstallationTypePanel.info"),
//                parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);

        topPanel.add(LabelFactory.create(parent.langpack.getString("InstallationTypePanel.info"),
              parent.icons.getImageIcon("history"), LEADING));
        
        topPanel.add(Box.createRigidArea(new Dimension(0,5)));
        
        ButtonGroup group = new ButtonGroup();

        boolean modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));

        normalinstall = new JRadioButton(parent.langpack.getString("InstallationTypePanel.normal"), !modifyinstallation);
        normalinstall.addActionListener(this);
        group.add(normalinstall);
        //add(normalinstall, NEXT_LINE);
        topPanel.add(normalinstall);

        modifyinstall = new JRadioButton(parent.langpack.getString("InstallationTypePanel.modify"), modifyinstallation);
        modifyinstall.addActionListener(this);
        group.add(modifyinstall);
        //add(modifyinstall, NEXT_LINE);
        topPanel.add(modifyinstall);
        
        lstCompProps = new HashMap();

        listItems = new DefaultListModel();
        installedComponents = new JList (listItems);
        installedComponents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        installedComponents.setLayoutOrientation(JList.VERTICAL);
        installedComponents.setVisibleRowCount(5);
        installedComponents.setEnabled(false);
        
        JScrollPane listScroller = new JScrollPane(installedComponents);
        listScroller.setPreferredSize(new Dimension (600, 100));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        topPanel.add(listScroller);
        
        add(topPanel, NEXT_LINE);

        setInitialFocus(normalinstall);
        getLayoutHelper().completeLayout();
    }

    /**
     *
     */

    /* (non-Javadoc)
    * @see com.izforge.izpack.installer.IzPanel#panelActivate()
    */
    public void panelActivate()
    {
        listItems.clear();
        lstCompProps.clear();
        loadComponents ();

        boolean modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        if (modifyinstallation)
        {
            modifyinstall.setSelected(true);
            installedComponents.setEnabled(true);
            
            if (selectedKey!=null)
            {
                if (listItems.contains(selectedKey))
                {
                    installedComponents.setSelectedValue(selectedKey, true);
                }
                else
                {
                    if (listItems.size()>0)
                    {
                        installedComponents.setSelectedIndex(0);
                    }
                }
            }
            else
            {
                if (listItems.size()>0)
                {
                    installedComponents.setSelectedIndex(0);
                }
            }
                
        }
        else
        {
            normalinstall.setSelected(true);
            installedComponents.setEnabled(false);
        }

    }

    public void actionPerformed(ActionEvent e)
    {
        Debug.trace("installation type changed");
        if (e.getSource() == normalinstall)
        {
            Debug.trace("normal installation");
            installedComponents.clearSelection();
            
            installedComponents.setEnabled(false);
            idata.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else if (e.getSource() == modifyinstall)
        {
            Debug.trace("modification installation");
            installedComponents.setEnabled(true);
            idata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
            
            if (selectedKey!=null)
            {
                if (listItems.contains(selectedKey))
                {
                    installedComponents.setSelectedValue(selectedKey, true);
                }
                else
                {
                    if (listItems.size()>0)
                    {
                        installedComponents.setSelectedIndex(0);
                    }
                }
            }
            else
            {
                if (listItems.size()>0)
                {
                    installedComponents.setSelectedIndex(0);
                }
            }
            
        }
        else if (e.getSource() == installedComponents)
        {
            
        }

    }
    
    public String getPathFromSelected ()
    {
        String compPath = null;
        
        if (installedComponents.getSelectedValue()!=null) 
        {
            String key = (String) installedComponents.getSelectedValue();
            String[] compProps = (String[]) lstCompProps.get(key);
            compPath=compProps[1];     
        }
        
        return compPath;

    }
    
    
    public boolean isValidated()
    {
        // we must ensure .installinformation is present if in modification mode
        // then set install_path
        
        Boolean modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        
        if (modifyinstallation)
        {
            
            
            String compPath = getPathFromSelected();
            if (compPath==null) return false;
            
            
            File installationinformation = new File(compPath + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
            if (!installationinformation.exists())
            {
                emitError(parent.langpack.getString("installer.error"), parent.langpack.getString("PathInputPanel.required.forModificationInstallation"));

                return false;
            }
            
            TargetPanel.reset();
            idata.setInstallPath(compPath);
            selectedKey = (String) installedComponents.getSelectedValue();
        }
            
            
        return super.isValidated();
    }    
    
    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        new InstallTypePanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */
    public String getSummaryBody()
    {

        if (Boolean.parseBoolean(idata.getVariable(InstallData.MODIFY_INSTALLATION)))
        {
            return parent.langpack.getString("InstallationTypePanel.modify");
        }
        else
        {
            return parent.langpack.getString("InstallationTypePanel.normal");
        }        
    }

    public void valueChanged(ListSelectionEvent e)
    {
        selectedKey = (String) installedComponents.getSelectedValue();
        
    }
    
    
}
