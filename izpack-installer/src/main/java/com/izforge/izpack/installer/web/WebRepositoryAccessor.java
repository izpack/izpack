/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
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

package com.izforge.izpack.installer.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.util.OsConstraintHelper;

/**
 * This class enumerates the availabe packs at the web repository. Parses the config files
 * - install.xml, packsinfo.xml, langpacks and is used to override the static configuration
 * in the installer jar.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
public class WebRepositoryAccessor
{
    /**
     * URL to remote install.xml
     */
    private String installXmlUrl;

    /**
     * Base repository URL
     */
    private String baseUrl;

    /**
     * install.xml
     */
    private String installXmlString;

    /**
     * packsinfo.xml contains size, pack name and pack id
     */
    private String packsInfo;

    /**
     * list of PackInfo entries
     */
    private ArrayList<PackInfo> packs;

    /**
     * Constant for checking attributes.
     */
    private static boolean YES = true;

    /**
     * Constant for checking attributes.
     */
    private static boolean NO = false;

    /**
     * Files to be looked for at the repository base url
     */
    private static final String installFilename = "install.xml";

    private static final String packsinfoFilename = "packsinfo.xml";

    /**
     * Files being downloaded in the buffer, 1MB max
     */
    private static final int BUFFER_SIZE = 1000000;


    /**
     * Create a new WebRepositoryAccessor.
     *
     * @param urlbase
     */
    public WebRepositoryAccessor(String urlbase)
    {
        this.installXmlUrl = urlbase + "/" + installFilename;
        this.baseUrl = urlbase;
    }

    /**
     * Get the list of the packs from the remore install.xml
     *
     * @return the packs list
     */
    public ArrayList<PackInfo> getOnlinePacks()
    {
        readConfig();
        packs = parsePacks();
        readPacksInfo();
        parsePacksInfo();
        return packs;
    }

    /**
     * Returns the contents of a file at url as a string (must be a text file)
     *
     * @param url
     * @return the content
     */
    private String stringFromURL(String url)
    {
        int max = BUFFER_SIZE;
        byte[] raw = new byte[max];
        InputStream in = null;
        try
        {
            WebAccessor webAccessor = new WebAccessor(null);
            in = webAccessor.openInputStream(new URL(url));
            if (in == null)
            {
                throw new RuntimeException("Unable to open network stream");
            }
            int r = in.read(raw);
            int off = r;
            while (r > 0)
            {
                r = in.read(raw, off, max - off);
                off += r;
            }
            return new String(raw);
        }
        catch (Exception e)
        {
            System.out.println(e + " while trying to download " + url);
            return null;
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * Reads the install.xml into confgiString
     */
    private void readConfig()
    {
        installXmlString = stringFromURL(installXmlUrl);
    }


    /**
     * Reads packsinfo.xml
     */
    private void readPacksInfo()
    {
        String url = this.baseUrl + "/" + packsinfoFilename;
        packsInfo = stringFromURL(url);
    }

    /**
     * Parse install.xml and return the list of packs
     *
     * @return the list of packs
     */
    private ArrayList<PackInfo> parsePacks()
    {
        try
        {
            IXMLParser parser = new XMLParser();

            IXMLElement xml = parser.parse(installXmlString);
            return loadPacksList(xml);
        }
        catch (Exception e)
        {
            System.out.println("WARN: Unable to parse install.xml");
            return null;
        }
    }

    /**
     * Parse packsinfo.xml, fill the size and fileSize field, which is not available at runtime
     * otherwise.
     */
    private void parsePacksInfo()
    {
        try
        {
            IXMLParser parser = new XMLParser();

            IXMLElement xml = parser.parse(packsInfo);
            IXMLElement root = xml; //requireChildNamed(xml, "packs");
            for (int i = 0; i < root.getChildrenCount(); i++)
            {
                IXMLElement packElement = root.getChildAtIndex(i);
                PackInfo packInfo = packs.get(i);
                Pack pack = packInfo.getPack();
                pack.setSize(Long.parseLong(packElement.getAttribute("size")));
                pack.setFileSize(Long.parseLong(packElement.getAttribute("fileSize")));
            }
        }
        catch (Exception e)
        {
            System.out.println("WARN: Unable to parse packsinfo.xml");
        }
    }

    /**
     * First download the jar file. The create the input stream from the
     * downloaded file. This is because the Jar connection's openInputStream
     * will blocks until the whole jar in order to unzip it (there is no way
     * to see the download progress there).
     *
     * @param url
     * @return the url
     */
    public static String getCachedUrl(String url, String tempFolder) throws IOException
    {
        int max = BUFFER_SIZE;
        byte[] raw = new byte[max];
        WebAccessor webAccessor = new WebAccessor(null);
        InputStream in = webAccessor.openInputStream(new URL(url));
        int r = in.read(raw);
        File tempDir = new File(tempFolder);

        tempDir.mkdirs();

        File temp = File.createTempFile("izpacktempfile", "jar", new File(tempFolder));
        FileOutputStream fos = new FileOutputStream(temp);
        String path = "file:///" + temp.getAbsolutePath();
        while (r > 0)
        {
            fos.write(raw, 0, r);
            r = in.read(raw);
        }
        in.close();
        fos.close();

        return path;
    }


    protected ArrayList<PackInfo> loadPacksList(IXMLElement data) throws CompilerException
    {
        ArrayList<PackInfo> result = new ArrayList<PackInfo>();

        // Initialisation
        IXMLElement root = requireChildNamed(data, "packs");

        // at least one pack is required
        List<IXMLElement> packElements = root.getChildrenNamed("pack");
        if (packElements.isEmpty())
        {
            parseError(root, "<packs> requires a <pack>");
        }

        for (IXMLElement packElement : packElements)
        {
            // Trivial initialisations
            String name = requireAttribute(packElement, "name");
            String id = packElement.getAttribute("id");

            boolean loose = "true".equalsIgnoreCase(packElement.getAttribute("loose", "false"));
            String description = requireChildNamed(packElement, "description").getContent();
            boolean required = requireYesNoAttribute(packElement, "required");
            String group = packElement.getAttribute("group");
            String installGroups = packElement.getAttribute("installGroups");
            String excludeGroup = packElement.getAttribute("excludeGroup");
            boolean uninstall = "yes".equalsIgnoreCase(packElement.getAttribute("uninstall", "yes"));
            String parent = packElement.getAttribute("parent");

            if (required && excludeGroup != null)
            {
                parseError(packElement, "Pack, which has excludeGroup can not be required.", new Exception(
                        "Pack, which has excludeGroup can not be required."));
            }

            PackInfo pack = new PackInfo(name, id, description, required, loose, excludeGroup, uninstall, 0);
            pack.setOsConstraints(OsConstraintHelper.getOsList(packElement)); // TODO:
            pack.setParent(parent);

            // unverified
            // if the pack belongs to an excludeGroup it's not preselected by default
            if (excludeGroup == null)
            {
                pack.setPreselected(validateYesNoAttribute(packElement, "preselected", YES));
            }
            else
            {
                pack.setPreselected(validateYesNoAttribute(packElement, "preselected", NO));
            }

            // Set the pack group if specified
            if (group != null)
            {
                pack.setGroup(group);
            }
            // Set the pack install groups if specified
            if (installGroups != null)
            {
                StringTokenizer tokenizer = new StringTokenizer(installGroups, ",");
                while (tokenizer.hasMoreTokens())
                {
                    String igroup = tokenizer.nextToken();
                    pack.addInstallGroup(igroup);
                }
            }

            // We get the parsables list
            for (IXMLElement parsableElement : packElement.getChildrenNamed("parsable"))
            {
                String target = parsableElement.getAttribute("targetfile", null);
                SubstitutionType type = SubstitutionType.lookup(parsableElement.getAttribute("type", "plain"));
                String encoding = parsableElement.getAttribute("encoding", null);
                List<OsModel> osList = OsConstraintHelper.getOsList(parsableElement); // TODO: unverified
                if (target != null)
                {
                    pack.addParsable(new ParsableFile(target, type, encoding, osList));
                }
            }

            // We get the executables list
            for (IXMLElement executableElement : packElement.getChildrenNamed("executable"))
            {
                ExecutableFile executable = new ExecutableFile();
                String val; // temp value

                executable.path = requireAttribute(executableElement, "targetfile");

                // when to execute this executable
                val = executableElement.getAttribute("stage", "never");
                if ("postinstall".equalsIgnoreCase(val))
                {
                    executable.executionStage = ExecutableFile.POSTINSTALL;
                }
                else if ("uninstall".equalsIgnoreCase(val))
                {
                    executable.executionStage = ExecutableFile.UNINSTALL;
                }

                // type of this executable
                val = executableElement.getAttribute("type", "bin");
                if ("jar".equalsIgnoreCase(val))
                {
                    executable.type = ExecutableFile.JAR;
                    executable.mainClass = executableElement.getAttribute("class"); // executable
                    // class
                }

                // what to do if execution fails
                val = executableElement.getAttribute("failure", "ask");
                if ("abort".equalsIgnoreCase(val))
                {
                    executable.onFailure = ExecutableFile.ABORT;
                }
                else if ("warn".equalsIgnoreCase(val))
                {
                    executable.onFailure = ExecutableFile.WARN;
                }

                // whether to keep the executable after executing it
                val = executableElement.getAttribute("keep");
                executable.keepFile = "true".equalsIgnoreCase(val);

                // get arguments for this executable
                IXMLElement args = executableElement.getFirstChildNamed("args");
                if (null != args)
                {
                    for (IXMLElement arg : args.getChildrenNamed("arg"))
                    {
                        executable.argList.add(requireAttribute(arg, "value"));
                    }
                }

                executable.osList = OsConstraintHelper.getOsList(executableElement); // TODO:
                // unverified

                pack.addExecutable(executable);
            }

            // get the updatechecks list
            for (IXMLElement fileElement : packElement.getChildrenNamed("updatecheck"))
            {
                String casesensitive = fileElement.getAttribute("casesensitive");

                // get includes and excludes
                ArrayList<String> includesList = new ArrayList<String>();
                ArrayList<String> excludesList = new ArrayList<String>();

                // get includes and excludes
                for (IXMLElement inc_el : fileElement.getChildrenNamed("include"))
                {
                    includesList.add(requireAttribute(inc_el, "name"));
                }

                for (IXMLElement excl_el : fileElement.getChildrenNamed("exclude"))
                {
                    excludesList.add(requireAttribute(excl_el, "name"));
                }

                pack.addUpdateCheck(new UpdateCheck(includesList, excludesList, casesensitive));
            }
            // We get the dependencies
            for (IXMLElement dep : packElement.getChildrenNamed("depends"))
            {
                String depName = requireAttribute(dep, "packname");
                pack.addDependency(depName);

            }
            result.add(pack);
        }
        return result;
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws CompilerException
    {
        throw new CompilerException(installFilename + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message) throws CompilerException
    {
        throw new CompilerException(installFilename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a chained parse error with consistent messages. Includes file name and line # of
     * parent. It is an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message, Throwable cause) throws CompilerException
    {
        throw new CompilerException(installFilename + ":" + parent.getLineNr() + ": " + message, cause);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent. It
     * is an error for 'parent' to be null.
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(IXMLElement parent, String message)
    {
        System.out.println(installFilename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     *
     * @param parent The element to search for a child
     * @param name   Name of the child element to get
     */
    protected IXMLElement requireChildNamed(IXMLElement parent, String name) throws CompilerException
    {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
        {
            parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">");
        }
        return child;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    protected URL requireURLContent(IXMLElement element) throws CompilerException
    {
        URL url = null;
        try
        {
            url = new URL(requireContent(element));
        }
        catch (MalformedURLException x)
        {
            parseError(element, "<" + element.getName() + "> requires valid URL", x);
        }
        return url;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    protected String requireContent(IXMLElement element) throws CompilerException
    {
        String content = element.getContent();
        if (content == null || content.length() == 0)
        {
            parseError(element, "<" + element.getName() + "> requires content");
        }
        return content;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(IXMLElement element, String attribute) throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        return value;
    }

    /**
     * Get a required attribute of an element, ensuring it is an integer. A meaningful error message
     * is generated as a CompilerException if not present or parseable as an int. It is an error for
     * 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected int requireIntAttribute(IXMLElement element, String attribute) throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x)
        {
            parseError(element, "'" + attribute + "' must be an integer");
        }
        return 0; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or one
     * of "yes" or "no". It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected boolean requireYesNoAttribute(IXMLElement element, String attribute) throws CompilerException
    {
        String value = requireAttribute(element, attribute);
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        parseError(element, "<" + element.getName() + "> invalid attribute '" + attribute + "': Expected (yes|no)");

        return false; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful warning if not "yes" or "no". If the
     * 'element' or 'attribute' are null, the default value is returned.
     *
     * @param element      The element to get the attribute value of
     * @param attribute    The name of the attribute to get
     * @param defaultValue Value returned if attribute not present or invalid
     */
    protected boolean validateYesNoAttribute(IXMLElement element, String attribute, boolean defaultValue)
    {
        if (element == null)
        {
            return defaultValue;
        }

        String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        // TODO: should this be an error if it's present but "none of the
        // above"?
        parseWarn(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no) if present");

        return defaultValue;
    }

}
