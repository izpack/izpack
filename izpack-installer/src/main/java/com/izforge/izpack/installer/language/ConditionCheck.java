package com.izforge.izpack.installer.language;

import java.awt.Font;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.installer.InstallerRequirementDisplay;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.FileUtil;

/**
 * Checker for java version, JDK and running install
 *
 * @deprecated This doesn't support console installations. For a replacement, see
 *             {@link com.izforge.izpack.installer.requirement.RequirementsChecker}
 */
@Deprecated
public class ConditionCheck
{
    private static final Logger logger = Logger.getLogger(ConditionCheck.class.getName());

    private InstallData installdata;
    private ResourceManager resourceManager;
    private RulesEngine rules;


    public ConditionCheck(InstallData installdata, ResourceManager resourceManager, RulesEngine rules)
    {
        this.installdata = installdata;
        this.resourceManager = resourceManager;
        this.rules = rules;
    }

    public void check() throws Exception
    {
        checkJavaVersion();
        checkJDKAvailable();
        // Check for already running instance
        checkLockFile();
        checkLangPackAvaible();
    }

    /**
     * Sets a lock file. Not using java.nio.channels.FileLock to prevent
     * the installer from accidentally keeping a lock on a file if the install
     * fails or is killed.
     *
     * @throws Exception Description of the Exception
     */
    public void checkLockFile() throws Exception
    {
        String appName = installdata.getInfo().getAppName();
        File file = FileUtil.getLockFile(appName);
        if (file.exists())
        {
            // Ask user if they want to proceed.
            logger.fine("Lock File Exists, asking user for permission to proceed.");
            StringBuilder msg = new StringBuilder();
            msg.append("<html>");
            msg.append("The ").append(appName).append(
                    " installer you are attempting to run seems to have a copy already running.<br><br>");
            msg.append(
                    "This could be from a previous failed installation attempt or you may have accidentally launched <br>");
            msg.append(
                    "the installer twice. <b>The recommended action is to select 'Exit'</b> and wait for the other copy of <br>");
            msg.append(
                    "the installer to start. If you are sure there is no other copy of the installer running, click <br>");
            msg.append("the 'Continue' button to allow this installer to run. <br><br>");
            msg.append("Are you sure you want to continue with this installation?");
            msg.append("</html>");
            JLabel label = new JLabel(msg.toString());
            label.setFont(new Font("Sans Serif", Font.PLAIN, 12));
            Object[] optionValues = {"Continue", "Exit"};
            int selectedOption = JOptionPane.showOptionDialog(null, label, "Warning",
                                                              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                                              null, optionValues,
                                                              optionValues[1]);
            logger.fine("Selected option: " + selectedOption);
            if (selectedOption == 0)
            {
                // Take control of the file so it gets deleted after this installer instance exits.
                logger.fine("Setting temporary file to delete on exit");
                file.deleteOnExit();
            }
            else
            {
                // Leave the file as it is.
                logger.fine("Leaving temporary file alone and exiting");
                System.exit(1);
            }
        }
        else
        {
            try
            {
                // Create the new lock file
                if (file.createNewFile())
                {
                    logger.fine("Temporary file created");
                    file.deleteOnExit();
                }
                else
                {
                    logger.warning("Temporary file could not be created");
                    logger.warning("*** Multiple instances of installer will be allowed ***");
                }
            }
            catch (Exception e)
            {
                logger.log(Level.WARNING, "Temporary file could not be created: " + e.getMessage(), e);
                logger.warning("*** Multiple instances of installer will be allowed ***");
            }
        }
    }

    /**
     * Checks the Java version.
     *
     * @throws Exception Description of the Exception
     */
    private void checkJavaVersion() throws Exception
    {
        String version = System.getProperty("java.version");
        String required = installdata.getInfo().getJavaVersion();
        if (version.compareTo(required) < 0)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("The application that you are trying to install requires a ");
            msg.append(required);
            msg.append(" version or later of the Java platform.\n");
            msg.append("You are running a ");
            msg.append(version);
            msg.append(" version of the Java platform.\n");
            msg.append("Please upgrade to a newer version.");

            System.out.println(msg.toString());
            JOptionPane.showMessageDialog(null, msg.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Checks if a JDK is available.
     */
    private void checkJDKAvailable()
    {
        if (!installdata.getInfo().isJdkRequired())
        {
            return;
        }

        FileExecutor exec = new FileExecutor();
        String[] output = new String[2];
        String[] params = {"javac", "-help"};
        if (exec.executeCommand(params, output) != 0)
        {
            String[] message = {
                    "It looks like your system does not have a Java Development Kit (JDK) available.",
                    "The software that you plan to install requires a JDK for both its installation and execution.",
                    "\n",
                    "Do you still want to proceed with the installation process?"
            };
            int status = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION,
                                                       JOptionPane.WARNING_MESSAGE);
            if (status == JOptionPane.NO_OPTION)
            {
                System.exit(1);
            }
        }
    }

    /**
     * Checks installation requirements.
     *
     * @param display the display to log missing requirements to
     * @return <tt>true</tt> if the installation requirements are met, otherwise <tt>false</tt> if not
     * @throws IzPackException if a {@link InstallerRequirement} condition is not defined
     */
    public boolean checkInstallerRequirements(InstallerRequirementDisplay display)
    {
        boolean result = true;

        for (InstallerRequirement installerrequirement : installdata.getInstallerRequirements())
        {
            String conditionid = installerrequirement.getCondition();
            Condition condition = rules.getCondition(conditionid);
            if (condition == null)
            {
                logger.warning(conditionid + " is not a valid condition.");
                throw new IzPackException(conditionid + " could not be found as a defined condition");
            }
            if (!condition.isTrue())
            {
                String message = installerrequirement.getMessage();
                if ((message != null) && (message.length() > 0))
                {
                    String localizedMessage = installdata.getMessages().get(message);
                    display.showMissingRequirementMessage(localizedMessage);
                }
                result = false;
                break;
            }
        }
        return result;
    }

    public boolean checkLangPackAvaible() throws Exception
    {
        java.util.List<String> availableLangPacks = resourceManager.getAvailableLangPacks();
        return availableLangPacks.size() != 0;
    }

}

