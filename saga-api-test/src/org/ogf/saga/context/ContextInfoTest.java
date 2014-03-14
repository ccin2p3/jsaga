package org.ogf.saga.context;

import org.junit.Test;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextInfoTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class ContextInfoTest extends ContextTest {
    private static String ALL_CONTEXTS = "all";
    
    protected ContextInfoTest() throws Exception {
        super(ALL_CONTEXTS);
    }

    protected ContextInfoTest(String contextId) throws Exception {
        super(contextId);
    }
    
    @Test
    public void info() throws Exception {
        
        // Keep old mechanism for printing all contexts in default session
        if (this.m_contextId.equals(ALL_CONTEXTS)) {
            Session session = SessionFactory.createSession();
            Context[] contexts = session.listContexts();
            for (int i=0; i<contexts.length; i++) {
                Context context = contexts[i];
    
                // print title
                System.out.println("Security context: "+context.getAttribute(Context.TYPE));
    
                System.out.println(context);
            }
            session.close();
        } else {
            Session session = SessionFactory.createSession(false);
            Context context = ContextFactory.createContext();
            context.setAttribute(Context.TYPE, m_contextId);
        
            // set context-specific attributes
            this.updateContextAttributes(context);
    
            // trigger initialization of context
            try {
                session.addContext(context);
            } catch(Exception e) {
                System.out.println("  Context not initialized ["+e.getMessage()+"]");
            }
        
            // print context
            System.out.println(context);
            session.close();
        }
    }
}