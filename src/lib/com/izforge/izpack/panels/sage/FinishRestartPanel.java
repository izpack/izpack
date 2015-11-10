package com.izforge.izpack.panels.sage;
/**
 * @author tchambard
 */
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.panels.FinishPanel;

public class FinishRestartPanel extends FinishPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 276505658274314843L;

	public FinishRestartPanel(InstallerFrame parent, InstallData idata) {
		super(parent, idata);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void panelActivate() {
		parent.setQuitButtonRestartEnabled();
		super.panelActivate();
		
	}
}