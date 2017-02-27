package com.izforge.izpack.data;

/**
 * Enumeration for custom data type
 */
public enum CustomDataType
{

    /**
     * Identifier for custom data type "installer listener".
     */
    INSTALLER_LISTENER("installer"),
    /**
     * Identifier for custom data typ "uninstaller listener".
     */
    UNINSTALLER_LISTENER("uninstaller"),
    /**
     * Identifier for custom data typ "uninstaller lib". This is used for binary
     * libs (DLLs or SHLs or SOs or ...) which will be needed from the
     * uninstaller.
     */
    UNINSTALLER_LIB(""),
    /**
     * Identifier for custom data typ "uninstaller jar files".
     */
    UNINSTALLER_JAR("");

    private String attribute;

    CustomDataType(String attribute)
    {
        this.attribute = attribute;
    }

    public String getAttribute()
    {
        return attribute;
    }
}
