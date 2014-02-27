package org.ogf.saga.namespace.abstracts;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.url.URLFactory;

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
public abstract class AbstractDirectory extends AbstractData {
    // setup
    protected NSDirectory m_subDir;

    public AbstractDirectory(String protocol) throws Exception {
        super(protocol);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        m_subDir = m_dir.openDir(m_subDirUrl, Flags.NONE.getValue());
    }

    @After
    public void tearDown() throws Exception {
        if (m_subDir != null) {
            m_subDir.close();
        }
        super.tearDown();
    }

}
