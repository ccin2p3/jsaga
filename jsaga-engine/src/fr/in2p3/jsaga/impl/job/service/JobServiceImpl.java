package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.helpers.XMLFileParser;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import fr.in2p3.jsaga.impl.job.description.SAGAJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.description.SubJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.JobImpl;
import org.ogf.saga.*;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.lang.Exception;
import java.util.List;

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
    private URL m_resourceManager;
    private JobControlAdaptor m_controlAdaptor;
    private JobMonitorService m_monitorService;

    /** constructor */
    public JobServiceImpl(Session session, URL rm, JobControlAdaptor controlAdaptor, JobMonitorService monitorService) {
        super(session);
        m_resourceManager = rm;
        m_controlAdaptor = controlAdaptor;
        m_monitorService = monitorService;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobServiceImpl clone = (JobServiceImpl) super.clone();
        clone.m_resourceManager = m_resourceManager;
        clone.m_controlAdaptor = m_controlAdaptor;
        clone.m_monitorService = m_monitorService;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.JOBSERVICE;
    }

    public Job createJob(JobDescription jobDesc) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        Document jsdlDOM;
        if (jobDesc instanceof SAGAJobDescriptionImpl) {
            jsdlDOM = ((SAGAJobDescriptionImpl) jobDesc).getJSDL().getOwnerDocument();
        } else if (jobDesc instanceof SubJobDescriptionImpl) {
            throw new NotImplemented("Not implemented yet..."); //todo
        } else {
            throw new NotImplemented("Unsupported JobDescription implementation: "+jobDesc.getClass().getName());
        }

        // translate from JSDL
        String stylesheet = m_controlAdaptor.getTranslator();
        String nativeJobDesc;
        try {
            if (stylesheet != null) {
                XSLTransformer transformer = XSLTransformerFactory.getInstance().getCached(stylesheet, m_controlAdaptor.getTranslatorParameters());
                byte[] bytes = transformer.transform(jsdlDOM);
                nativeJobDesc = new String(bytes);
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                XMLFileParser.dump(jsdlDOM, out);
                nativeJobDesc = out.toString();
            }
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // returns
        return new JobImpl(m_session, jobDesc, nativeJobDesc, m_controlAdaptor, m_monitorService);
    }

    public Job runJob(String commandLine, String host, boolean interactive) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        try {
            // set job description
            JobDescription desc = JobFactory.createJobDescription();
            desc.setAttribute(JobDescription.EXECUTABLE, commandLine);
            desc.setAttribute(JobDescription.INTERACTIVE, ""+interactive);
            desc.setAttribute(JobDescription.CANDIDATEHOSTS, ""+host);

            // set job service
            Session session = SessionFactory.createSession(true);
            URL serviceURL = new URL(host!=null ? host.replaceAll(" ", "%20") : "");
            JobService service = JobFactory.createJobService(session, serviceURL);

            // submit job
            Job job = service.createJob(desc);
            job.run();
            return job;
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }
    public Job runJob(String commandLine, String host) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return this.runJob(commandLine, host, false);
    }
    public Job runJob(String commandLine, boolean interactive) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return this.runJob(commandLine, "", interactive);
    }
    public Job runJob(String commandLine) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return this.runJob(commandLine, "", false);
    }

    public List<String> list() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented yet..."); //todo: implement method list()
    }

    public Job getJob(String nativeJobId) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        return new JobImpl(m_session, nativeJobId, m_controlAdaptor, m_monitorService);
    }

    public JobSelf getSelf() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }
}
