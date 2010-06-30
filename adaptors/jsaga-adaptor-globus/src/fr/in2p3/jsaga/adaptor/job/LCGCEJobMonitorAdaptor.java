package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import fr.in2p3.jsaga.adaptor.job.monitor.ListenFilteredJob;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputStream;
import org.ogf.saga.error.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobMonitorAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 * fixme: tell globus-gma to monitor our jobs!
 * fixme: grid-monitor is not stopped when jsaga-job-status fails
 */
public class LCGCEJobMonitorAdaptor extends GatekeeperJobAdaptorAbstract implements ListenFilteredJob {
    private static final String GASS_PATH = "out";

    private GassServer m_gass;
    private LCGCEJobMonitorWatchdog m_watchdog;

    public String getType() {
        return "lcgce";
    }

    /** override super.getUsage() */
    public Usage getUsage() {
    	return new UAnd(new Usage[] {
        		new UOptional(IP_ADDRESS),
        		new UOptional(TCP_PORT_RANGE)
        });
    }

    /** override super.getDefaults() */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        String defaultIp;
    	try {
            defaultIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			defaultIp = null;
		}
        return new Default[]{
                new Default(IP_ADDRESS, defaultIp),
                new Default(TCP_PORT_RANGE, "40000,45000")
        };
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);

        // start grid-monitor
        m_watchdog = new LCGCEJobMonitorWatchdog(m_credential, host, port);
    }

    public void disconnect() throws NoSuccessException {
        super.disconnect();

        // stop grid-monitor
        m_watchdog.stopAll();
    }

    public void subscribeFilteredJob(JobStatusNotifier notifier) throws TimeoutException, NoSuccessException {
        try {
            m_gass = new GassServer(m_credential, 0);
        } catch (Exception e) {
            throw new NoSuccessException("Failed to create Gass Server", e);
        }
        m_gass.registerDefaultDeactivator();
        m_gass.registerJobOutputStream(GASS_PATH, new JobOutputStream(new LCGCEJobMonitorListener(notifier)));
    }

    public void unsubscribeFilteredJob() throws TimeoutException, NoSuccessException {
        m_gass.unregisterJobOutputStream(GASS_PATH);
        m_gass.unregisterDefaultDeactivator();
        m_gass = null;
    }
}
