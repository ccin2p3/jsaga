package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobServiceImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobServiceImpl extends AbstractAsyncJobServiceImpl implements JobService {
    /** constructor */
    public JobServiceImpl(Session session, URL rm, JobControlAdaptor controlAdaptor, JobMonitorService monitorService, JobDescriptionTranslator translator) {
        super(session, rm, controlAdaptor, monitorService, translator);
    }

    ///////////////////////////////////////// interface JobService /////////////////////////////////////////

    public synchronized Job createJob(JobDescription jd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        // can not hang...
        return super.createJobSync(jd);
    }

    public Job runJob(String commandLine, String host, boolean interactive) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("runJob");
        if (timeout == WAIT_FOREVER) {
            return super.runJobSync(commandLine, host, interactive);
        } else {
            throw new NotImplementedException("Instead you should set timeout for: "+Job.class+"#run");
        }
    }
    public Job runJob(String commandLine, String host) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJob(commandLine, host, DEFAULT_INTERACTIVE);
    }
    public Job runJob(String commandLine, boolean interactive) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJob(commandLine, DEFAULT_HOST, interactive);
    }
    public Job runJob(String commandLine) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJob(commandLine, DEFAULT_HOST, DEFAULT_INTERACTIVE);
    }

    public List<String> list() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("list");
        if (timeout == WAIT_FOREVER) {
            return super.listSync();
        } else {
            try {
                return (List<String>) getResult(super.list(TaskMode.ASYNC), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        }
    }

    public Job getJob(String jobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // can not hang...
        return super.getJobSync(jobId);
    }

    public JobSelf getSelf() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    public synchronized void resetAdaptors(SecurityAdaptor security, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_monitorService.startReset();
        try {
            // reset control adaptor
            JobAdaptorFactory.disconnect(m_controlAdaptor);
            JobAdaptorFactory.connect(m_controlAdaptor, security, m_resourceManager, attributes);

            // reset monitor adaptor
            JobMonitorAdaptor monitorAdaptor = m_monitorService.getAdaptor();
            URL monitorURL = m_monitorService.getURL();
            Map monitorAttributes = m_monitorService.getAttributes();
            JobMonitorAdaptorFactory.disconnect(monitorAdaptor);
            JobMonitorAdaptorFactory.connect(monitorAdaptor, security, monitorURL, monitorAttributes);
        } finally {
            m_monitorService.stopReset();
        }
    }

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(JobService.class, methodName, m_resourceManager.getScheme());
    }
}
