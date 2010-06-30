package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import org.apache.log4j.Logger;
import org.globus.io.gass.server.JobOutputListener;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobMonitorListener
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   23 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LCGCEJobMonitorListener implements JobOutputListener {
    private static Logger s_logger = Logger.getLogger(LCGCEJobMonitorListener.class);

    private JobStatusNotifier m_notifier;

    public LCGCEJobMonitorListener(JobStatusNotifier notifier) {
        m_notifier = notifier;
    }

    public void outputChanged(String s) {
        BufferedReader reader = new BufferedReader(new StringReader(s));
        try {
            String timestamps = reader.readLine();
            String line;
            while ( !"GRIDMONEOF".equals(line=reader.readLine()) ) {
                String array[] = line.split(" +");
                if (array.length == 2) {
                    String nativeJobId = array[0];
                    Integer stateCode = new Integer(array[1]);
                    String stateString = array[1];
                    JobStatus status = new GatekeeperJobStatus(nativeJobId, stateCode, stateString);
                    m_notifier.notifyChange(status);
                } else {
                    s_logger.warn("Syntax error in grid-monitor output: "+line);
                }
            }
            reader.close();
        } catch (IOException e) {
            s_logger.warn("Failed to read grid-monitor output");
        }
    }

    public void outputClosed() {
        // do nothing
    }
}
