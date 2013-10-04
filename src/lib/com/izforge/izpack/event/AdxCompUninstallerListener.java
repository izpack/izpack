package com.izforge.izpack.event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.xml.XMLHelper;


public class AdxCompUninstallerListener extends SimpleUninstallerListener
{
    private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";

    public AdxCompUninstallerListener()
    {
        super();
        
    }

    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Load the defined adx module to be deleted.
        InputStream in = getClass().getResourceAsStream("/"+SPEC_FILE_NAME);
        if (in == null)
        { // No actions, nothing todo.
            return;
        }
        
        // récupérer le fichier adxinstalls
        
        // here we need to update adxinstalls.xml
        
        // we need to find adxadmin path
        String strAdxAdminPath = "";
        
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh != null)
        {
            //rh.verify(idata);

            // test adxadmin déjà installé avec registry
            if (rh.adxadminProductRegistered())
            {

                String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
                int oldVal = rh.getRoot();
                rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                if (!rh.valueExist(keyName, "ADXDIR")) keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
                if (!rh.valueExist(keyName, "ADXDIR")) return;
                
                // récup path
                strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

                // free RegistryHandler
                rh.setRoot(oldVal);
            }
            else return;
        }
        else
        {
            Debug.log("CheckedHelloPanel - Could not get RegistryHandler !");

            // else we are on a os which has no registry or the
            // needed dll was not bound to this installation. In
            // both cases we forget the "already exist" check.
            
            // test adxadmin sous unix avec /adonix/adxadm ?
                if (OsVersion.IS_UNIX)
                {
                    java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                    if (!adxadmFile.exists())
                    {
                        adxadmFile = new java.io.File ("/adonix/adxadm");
                        if (!adxadmFile.exists())
                        {
                            return;
                        }
                    }
                    
                    FileReader readerAdxAdmFile = new FileReader(adxadmFile);
                    BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
                    strAdxAdminPath = buffread.readLine();
                }

        }
        
        
        // vérification strAdxAdminPath
        
        if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) return;
        
        java.io.File dirAdxDir = new java.io.File (strAdxAdminPath);
        
        if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) return;
        
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(dirAdxDir.getAbsolutePath());
        strBuilder.append(dirAdxDir.separator);
        strBuilder.append("inst");
        strBuilder.append(dirAdxDir.separator);
        strBuilder.append("adxinstalls.xml");
        
        java.io.File fileAdxinstalls = new java.io.File (strBuilder.toString());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document xdoc = null;
        Element xmodule = null;
        Document xdoc2 = null;
        Element xmodule2 = null;
        
        if (!fileAdxinstalls.exists())
        {
            return;
        }
        else
        {
            xdoc = dBuilder.parse(fileAdxinstalls);
        }
        
        XMLHelper.cleanEmptyTextNodes((Node)xdoc);
        
        xdoc2 = dBuilder.parse(in);
        xmodule2 = (Element) xdoc2.getDocumentElement().getElementsByTagName("module").item(0);
        
        NodeList listAdxInstallsNodes = xdoc.getDocumentElement().getElementsByTagName("module");
        for (int i = 0; i < listAdxInstallsNodes.getLength(); i++) {
            Element aNode = (Element) listAdxInstallsNodes.item(i);
            
            if ( aNode.getAttribute("name").equals(xmodule2.getAttribute("name"))
                    && aNode.getAttribute("type").equals(xmodule2.getAttribute("type"))
                            && aNode.getAttribute("family").equals(xmodule2.getAttribute("family")))
            {
                xmodule = aNode;
                break;
            }
                
            
        }  
        
        // module non trouvé :(
        if (xmodule==null) return;
        
        xmodule.getParentNode().removeChild(xmodule);
        
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(xdoc);
        StreamResult result = new StreamResult(fileAdxinstalls);
 
        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);
        transformer.transform(source, result);        
        
        return;
        
    }    
}
