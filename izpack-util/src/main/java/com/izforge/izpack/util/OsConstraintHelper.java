package com.izforge.izpack.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;

/**
 * Encapsulates OS constraints specified on creation time and allows to check them against the
 * current OS.
 * <p/>
 * For example, this is used for &lt;executable&gt;s to check whether the executable is suitable for
 * the current OS.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class OsConstraintHelper
{
    private static final Logger logger = Logger.getLogger(OsConstraintHelper.class.getName());

    /**
     * Matches OS specification in this class against current system properties.
     *
     * @param osModel
     * @return Description of the Return Value
     * @deprecated use {@link PlatformModelMatcher#matchesCurrentPlatform(OsModel)}
     */
    @Deprecated
    public static boolean matchCurrentSystem(OsModel osModel)
    {
        boolean match = true;
        String osName = System.getProperty("os.name").toLowerCase();


        if ((osModel.getArch() != null) && (osModel.getArch().length() != 0))
        {
            match = System.getProperty("os.arch").toLowerCase().equals(osModel.getArch());
        }    // end if

        if (match && (osModel.getVersion() != null) && (osModel.getVersion().length() != 0))
        {
            match = System.getProperty("os.version").toLowerCase().equals(osModel.getVersion());
        }    // end if

        if (match && (osModel.getName() != null) && (osModel.getName().length() != 0))
        {
            match = osName.equals(osModel.getName());
        }    // end if

        if (match && (osModel.getFamily() != null))
        {
            if ("windows".equals(osModel.getFamily()))
            {
                match = OsVersion.IS_WINDOWS;
            }    // end if
            else if ("mac".equals(osModel.getFamily()) || "osx".equals(osModel.getFamily()))
            {
                match = OsVersion.IS_OSX;
            }    // end else if
            else if ("unix".equals(osModel.getFamily()))
            {
                match = OsVersion.IS_UNIX;
            }    // end else if
        }    // end if

        if (match && (osModel.getJre() != null) && (osModel.getJre().length() > 0))
        {
            match = System.getProperty("java.version").toLowerCase().startsWith(osModel.getJre());
        }

        return match
                && ((osModel.getFamily() != null) || (osModel.getName() != null) || (osModel.getVersion() != null) || (osModel.getArch() != null) || (osModel.getJre() != null));
    }

    /**
     * Extract a list of OS constraints from given element.
     *
     * @param element parent IXMLElement
     * @return List of OsModel (or empty List if no constraints found)
     */
    public static List<OsModel> getOsList(IXMLElement element)
    {
        // get os info on this executable
        ArrayList<OsModel> osList = new ArrayList<OsModel>();
        for (IXMLElement osElement : element.getChildrenNamed("os"))
        {
            osList.add(
                    new OsModel(
                            osElement.getAttribute("arch",
                                                   null),
                            osElement.getAttribute("family",
                                                   null),
                            osElement.getAttribute("jre",
                                                   null),
                            osElement.getAttribute("name",
                                                   null),
                            osElement.getAttribute("version",
                                                   null))
            );
        }
        // backward compatibility: still support os attribute
        String osattr = element.getAttribute("os");
        if ((osattr != null) && (osattr.length() > 0))
        {
            // add the "os" attribute as a family constraint
            osList.add(
                    new OsModel(null, osattr, null, null, null)
            );
        }

        return osList;
    }

    /**
     * Helper function: Scan a list of OsConstraints for a match.
     *
     * @param constraint_list List of OsModel to check
     * @return true if one of the OsConstraints matched the current system or constraint_list is
     *         null (no constraints), false if none of the OsConstraints matched
     * @deprecated use {@link PlatformModelMatcher#matchesCurrentPlatform(java.util.List)}
     */
    @Deprecated
    public static boolean oneMatchesCurrentSystem(List<OsModel> constraint_list)
    {
        if (constraint_list == null || constraint_list.isEmpty())
        {
            return true;
        }
        for (OsModel osModel : constraint_list)
        {
            logger.fine("Checking if os constraints " + osModel + " match current OS");
            // check for match
            if (matchCurrentSystem(osModel))
            {
                logger.fine("OS constraints matched current OS");
                return true;    // bail out on first match
            }    // end if
        }    // end while

        logger.fine("OS constraints do not match current OS");

        // no match found
        return false;
    }

    /**
     * Helper function: Check whether the given IXMLElement is "suitable" for the current OS.
     *
     * @param el The IXMLElement to check for OS constraints.
     * @return true if there were no OS constraints or the constraints matched the current OS.
     * @deprecated no replacement
     */
    @Deprecated
    public static boolean oneMatchesCurrentSystem(IXMLElement el)
    {
        return oneMatchesCurrentSystem(getOsList(el));
    }
}
