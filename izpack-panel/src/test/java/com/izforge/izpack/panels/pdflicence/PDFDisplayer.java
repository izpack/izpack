package com.izforge.izpack.panels.pdflicence;

import static org.icepdf.ri.util.PropertiesManager.PROPERTY_DEFAULT_PAGEFIT;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_DEFAULT_ZOOM_LEVEL;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_HIDE_UTILITYPANE;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_KEYBOARD_SHORTCUTS;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_STATUSBAR;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_STATUSBAR_STATUSLABEL;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_FIT;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_FORMS;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_PAGENAV;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_TOOL;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_UTILITY;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_TOOLBAR_ZOOM;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION_FLAGS;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITYPANE_BOOKMARKS;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITYPANE_LAYERS;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITYPANE_SEARCH;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITYPANE_THUMBNAILS;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITY_OPEN;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITY_PRINT;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITY_SAVE;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITY_SEARCH;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_SHOW_UTILITY_UPANE;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_VIEWPREF_FITWINDOW;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_VIEWPREF_FORM_HIGHLIGHT;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_VIEWPREF_HIDEMENUBAR;
import static org.icepdf.ri.util.PropertiesManager.PROPERTY_VIEWPREF_HIDETOOLBAR;

import java.awt.FlowLayout;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.util.PropertiesManager;

public class PDFDisplayer extends javax.swing.JFrame
{

    private static final String FILE_NAME = "./src/test/resources/com/izforge/izpack/panels/panel/PDFLicencePanel.pdf";
    private static final long serialVersionUID = 1L;

    public PDFDisplayer()
    {
        initComponents();
    }

    private void initComponents()
    {
        setTitle("PDF Licence");
        setSize(600, 900);

        final SwingController controller = new SwingController();
        final SwingViewBuilder builder = new SwingViewBuilder(controller, createProperties());
        final JPanel viewerComponentPanel = builder.buildViewerPanel();

        ComponentKeyBinding.install(controller, viewerComponentPanel);

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitApplication(controller);
            }
        });

        getContentPane().add(controller.getDocumentViewController().getViewContainer(), java.awt.BorderLayout.CENTER);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.add(builder.buildFitToolBar());
        p.add(builder.buildPageNavigationToolBar());
        p.add(builder.buildZoomToolBar());
        getContentPane().add(p, java.awt.BorderLayout.SOUTH);

        // Open a PDF document to view
        controller.openDocument(FILE_NAME);
        // controller.setPageFitMode(DocumentViewController.PAGE_FIT_WINDOW_WIDTH,
        // true);
    }

    private void exitApplication(SwingController controller)
    {
        this.setVisible(false);
        controller.closeDocument();
        this.dispose();
    }

    /**
     * @param args the command line arguments
     *
     * @throws Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception
    {
        PDFDisplayer viewer = new PDFDisplayer();
        viewer.setVisible(true);
    }

    private PropertiesManager createProperties()
    {

        Properties result = new Properties();

        // General
        result.setProperty(PROPERTY_DEFAULT_PAGEFIT, Integer.toString(DocumentViewController.PAGE_FIT_WINDOW_WIDTH));

        result.setProperty(PROPERTY_VIEWPREF_HIDEMENUBAR, "false");
        // Hides the menubar. Default value false.
        result.setProperty(PROPERTY_VIEWPREF_HIDETOOLBAR, "false");
        // Hides the top toolbar. Default value false.
        result.setProperty(PROPERTY_VIEWPREF_FORM_HIGHLIGHT, "false");
        result.setProperty(PROPERTY_VIEWPREF_FITWINDOW, "true");
        // When enabled shows the document with fit to width if the document
        // does not already specify a default view. Default value, true.
        result.setProperty(PROPERTY_SHOW_KEYBOARD_SHORTCUTS, "false");
        // Enables/disables menubar key events, default is true.
        result.setProperty("application.alwaysShowImageSplashWindow", "no");
        // Enables/disables the splash window, default is no
        result.setProperty("application.chromeOnStartup", "no");
        // Start with with chrome look and feel, default value is yes.
        result.setProperty("application.showLocalStorageDialogs", "no");
        // Yes is default value. Show dialog to use before writing properties to
        // disk.
        // "application.datadir - Directory to write properties file to in users
        // home, default .icesoft/icepdf-viewer.
        // Toolbar - General
        result.setProperty(PROPERTY_SHOW_TOOLBAR_PAGENAV, "false");
        // Show page navigation controls; first, previous, next and last.
        // Default is true.
        result.setProperty(PROPERTY_SHOW_TOOLBAR_FIT, "false");
        // Shows page fit controls; normal, hight and width. Default is true.
        result.setProperty(PROPERTY_SHOW_TOOLBAR_ZOOM, "false");
        // Shows page zoom controls. Default is true.
        result.setProperty(PROPERTY_SHOW_TOOLBAR_ROTATE, "false");
        // Shows page rotation controls. Default is true.
        result.setProperty(PROPERTY_SHOW_TOOLBAR_TOOL, "false");
        // Hide all toolbars. Default is false.
        result.setProperty(PROPERTY_SHOW_TOOLBAR_ANNOTATION, "false");
        // Shows annotation creation controls. Default is true.
        result.setProperty(PROPERTY_SHOW_TOOLBAR_FORMS, "false");
        // Shows utility pane control to show/hide utility pane. Default is
        // true.
        // Toolbar - Zoom
        result.setProperty(PROPERTY_DEFAULT_ZOOM_LEVEL, "1.3");
        // Float value of default page zoom. Default if 1.0
        // Toolbar - Utility
        result.setProperty(PROPERTY_SHOW_TOOLBAR_UTILITY, "false");
        // show utility toolbar and children. Default is true.
        result.setProperty(PROPERTY_SHOW_UTILITY_SAVE, "false");
        // Show the save control, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITY_OPEN, "false");
        // Show the open control, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITY_SEARCH, "false");
        // Show the search control, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITY_UPANE, "false");
        // Show the utility pane control, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITY_PRINT, "false");
        // Show the print control, default is true.

        // Utility Pane
        result.setProperty(PROPERTY_HIDE_UTILITYPANE, "true");
        result.setProperty(PROPERTY_SHOW_UTILITYPANE_BOOKMARKS, "false");
        // Show the document outline panel tab, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITYPANE_SEARCH, "false");
        // Show the search panel tab, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITYPANE_ANNOTATION, "false");
        // Show the annotation properties pane, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITYPANE_THUMBNAILS, "false");
        // Show the thumbnail view pane, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITYPANE_LAYERS, "false");
        // Show the layers view pane, default is true.
        result.setProperty(PROPERTY_SHOW_UTILITYPANE_ANNOTATION_FLAGS, "false");
        // Status Bar
        result.setProperty(PROPERTY_SHOW_STATUSBAR, "false");
        // Show the status bar and child component, default is true.
        result.setProperty(PROPERTY_SHOW_STATUSBAR_STATUSLABEL, "false");
        // Show the status label, default is true.
        result.setProperty(PROPERTY_SHOW_STATUSBAR_VIEWMODE, "false");
        // Show the view mode controls, default is true.

        return new PropertiesManager(System.getProperties(), result,
                ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
    }

}
