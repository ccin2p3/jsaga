package org.ogf.saga.context;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import javax.swing.*;
import java.awt.*;

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
public abstract class ContextInitTest extends AbstractTest {
    private String m_contextId;
    private boolean m_hasUserPass;

    protected ContextInitTest(String contextId) throws Exception {
        this(contextId, true);
    }

    protected ContextInitTest(String contextId, boolean hasUserPass) throws Exception {
        super();
        m_contextId = contextId;
        m_hasUserPass = hasUserPass;
    }

    /**
     * Override this method to add context-specific attributes at run-time.
     */
    protected void updateContextAttributes(Context context) throws Exception {
        // do nothing
    }

    public void test_init() throws Exception {
    	// create empty session
    	Session session = SessionFactory.createSession(false);
        // create context
        Context context = ContextFactory.createContext();
        context.setAttribute(Context.TYPE, m_contextId);

        // set attribute UserPass
        if (m_hasUserPass) {
            // test-only passwords can be retrieved from test properties
            String userpass = super.getOptionalProperty(m_contextId, Context.USERPASS);
            if (userpass == null) {
                // prompt for UserPass
                final String prompt = "Please enter UserPass (WARNING: clear text!)";
                try {
                    userpass = JOptionPane.showInputDialog(prompt);
                } catch (HeadlessException e) {
                    userpass = System.getProperty("userpass");
                    if (userpass == null) {
                        throw e;
                    }
                }
                if (userpass==null || userpass.trim().length()==0) {
                    fail("Test aborted by tester");
                }
            }
            context.setAttribute(Context.USERPASS, userpass);
        }

        // set context-specific attributes
        this.updateContextAttributes(context);

        // init context
        session.addContext(context);
    }
}