package fr.in2p3.jsaga.adaptor.cream;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamConfigurationContext
* Author: lionel.schwarz@in2p3.fr
* Date:   25 feb 2014
* ***************************************************
* Description:                                      */

public class CreamConfigurationContext {

    private static ConfigurationContext m_cc;
    
    private CreamConfigurationContext() throws AxisFault {
        m_cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
    }

    public static ConfigurationContext getInstance() throws AxisFault {
        if (m_cc == null) {
            new CreamConfigurationContext();
        }
        return m_cc;
    }
}
