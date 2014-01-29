package org.ogf.saga.context;

import org.junit.Test;
import org.ogf.saga.BaseTest;
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
public abstract class ContextInfoTest extends BaseTest {
    private String m_type = null;
    
    protected ContextInfoTest() throws Exception {
        super();
    }

    protected ContextInfoTest(String contextType) throws Exception {
        super();
        this.m_type = contextType;
    }
    
    @Test
    public void info() throws Exception {
        Session session = SessionFactory.createSession();
        Context[] contexts = session.listContexts();
        for (int i=0; i<contexts.length; i++) {
            if (this.m_type == null || this.m_type.equals((String)contexts[i].getAttribute(Context.TYPE))) {
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
        }
        session.close();
    }
}