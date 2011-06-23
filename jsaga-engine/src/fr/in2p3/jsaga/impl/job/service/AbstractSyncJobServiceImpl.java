package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.job.description.AbstractJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.JobImpl;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManager;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManagerFactory;
import fr.in2p3.jsaga.sync.job.SyncJobService;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncJobServiceImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   6 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncJobServiceImpl extends AbstractSagaObjectImpl implements SyncJobService {
    protected static final String DEFAULT_HOST = "";
    protected static final boolean DEFAULT_INTERACTIVE = false;

    public URL m_resourceManager;
    public JobControlAdaptor m_controlAdaptor;
    public JobMonitorService m_monitorService;
    private JobDescriptionTranslator m_translator;

    /** constructor */
    public AbstractSyncJobServiceImpl(Session session, URL rm, JobControlAdaptor controlAdaptor, JobMonitorService monitorService, JobDescriptionTranslator translator) {
        super(session);
        m_resourceManager = rm;
        m_controlAdaptor = controlAdaptor;
        m_monitorService = monitorService;
        m_translator = translator;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSyncJobServiceImpl clone = (AbstractSyncJobServiceImpl) super.clone();
        clone.m_resourceManager = m_resourceManager;
        clone.m_controlAdaptor = m_controlAdaptor;
        clone.m_monitorService = m_monitorService;
        clone.m_translator = m_translator;
        return clone;
    }

    public Job createJobSync(JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        // create uniqId
        String uniqId = ""+System.currentTimeMillis();

        // may modify jobDesc
        DataStagingManager stagingMgr = DataStagingManagerFactory.create(m_controlAdaptor, jobDesc, uniqId);
        jobDesc = stagingMgr.modifyJobDescription(jobDesc);

        // get JSDL
        Document jsdlDOM;
        if (jobDesc instanceof AbstractJobDescriptionImpl) {
            jsdlDOM = ((AbstractJobDescriptionImpl) jobDesc).getJSDL();
        } else {
            throw new NotImplementedException("Unsupported JobDescription implementation: "+jobDesc.getClass().getName());
        }

        // translate from JSDL
        String nativeJobDesc = m_translator.translate(jsdlDOM, uniqId);

        // returns
        return new JobImpl(m_session, nativeJobDesc, jobDesc, stagingMgr, uniqId, this);
    }

    public Job runJobSync(String commandLine, String host, boolean interactive) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            // set job description
            JobDescription desc = JobFactory.createJobDescription(JSAGA_FACTORY);
            desc.setAttribute(JobDescription.EXECUTABLE, commandLine);
            desc.setAttribute(JobDescription.INTERACTIVE, ""+interactive);
            desc.setVectorAttribute(JobDescription.CANDIDATEHOSTS, new String[]{""+host});

            // submit job
            Job job = this.createJobSync(desc);
            job.run();
            return job;
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        }
    }
    public Job runJobSync(String commandLine, String host) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJobSync(commandLine, host, DEFAULT_INTERACTIVE);
    }
    public Job runJobSync(String commandLine, boolean interactive) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJobSync(commandLine, DEFAULT_HOST, interactive);
    }
    public Job runJobSync(String commandLine) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return this.runJobSync(commandLine, DEFAULT_HOST, DEFAULT_INTERACTIVE);
    }

    public List<String> listSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        JobMonitorAdaptor monitorAdaptor = m_monitorService.getAdaptor();
        if (monitorAdaptor instanceof ListableJobAdaptor) {
            String[] array = ((ListableJobAdaptor)monitorAdaptor).list();
            List<String> list = new ArrayList<String>();
            for (int i=0; array!=null && i<array.length; i++) {
                String sagaJobId = "["+m_resourceManager.getString()+"]-["+array[i]+"]";
                list.add(sagaJobId);
            }
            return list;
        } else {
            throw new NotImplementedException("Not implemented yet..."); //todo: implement default behavior for method list()
        }
    }

    public Job getJobSync(String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataStagingManager stagingMgr = DataStagingManagerFactory.create(m_controlAdaptor);
        return new JobImpl(m_session, nativeJobId, stagingMgr, this);
    }

    public JobSelf getSelfSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }
}
