package fr.in2p3.jsaga.adaptor.batchssh.job;
import fr.in2p3.jsaga.adaptor.batchssh.job.BatchSSHJobAdaptor;
import org.ogf.saga.error.*;
import org.ogf.saga.url.*; 

import junit.framework.TestCase;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BatchSSHJobAdaptorTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class BatchSSHJobAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "pbs-ssh",
                new BatchSSHJobAdaptor().getType());
    }
}
