package com.izforge.izpack.installer.container.provider;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.gui.InstallerFrame;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Provide icons database
 */
@ApplicationScoped
public class IconsProvider
{
    private static final Logger logger = Logger.getLogger(IconsProvider.class.getName());

    @Produces
    public IconsDatabase provide(Resources resources) throws Exception
    {
        IconsDatabase icons = new IconsDatabase();
        loadIcons(icons);
        loadCustomIcons(icons, resources);
        return icons;
    }

    /**
     * Loads the icons.
     *
     * @param iconsDatabase
     * @throws Exception Description of the Exception
     */
    private void loadIcons(IconsDatabase iconsDatabase) throws Exception
    {
        // Initialisations
        InputStream inXML = getClass().
                getResourceAsStream("icons.xml");

        parseXML(inXML, iconsDatabase);
    }

    /**
     * Loads custom icons into the installer.
     *
     * @param icons     the icons database
     * @param resources used to load the icons
     * @throws Exception
     */
    private void loadCustomIcons(IconsDatabase icons, Resources resources) throws Exception
    {
        // We try to load and add a custom langpack.
        InputStream inXML = null;
        try
        {
            inXML = resources.getInputStream(Resources.CUSTOM_ICONS_RESOURCE_NAME);
        }
        catch (Throwable exception)
        {
            logger.fine("Resource " + Resources.CUSTOM_ICONS_RESOURCE_NAME
                                   + " not defined. No custom icons available");
            return;
        }
        logger.fine("Custom icons available");

        parseXML(inXML, icons);
    }

    /**
     * parse the xml and fill in the db
     *
     * @param inXML
     * @param icons
     */
    private void parseXML(InputStream inXML, IconsDatabase icons)
    {
        ImageIcon img;
        // Initialises the parser
        IXMLParser parser = new XMLParser(false);

        // We get the data
        IXMLElement data = parser.parse(inXML);

        // We load the icons
        for (IXMLElement icon : data.getChildrenNamed("icon"))
        {
            img = loadIcon(icon);
            if (img != null)
            {
                icons.put(icon.getAttribute("id"), img);
            }
        }

        // We load the Swing-specific icons
        for (IXMLElement icon : data.getChildrenNamed("sysicon"))
        {
            img = loadIcon(icon);
            if (img != null)
            {
                UIManager.put(icon.getAttribute("id"), img);
            }
        }
    }

    /**
     * Loads an icon declared in an XML file.
     *
     * @param icon
     *            the XML element that declares the icon
     * @return the icon or <code>null</icon> if it does not exist
     */
    private ImageIcon loadIcon(IXMLElement icon)
    {
        ImageIcon img = null;
        String id = icon.getAttribute("id");
        String path = icon.getAttribute("res");
        URL url = InstallerFrame.class.getResource(path);
        if (url == null)
        {
            logger.warning("Icon with id '" + id + "': file '" + path + "' not found");
        }
        else
        {
            img = new ImageIcon(url);
            logger.fine("Icon with id '" + id + "' found");
        }
        return img;
    }
}
