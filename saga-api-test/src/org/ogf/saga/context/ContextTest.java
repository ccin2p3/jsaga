package org.ogf.saga.context;

import org.junit.Test;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.BaseTest;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextInitTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Author: lionel.schwarz@in2p3.fr
* Date:   7 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class ContextTest extends BaseTest {
    private String m_contextId;
    private boolean m_hasUserPass;

    protected ContextTest(String contextId) throws Exception {
        this(contextId, true);
    }

    protected ContextTest(String contextId, boolean hasUserPass) throws Exception {
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

    @Test
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

    @Test
    public void test_destroy() throws Exception {
        // create context
        Context context = ContextFactory.createContext();
        context.setAttribute(Context.TYPE, m_contextId);

        // destroy context
        destroy(context);
    }

    private static void destroy(Context context) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = context.getClass().getMethod("destroy");
        if (m != null) {
            m.invoke(context);
        }
    }
    
    @Test
    public void test_info() throws Exception {
        Session session = SessionFactory.createSession();
        Context[] contexts = session.listContexts();
        for (int i=0; i<contexts.length; i++) {
            Context context = contexts[i];

            // print title
            System.out.println("Security context: "+context.getAttribute(Context.TYPE));

            // trigger initialization of context
            try {
            	session.addContext(context);
            } catch(Exception e) {
                System.out.println("  Context not initialized ["+e.getMessage()+"]");
            }

            // print context
            System.out.println(context);
        }
        session.close();
    }
    
}