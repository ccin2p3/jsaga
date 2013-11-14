package integration.lcgce;

import org.junit.Test;

import fr.in2p3.jsaga.adaptor.job.LCGCEJobMonitorWatchdog;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobMonitorWatchdogTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   25 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LCGCEJobMonitorWatchdogTest {
    public static void main(String[] args) throws Exception {
        new LCGCEJobMonitorWatchdog(Proxy.get(), "cclcgceli01.in2p3.fr", 2119);
    }

    // For JUnit4
    @Test
    public void test_void() throws Exception {
    }
}
