package fr.in2p3.jsaga.adaptor.job;

import org.apache.log4j.Logger;
import org.globus.gram.*;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.NoSuccessException;

import java.util.Timer;
import java.util.TimerTask;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobMonitorWatchdog
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   24 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 * todo: notify several JobService instances connected to the same LCG-CE with a single grid-monitor ?
 * -> if several JobService instances in the same JVM ?     (yes: submit job with 'ps | grep dest-url')
 * -> if several JobService instances in separate JVMs ?    (no)
 * -> if several JobService instances on separate hosts ?   (no)
 */
public class LCGCEJobMonitorWatchdog extends TimerTask {
    private static final String RSL = "&(executable = /opt/globus/libexec/grid_monitor_lite.sh)" +
            "(arguments = '--dest-url=https://134.158.71.194:9000/dev/stdout')";
    private static final int WATCHDOG_PERIOD = 5*60*1000;

    private static Logger s_logger = Logger.getLogger(LCGCEJobMonitorListener.class);

    private GramJob m_gridMonitorJob;
    private String m_serverUrl;
    private Timer m_timer;

    public LCGCEJobMonitorWatchdog(GSSCredential cred, String host, int port) throws NoSuccessException {
        m_gridMonitorJob = new GramJob(cred, RSL);
        m_serverUrl = host+":"+port+"/jobmanager-fork";

        // start grid monitor
        this.startMonitor();

        // start watchdog
        m_timer = new Timer();
        m_timer.schedule(this, WATCHDOG_PERIOD, WATCHDOG_PERIOD);
    }

    public void stopAll() throws NoSuccessException {
        // stop watchdog
        m_timer.cancel();

        // stop grid monitor
        this.stopMonitor();
    }

    public void run() {
        try {
            if (this.isStopped()) {
                this.startMonitor();
            } else {
                s_logger.info("Grid monitor is alived: "+m_gridMonitorJob.getIDAsString());
            }
        } catch (NoSuccessException e) {
            s_logger.warn("Failed to start grid monitor", e);
        }
    }

    private void startMonitor() throws NoSuccessException {
        try {
            Gram.request(m_serverUrl, m_gridMonitorJob, false);
            s_logger.info("Started grid monitor: "+m_gridMonitorJob.getIDAsString());
        } catch (GramException e) {
            throw new NoSuccessException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
    }

    private void stopMonitor() throws NoSuccessException {
        try {
            Gram.cancel(m_gridMonitorJob);
            s_logger.info("Stopped grid monitor: "+m_gridMonitorJob.getIDAsString());
        } catch (GramException e) {
            throw new NoSuccessException(e);
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
    }

    private boolean isStopped() throws NoSuccessException {
        try {
            Gram.jobStatus(m_gridMonitorJob);
        } catch (GramException e) {
            if (e.getErrorCode() == GramException.ERROR_CONTACTING_JOB_MANAGER) {
                return true;    // job manager is stopped when status is DONE
            } else {
                throw new NoSuccessException(e);
            }
        } catch (GSSException e) {
            throw new NoSuccessException(e);
        }
        switch (m_gridMonitorJob.getStatus()) {
            case GramJob.STATUS_DONE:
            case GramJob.STATUS_FAILED:
                return true;
            default:
                return false;
        }
    }
}
