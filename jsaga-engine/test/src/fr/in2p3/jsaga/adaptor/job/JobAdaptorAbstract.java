package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobAdaptorAbstract
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public abstract class JobAdaptorAbstract implements JobControlAdaptor, JobMonitorAdaptor, CleanableJobAdaptor {

    //////////////////////////////////////// interface ClientAdaptor ////////////////////////////////////////

    public Usage getUsage() {
        return null;
    }
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }
    public int getDefaultPort() {
        return NO_PORT;
    }
    public Class[] getSupportedSecurityCredentialClasses() {
        return null;
    }
    public void setSecurityCredential(SecurityCredential credential) {
        // do nothing
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // do nothing
    }
    public void disconnect() throws NoSuccessException {
        // do nothing
    }

    //////////////////////////////////////// interface JobControlAdaptor ////////////////////////////////////////

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorXSLT("xsl/job/hang.xsl");
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        return "myjobid";
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // do nothing
    }

    //////////////////////////////////////// interface JobMonitorAdaptor ////////////////////////////////////////

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return this;
    }

    //////////////////////////////////////// interface CleanableJobAdaptor ////////////////////////////////////////

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // do nothing
    }
}
