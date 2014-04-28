package com.izforge.izpack.event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.xml.XMLHelper;


public class AdxCompInstallerListener extends SimpleInstallerListener implements CleanupClient
{

    private static final String SPEC_FILE_NAME = "AdxCompSpec.xml";
    
    public AdxCompInstallerListener()
    {
        super(true);
    }

    /**
     * Remove all registry entries on failed installation
     */
    public void cleanUp()
    {
        // installation was not successful now rewind aedxinstalls.xml changes
    }

    public void beforePacks(AutomatedInstallData idata, Integer npacks,
            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);
        getSpecHelper().readSpec(SPEC_FILE_NAME);
        
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null)
        {
            return;
        }
        rh.verify(idata);
        
    }

    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
    throws Exception
    {
        // here we need to update adxinstalls.xml
        
        // we need to find adxadmin path
        String strAdxAdminPath = "";
        
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh != null)
        {
            rh.verify(idata);

            // test adxadmin déjà installé avec registry
            if (rh.adxadminProductRegistered())
            {

                String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
                int oldVal = rh.getRoot();
                rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                if (!rh.valueExist(keyName, "ADXDIR")) keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
                if (!rh.valueExist(keyName, "ADXDIR")) throw new Exception(langpack.getString("adxadminNoAdxDir"));
                
                // récup path
                strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

                // free RegistryHandler
                rh.setRoot(oldVal);
            }
            else throw new Exception(langpack.getString("adxadminNotRegistered"));
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
                            throw new Exception(langpack.getString("adxadminNotRegistered"));
                        }
                    }
                    
                    FileReader readerAdxAdmFile = new FileReader(adxadmFile);
                    BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
                    strAdxAdminPath = buffread.readLine();
                }

        }
        
        
        // vérification strAdxAdminPath
        
        if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) throw new Exception(langpack.getString("adxadminParseError"));
        
        java.io.File dirAdxDir = new java.io.File (strAdxAdminPath);
        
        if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) throw new Exception(langpack.getString("adxadminParseError"));
        
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
        Document xmodule = null;
        
        if (!fileAdxinstalls.exists())
        {
            fileAdxinstalls.createNewFile();
            
            if (OsVersion.IS_UNIX)
            {
                Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
                //add owners permission
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                //add group permissions
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                //add others permissions
                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                
                Files.setPosixFilePermissions(fileAdxinstalls.toPath(), perms);
            }
            
            xdoc = dBuilder.newDocument();

             // Propriétés du DOM
            xdoc.setXmlVersion("1.0");
            xdoc.setXmlStandalone(true);
    
            // Création de l'arborescence du DOM
            Element racine = xdoc.createElement("install");
            xdoc.appendChild(racine);
         }
        else
        {
            xdoc = dBuilder.parse(fileAdxinstalls);
        }
        
        XMLHelper.cleanEmptyTextNodes ((Node)xdoc);
        
        // adxinstalls.xml lu ou crée
        // il faut ajouter le module
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");        
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        IXMLElement elemSpec = getSpecHelper().getSpec();
        IXMLElement moduleSpec = elemSpec.getFirstChildNamed("module");
        VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());

        String name = substitutor.substitute(moduleSpec.getAttribute("name"), VariableSubstitutor.PLAIN);
        String family = substitutor.substitute(moduleSpec.getAttribute("family"), VariableSubstitutor.PLAIN);
        String type = substitutor.substitute(moduleSpec.getAttribute("type"), VariableSubstitutor.PLAIN);
        String version = substitutor.substitute(moduleSpec.getFirstChildNamed("component."+family.toLowerCase()+".version").getContent(), VariableSubstitutor.PLAIN);
                
        Element module = null;
                
        boolean modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        if (modifyinstallation)
        {
            Element xmlinstall = xdoc.getDocumentElement();
            XPath xPath =  XPathFactory.newInstance().newXPath();
            Node status = (Node) xPath.compile("/install/module[@name='" + name + "' and @type='"+type+"' and @family='"+family+"']/component."+family.toLowerCase()+".installstatus").evaluate(xdoc, XPathConstants.NODE);
            module = (Element) status.getParentNode();
            if (status.getTextContent().equalsIgnoreCase("active"))
            {
                status.setTextContent("update");
            }

            Node nodeVersion = (Node) xPath.compile("/install/module[@name='" + name + "' and @type='"+type+"' and @family='"+family+"']/component."+family.toLowerCase()+".version").evaluate(xdoc, XPathConstants.NODE);
            nodeVersion.setTextContent(version);
            
            if (family.equalsIgnoreCase("RUNTIME"))
            {
                //SAM (Syracuse) 99562 New Bug 'Performance issue with Oracle Instant Client'
                // do not use instant client by default but only when n-tiers

                //runtime.odbc.dbhome
                Node nodedbhome = (Node) xPath.compile("/install/module[@name='" + name + "' and @type='"+type+"' and @family='"+family+"']/runtime.odbc.dbhome").evaluate(xdoc, XPathConstants.NODE);
                nodedbhome.setTextContent("");
                //runtime.odbc.forcedblink
                Node nodedblink = (Node) xPath.compile("/install/module[@name='" + name + "' and @type='"+type+"' and @family='"+family+"']/runtime.odbc.forcedblink").evaluate(xdoc, XPathConstants.NODE);
                nodedblink.setTextContent("False");
                
            }
            
        }
        else
        {
            
            // création du module
            module = xdoc.createElement("module");
            module.setAttribute("name", name); 
            module.setAttribute("family", family ); 
            module.setAttribute("type", type ); 
    
            for(IXMLElement param : moduleSpec.getChildren()){
                Element xmlParam = xdoc.createElement(param.getName());
                xmlParam.setTextContent(substitutor.substitute(param.getContent(), VariableSubstitutor.PLAIN));
                module.appendChild(xmlParam);
            }
            
            xdoc.getDocumentElement().appendChild(module);

        
        }
        
        // en principe c'est bon
        // le module est ajouté
        // réécriture du XML
        
        // write the content into xml file
        DOMSource source = new DOMSource(xdoc);
        StreamResult result = new StreamResult(fileAdxinstalls);
 
        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);
        transformer.transform(source, result);        
        

        // create ressource for uninstall
        Document xdoc2 = dBuilder.newDocument();
        
        // Propriétés du DOM
        xdoc2.setXmlVersion("1.0");
        xdoc2.setXmlStandalone(true);
        
        // Création de l'arborescence du DOM
        Element racine2 = xdoc2.createElement("install");
        xdoc2.appendChild(racine2);
        xdoc2.getDocumentElement().appendChild(xdoc2.importNode(module, true));

        idata.uninstallOutJar.putNextEntry(new ZipEntry(SPEC_FILE_NAME));

        DOMSource source2 = new DOMSource(xdoc2);
        StreamResult result2 = new StreamResult (idata.uninstallOutJar);

        transformer.transform(source2, result2);        
        idata.uninstallOutJar.closeEntry();
        
        
    }
    
}
