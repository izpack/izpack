package com.izforge.izpack.panels.pdflicence;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.junit.Test;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.DefaultNavigator;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platform.Name;

public class PDFLicencePanelTest
{

    @Test
    public void testPDFLicencePanel() throws InvocationTargetException, InterruptedException
    {
        Runnable runnable = new Runnable()
        {

            @Override
            public void run()
            {

                Name name = (OsVersion.IS_WINDOWS) ? Name.WINDOWS : (OsVersion.IS_MAC) ? Name.MAC : Name.UNIX;

                Variables variables = new DefaultVariables();
                Platform platform = new Platform(name);

                GUIInstallData installData = new GUIInstallData(variables, platform);
                installData.guiPrefs = new GUIPrefs();
                installData.setMessages(createMessage());
                Messages messages = installData.getMessages();
                messages.getMessages().put("installer.prev", "installer.prev");

                Log log = new Log(installData);
                ResourceManager resourceManager = new ResourceManager();

                Container container = new DefaultContainer();
                ConditionContainer conditionContainer = new ConditionContainer(container);

                RulesEngine rules = new RulesEngineImpl(installData, conditionContainer, platform);
                installData.setRules(rules);

                IconsDatabase icons = new IconsDatabase();

                ObjectFactory factory = new DefaultObjectFactory(container);
                Panel panel = new Panel();

                List<IzPanelView> panelViews = Collections.singletonList(new IzPanelView(panel, factory, installData));

                IzPanels panels = new IzPanels(panelViews, container, installData);
                MergeableResolver mergeableResolver = new MergeableResolver();
                PathResolver pathResolver = new PathResolver(mergeableResolver);
                UninstallData uninstallData = new UninstallData();
                UninstallDataWriter uninstallDataWriter = new UninstallDataWriter(uninstallData, installData,
                        pathResolver, rules);
                Housekeeper housekeeper = new Housekeeper();
                DefaultNavigator navigator = new DefaultNavigator(panels, icons, installData);
                Locales locales = new DefaultLocales(resourceManager);

                InstallerFrame window = new InstallerFrame(installData, rules, icons, panels,
                        uninstallDataWriter, resourceManager, uninstallData, housekeeper, navigator, log, locales);

                PDFLicencePanel licencePanel = new PDFLicencePanel(panel, window, installData, resourceManager, log);

                window.setSize(400, 400);
                licencePanel.setSize(400, 400);
                window.add(licencePanel);

                window.pack();
                window.setVisible(true);

            }
        };
        SwingUtilities.invokeAndWait(runnable);

    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        new PDFLicencePanelTest().testPDFLicencePanel();
    }

    private Messages createMessage()
    {
        return new Messages()
        {

            private Map<String, String> messages = new HashMap<String, String>();

            @Override
            public Messages newMessages(String name)
            {
                // TODO Auto-generated method stub
                messages.put(name, name);
                return this;
            }

            @Override
            public Map<String, String> getMessages()
            {
                // TODO Auto-generated method stub
                return messages;
            }

            @Override
            public String get(String id, Object... args)
            {
                // TODO Auto-generated method stub
                return messages.get(id);
            }

            @Override
            public void add(Messages messages)
            {
                this.messages.putAll(messages.getMessages());

            }
        };
    }

}
