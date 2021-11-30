package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceInterruptedException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.web.WebRepositoryAccessor;
import com.izforge.izpack.util.IoHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * {@link PackResources} implementation for the GUI-based installer.
 * <p/>
 * This supports both local and web-based pack resources.
 */
public class GUIPackResources extends AbstractPackResources
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(GUIPackResources.class.getName());

    /**
     * Constructs a {@code GUIPackResources}.
     *
     * @param resources   the resources
     * @param installData the installation data
     */
    public GUIPackResources(Resources resources, InstallData installData)
    {
        super(resources, installData);
    }

    @Override
    protected InputStream getWebPackStream(String name, String webDirURL)
    {
        InputStream result;

        InstallData installData = getInstallData();
        String baseName = installData.getInfo().getInstallerBase();
        File installerDir = new File(baseName).getParentFile();

        baseName = baseName.substring(baseName.lastIndexOf(baseName.contains("\\") ? '\\' :'/') + 1);
        String packFileName = baseName + ".pack-" + name + ".jar";
        String path;

        // Look first in same directory as primary jar, then download it if not found
        File packLocalFile = new File(installerDir, packFileName);
        if (packLocalFile.exists() && packLocalFile.canRead())
        {
            logger.info("Found local pack " + packLocalFile.getAbsolutePath());
            try {

                path = "jar:" + packLocalFile.toURI().toURL() + "!/packs/pack-" + name;
            } catch(MalformedURLException exception) {
                throw new ResourceException("Malformed URL", exception);
            }
        }
        else
        {
            String packURL = webDirURL + "/" + baseName + ".pack-" + name + ".jar";
            logger.info("Downloading remote pack " + packURL);
            String tempFolder = IoHelper.translatePath(installData.getInfo().getUninstallerPath()
                    + WEB_TEMP_SUB_PATH, installData.getVariables());
            String tempFile;
            try
            {
                tempFile = WebRepositoryAccessor.getCachedUrl(packURL, tempFolder);
                packLocalFile = new File(tempFile);
            }
            catch (InterruptedIOException exception)
            {
                throw new ResourceInterruptedException("Retrieval of " + webDirURL + " interrupted", exception);
            }
            catch (IOException exception)
            {
                throw new ResourceException("Failed to read " + webDirURL, exception);
            }

            path = "jar:" + packLocalFile.getPath() + "!/packs/pack-" + name;
        }

        try
        {
            URL url = new URL(path);
            result = url.openStream();
        }
        catch (IOException exception)
        {
            throw new ResourceException("Failed to read pack", exception);
        }
        return result;
    }

}
