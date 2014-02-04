package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;

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
@Deprecated
public abstract class AbstractNSDirectoryTest extends AbstractNSEntryTest {
    // setup
    protected NSDirectory m_subDir;

    public AbstractNSDirectoryTest(String protocol) throws Exception {
        super(protocol);
    }

    protected void setUp() throws Exception {
        super.setUp();
        m_subDir = m_dir.openDir(m_subDirUrl, Flags.NONE.getValue());
    }

    protected void tearDown() throws Exception {
        if (m_subDir != null) {
            m_subDir.close();
        }
        super.tearDown();
    }
}
