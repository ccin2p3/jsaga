package org.ogf.saga.context;

import org.ogf.saga.AbstractTest;
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
public abstract class ContextInfoTest extends AbstractTest {
    protected ContextInfoTest() throws Exception {
        super();
    }

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