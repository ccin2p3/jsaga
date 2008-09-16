package org.ogf.saga.context;

import org.ogf.saga.AbstractTest;

import javax.swing.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextInitTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextInitTest extends AbstractTest {
    private String m_contextId;

    public ContextInitTest(String contextId) throws Exception {
        super();
        m_contextId = contextId;
    }

    public void test_init() throws Exception {
        // create context
        Context context = ContextFactory.createContext();
        context.setAttribute(Context.TYPE, m_contextId);

        // test-only passwords can be retrieved from test properties
        String userpass = super.getOptionalProperty(m_contextId, Context.USERPASS);
        if (userpass == null) {
            // prompt for UserPass
            userpass = JOptionPane.showInputDialog("Please enter UserPass (WARNING: clear text!)");
            if (userpass==null || userpass.trim().length()==0) {
                fail("Test aborted by tester");
            }
        }

        // set attribute UserPass
        context.setAttribute(Context.USERPASS, userpass);

        // initialize context
        context.setDefaults();

        // throw exception if context is still not initialized
        context.getAttribute(Context.USERID);
    }
}