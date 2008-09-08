package fr.in2p3.izpack.panels;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   OptionalLicencePanel
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   5 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class OptionalLicencePanel extends LicencePanel {
    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public OptionalLicencePanel(InstallerFrame parent, InstallData idata) {
        super(parent, idata);
        add(LabelFactory.create(
                parent.langpack.getString("OptionalLicencePanel.rollback"),
                parent.icons.getImageIcon("info"),
                LEADING), NEXT_LINE);
    }
}
