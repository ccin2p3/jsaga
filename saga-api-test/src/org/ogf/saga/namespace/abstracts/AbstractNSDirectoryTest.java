package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceDirectory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryTest extends AbstractNSEntryTest {
    // setup
    protected NamespaceDirectory m_dir;

    public AbstractNSDirectoryTest(String protocol) throws Exception {
        super(protocol);
    }

    protected void setUp() throws Exception {
        super.setUp();
        m_dir = m_root.openDir(m_dirUri, Flags.NONE);
    }

    protected void tearDown() throws Exception {
        if (m_dir != null) {
            m_dir.close();
        }
        super.tearDown();
    }
}
