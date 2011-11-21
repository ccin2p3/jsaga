package fr.in2p3.jsaga.adaptor.job;

import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SSHDataAdaptorTest
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   Nov 2011
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SSHAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "ssh2",
                new fr.in2p3.jsaga.adaptor.ssh2.job.SSHJobControlAdaptor().getType());
        assertEquals(
                "sftp2",
                new fr.in2p3.jsaga.adaptor.ssh2.data.SFTPDataAdaptor().getType());
    }
}
