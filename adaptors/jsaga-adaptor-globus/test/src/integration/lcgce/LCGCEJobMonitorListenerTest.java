package integration.lcgce;

import fr.in2p3.jsaga.adaptor.job.LCGCEJobMonitorListener;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import org.globus.common.CoGProperties;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputStream;
import org.junit.Test;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobMonitorListenerTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   25 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 * /opt/globus/bin/globus-url-copy
 *  file:///opt/globus/tmp/gma_state/grid_manager_monitor_agent_log.30604
 *  https://134.158.71.194:9000/dev/stdout
 */
public class LCGCEJobMonitorListenerTest implements JobStatusNotifier {
    public static void main(String[] args) throws Exception {
        // set parameters
        CoGProperties loadedCogProperties= CoGProperties.getDefault();
        loadedCogProperties.setProperty("tcp.port.range", "9000,9001");
        CoGProperties.setDefault(loadedCogProperties);

        // start gass server
        GassServer gassServer;
        try {
            gassServer = new GassServer(Proxy.get(), 0);
            gassServer.registerDefaultDeactivator();
        } catch (Exception e) {
            throw new Exception("Problems while creating a Gass Server", e);
        }
        JobStatusNotifier notifier = new LCGCEJobMonitorListenerTest();
        gassServer.registerJobOutputStream("out",
                new JobOutputStream(new LCGCEJobMonitorListener(notifier)));
        System.out.println("Started the GASS server: "+gassServer.getURL());
    }

    public void notifyChange(JobStatus status) {
        System.out.println(status.getNativeJobId()+": "+status.getSagaState());
    }
    
    // For JUnit4
    @Test
    public void test_void() throws Exception {
    }
    
}
