package com.izforge.izpack.installer.panel;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.installer.DataValidator;

/**
 * Encapsulates a {@link Panel} and its user-interface representation.
 *
 * @author Tim Anderson
 */
public interface PanelView<T>
{
    /**
     * Returns the panel identifier.
     *
     * @return the panel identifier
     */
    String getPanelId();

    /**
     * Returns the panel.
     *
     * @return the panel
     */
    Panel getPanel();

    /**
     * Returns the panel index.
     * <br/>
     * This is the offset of the panel relative to the other panels, visible or not.
     *
     * @return the panel index
     */
    int getIndex();

    /**
     * Sets the panel index.
     *
     * @param index the index
     */
    void setIndex(int index);

    /**
     * Returns the panel user interface.
     * <br/>
     * The view will be created if it doesn't exist.
     * <br/>
     * If the panel has a {@link DataValidator} specified, this will be constructed, with both the panel and view
     * supplied for injection into it's constructor.
     *
     * @return the panel user interface
     */
    T getView();

    /**
     * Sets the visibility of the panel.
     *
     * @param visible if {@code true} the panel is visible, otherwise it is hidden
     */
    void setVisible(boolean visible);

    /**
     * Determines the visibility of the panel.
     *
     * @return {@code true} if the panel is visible, {@code false} if it is hidden
     */
    boolean isVisible();

    /**
     * Determines if the panel is valid.
     * <p/>
     * This:
     * <ol>
     * <li>Refreshes variables</li>
     * <li>Executes any pre-validation panel actions</li>
     * <li>Validates any {@link DynamicInstallerRequirementValidator}s returned by
     * {@link InstallData#getDynamicInstallerRequirements()}</li>
     * <li>Validates any {@link DataValidator} associated with the panel</li>
     * <li>Executes any post-validation panel actions</li>
     * </ol>
     *
     * @return {@code true} if the panel is valid, otherwise {@code false}
     */
    boolean isValid();

    /**
     * Save the contents of the panel into install data.
     */
    void saveData();

    /**
     * Determines if the panel can be shown.
     *
     * @return {@code true} if the panel can be shown
     */
    boolean canShow();

    /**
     * Creates an installation record for unattended installations and adds it to a XML root element.
     *
     * @param rootElement the root to add child elements to
     */
    void createInstallationRecord(IXMLElement rootElement);

}