package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UnmonitoredJobMonitorAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   24 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class UnmonitoredJobMonitorAdaptor implements QueryIndividualJob {
    private boolean m_cancelled;

    public UnmonitoredJobMonitorAdaptor() {
        m_cancelled = false;
    }

    void cancel() {
        m_cancelled = true;
    }

    public String getType() {
        return "unmonitored";
    }

    public Usage getUsage() {
        return null;
    }
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }
    public int getDefaultPort() {
        return 0;
    }
    public Class[] getSupportedSecurityCredentialClasses() {
        return null;
    }
    public void setSecurityCredential(SecurityCredential credential) {
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
    }
    public void disconnect() throws NoSuccessException {
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        return new JobStatus(nativeJobId, null, null){
            public String getModel() {return "unmonitored";}
            public SubState getSubState() {
                if (m_cancelled) {
                    return SubState.CANCELED;
                } else {
                    return SubState.RUNNING_ACTIVE;
                }
            }
        };
    }
}
