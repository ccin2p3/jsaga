package org.ogf.saga.context;

import org.ogf.saga.JSAGABaseTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextInitTest
* Author: lionel.schwarz@in2p3.fr
* Date:   12 mars 2014
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class ContextTest extends JSAGABaseTest {
    protected String m_contextId;

    protected ContextTest(String contextId) throws Exception {
        super();
        m_contextId = contextId;
    }

    /**
     * Override this method to add context-specific attributes at run-time.
     */
    protected void updateContextAttributes(Context context) throws Exception {
        // do nothing
    }

}