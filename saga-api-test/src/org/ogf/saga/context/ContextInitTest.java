package org.ogf.saga.context;

import org.ogf.saga.AbstractTest;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
     * Override this method to add context-specific attribtues at run-time.
     */
    protected void updateContextAttributes(Context context) throws Exception {
        // do nothing
    }

    public void test_init() throws Exception {
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
                    System.out.println(prompt);
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    userpass = in.readLine();
                    System.out.println("Continuing...");
                }
                if (userpass==null || userpass.trim().length()==0) {
                    fail("Test aborted by tester");
                }
            }
            context.setAttribute(Context.USERPASS, userpass);
        }

        // set context-specific attributes
        this.updateContextAttributes(context);

        // initialize context
        context.setDefaults();

        // throw exception if context is still not initialized
        context.getAttribute(Context.USERID);
    }
}