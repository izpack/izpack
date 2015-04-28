package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.xml.XMLHelper;

public class X3NodeIdentifierValidator implements Validator
{

    private static final String X3FAMILY = "X3FAMILY";

    private static final String X3TYPE = "X3TYPE";

    public boolean validate(ProcessingClient client, AutomatedInstallData adata)
    {
        if (!client.hasParams()) return false;

        Map<String, String> params = client.getValidatorParams();

        String nodename = client.getText();
        String strX3Type = null;
        String strX3Family = null;

        if (!params.containsKey(X3FAMILY))
            return false;
        else
            strX3Family = params.get(X3FAMILY);

        if (params.containsKey(X3TYPE)) strX3Type = params.get(X3TYPE);

        // we need to find adxadmin path
        String strAdxAdminPath = "";

        try
        {

            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh != null)
            {
                rh.verify(adata);

                // test adxadmin déjà installé avec registry
                if (rh.adxadminProductRegistered())
                {

                    String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
                    int oldVal = rh.getRoot();
                    rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                    if (!rh.valueExist(keyName, "ADXDIR"))
                        keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
                    if (!rh.valueExist(keyName, "ADXDIR"))
                    {
                        rh.setRoot(oldVal);
                        return false;
                    }

                    // récup path
                    strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

                    // free RegistryHandler
                    rh.setRoot(oldVal);
                }
                else
                    return false;
            }
            else
            {
                // test adxadmin sous unix avec /adonix/adxadm ?
                if (OsVersion.IS_UNIX)
                {
                    java.io.File adxadmFile = new java.io.File("/sage/adxadm");
                    if (!adxadmFile.exists())
                    {
                        adxadmFile = new java.io.File("/adonix/adxadm");
                        if (!adxadmFile.exists()) { return false; }
                    }

                    FileReader readerAdxAdmFile = new FileReader(adxadmFile);
                    BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
                    strAdxAdminPath = buffread.readLine();
                }

            }

            // vérification strAdxAdminPath

            if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) return false;

            java.io.File dirAdxDir = new java.io.File(strAdxAdminPath);

            if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) return false;

            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(dirAdxDir.getAbsolutePath());
            strBuilder.append(dirAdxDir.separator);
            strBuilder.append("inst");

            java.io.File fileAdxinstalls = new java.io.File(strBuilder.toString());
            if (!fileAdxinstalls.exists() || !fileAdxinstalls.isDirectory()) { return false; }

            strBuilder.append(dirAdxDir.separator);
            strBuilder.append("adxinstalls.xml");

            fileAdxinstalls = new java.io.File(strBuilder.toString());
            if (!fileAdxinstalls.exists()) { return true; }

            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document xdoc = null;
            Document xmodule = null;

            xdoc = dBuilder.parse(fileAdxinstalls);

            Element xmlinstall = xdoc.getDocumentElement();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String strPath = "/install/module[@name='" + nodename + "' and @family='" + strX3Family
                    + "'";
            if (strX3Type != null && !"".equals(strX3Type))
                strPath += " and @type='" + strX3Type + "'";
            strPath += "]";

            Node module = (Node) xPath.compile(strPath).evaluate(xdoc, XPathConstants.NODE);
            
            if (module == null) return true;

        }
        catch (Exception ex)
        {
            Debug.log(ex);
        }

        return false;
    }

}
