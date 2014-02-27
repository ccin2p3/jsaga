package org.ogf.saga.namespace;

import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.namespace.abstracts.AbstractData;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   NSSetUpTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   30 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class SetUpTest extends AbstractData {
    protected SetUpTest(String protocol) throws Exception {
        super(protocol);
    }

    @Before
    public void setUp() {
        // ignore
    }

    @Test
    public void test_setUp() throws Exception {
        super.setUp();
    }
}
