package com.izforge.izpack.panels.shortcut;

public class ShortcutConstants
{
    private ShortcutConstants()
    {
        //Prevent instantiation
    }

    final static String SPEC_ATTRIBUTE_CONDITION = "condition";

    final static String SPEC_ATTRIBUTE_KDE_USERNAME = "KdeUsername";

    final static String SPEC_ATTRIBUTE_KDE_SUBST_UID = "KdeSubstUID";

    final static String SPEC_ATTRIBUTE_URL = "url";

    final static String SPEC_ATTRIBUTE_TYPE = "type";

    final static String SPEC_ATTRIBUTE_TERMINAL_OPTIONS = "terminalOptions";

    final static String SPEC_ATTRIBUTE_TERMINAL = "terminal";

    final static String SPEC_ATTRIBUTE_MIMETYPE = "mimetype";

    final static String SPEC_ATTRIBUTE_ENCODING = "encoding";

    static final String SPEC_CATEGORIES = "categories";

    static final String SPEC_TRYEXEC = "tryexec";

    static final String SEPARATOR_LINE = "--------------------------------------------------------------------------------";

    static final String SPEC_FILE_NAME = "shortcutSpec.xml";

    // ------------------------------------------------------
    // spec file section keys
    // -----------------------------------------------------
    static final String SPEC_KEY_SKIP_IFNOT_SUPPORTED = "skipIfNotSupported";

    static final String SPEC_KEY_NOT_SUPPORTED = "notSupported";

    static final String SPEC_KEY_DEF_CUR_USER = "defaultCurrentUser";

    static final String SPEC_KEY_LATE_INSTALL = "lateShortcutInstall";

    static final String SPEC_KEY_PROGRAM_GROUP = "programGroup";

    static final String SPEC_KEY_SHORTCUT = "shortcut";

    static final String SPEC_KEY_PACKS = "createForPack";

    // ------------------------------------------------------
    // spec file key attributes
    // ------------------------------------------------------
    static final String SPEC_ATTRIBUTE_DEFAULT_GROUP = "defaultName";

    static final String SPEC_ATTRIBUTE_INSTALLGROUP = "installGroup";

    static final String SPEC_ATTRIBUTE_LOCATION = "location";

    static final String SPEC_ATTRIBUTE_NAME = "name";

    static final String SPEC_ATTRIBUTE_SUBGROUP = "subgroup";

    static final String SPEC_ATTRIBUTE_DESCRIPTION = "description";

    static final String SPEC_ATTRIBUTE_TARGET = "target";

    static final String SPEC_ATTRIBUTE_COMMAND = "commandLine";

    static final String SPEC_ATTRIBUTE_ICON = "iconFile";

    static final String SPEC_ATTRIBUTE_ICON_INDEX = "iconIndex";

    static final String SPEC_ATTRIBUTE_WORKING_DIR = "workingDirectory";

    static final String SPEC_ATTRIBUTE_INITIAL_STATE = "initialState";

    static final String SPEC_ATTRIBUTE_DESKTOP = "desktop";

    static final String SPEC_ATTRIBUTE_APPLICATIONS = "applications";

    static final String SPEC_ATTRIBUTE_START_MENU = "startMenu";

    static final String SPEC_ATTRIBUTE_STARTUP = "startup";

    static final String SPEC_ATTRIBUTE_PROGRAM_GROUP = "programGroup";

    static final String SPEC_ATTRIBUTE_RUN_AS_ADMINISTRATOR = "runAsAdministrator";

    // ------------------------------------------------------
    // spec file attribute values
    // ------------------------------------------------------

    static final String SPEC_VALUE_APPLICATIONS = "applications";

    static final String SPEC_VALUE_START_MENU = "startMenu";

    static final String SPEC_VALUE_NO_SHOW = "noShow";

    static final String SPEC_VALUE_NORMAL = "normal";

    static final String SPEC_VALUE_MAXIMIZED = "maximized";

    static final String SPEC_VALUE_MINIMIZED = "minimized";

    // ------------------------------------------------------
    // automatic script keys attributes values
    // ------------------------------------------------------
    static final String AUTO_KEY_PROGRAM_GROUP = "programGroup";

    static final String AUTO_KEY_SHORTCUT_TYPE = "shortcutType";

    static final String AUTO_KEY_CREATE_MENU_SHORTCUTS = "createMenuShortcuts";

    static final String AUTO_KEY_CREATE_DESKTOP_SHORTCUTS = "createDesktopShortcuts";

    static final String AUTO_KEY_CREATE_STARTUP_SHORTCUTS = "createStartupShortcuts";

    static final String[] SHORTCUT_TYPES = {AUTO_KEY_CREATE_MENU_SHORTCUTS, AUTO_KEY_CREATE_DESKTOP_SHORTCUTS, AUTO_KEY_CREATE_STARTUP_SHORTCUTS};
    //For backwards comparability reasons
    static final String AUTO_KEY_CREATE_SHORTCUTS_LEGACY = "createShortcuts";

    static final String AUTO_KEY_SHORTCUT_TYPE_VALUE_ALL = "all";

    static final String AUTO_KEY_SHORTCUT_TYPE_VALUE_USER = "user";

    // permission flags

    static final String CREATE_FOR_ALL = "createForAll";

    static final String DEFAULT_FOLDER = "(Default)";
}
