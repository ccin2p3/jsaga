package org.ogf.saga.namespace.base;

import org.junit.Test;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WriteBaseTest
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class WriteBaseTest extends DataBaseTest {
    public WriteBaseTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test(expected = DoesNotExistException.class)
    public void test_remove() throws Exception {
        m_file.remove(Flags.NONE.getValue());
        NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
    }
}
