package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.advanced.SandboxJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.SandboxTransfer;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManagerThroughSandbox
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   15 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingManagerThroughSandbox implements DataStagingManager {
    // info
    private Set<String> m_supportedProtocols;
    private URL m_intermediaryURL;
    private Directory m_intermediaryDirectory;

    public DataStagingManagerThroughSandbox(SandboxJobAdaptor adaptor, String uniqId) throws NotImplementedException, BadParameterException, NoSuccessException {
        // get protocols and intermediary
        m_supportedProtocols = new HashSet<String>(Arrays.asList(adaptor.getStagingProtocols()));
        m_intermediaryURL = URLFactory.createURL(adaptor.getStagingIntermediaryBaseURL()+"/"+uniqId+"/");
    }

    public Set<String> getSupportedProtocols() {
        return m_supportedProtocols;
    }

    public URL getIntermediaryURL() {
        return m_intermediaryURL;
    }

    public JobDescription modifyJobDescription(final JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return jobDesc;
    }

    public void preStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // do nothing
    }

    public void preStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // create intermediary directory
        try {
            m_intermediaryDirectory = FileFactory.createDirectory(job.getSession(), m_intermediaryURL, Flags.CREATE.getValue());
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        }

        // for each input file
        for (SandboxTransfer transfer : job.getSandboxJobAdaptor().getInputSandboxTransfer(nativeJobId)) {
            transfer(job.getSession(), transfer);
        }
    }

    public void postStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // for each output file
        for (SandboxTransfer transfer : job.getSandboxJobAdaptor().getOutputSandboxTransfer(nativeJobId)) {
            transfer(job.getSession(), transfer);
        }
    }

    public void cleanup(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // for each input file
        for (SandboxTransfer transfer : job.getSandboxJobAdaptor().getInputSandboxTransfer(nativeJobId)) {
            remove(job.getSession(), transfer.getTo());
        }

        // for each output file
        for (SandboxTransfer transfer : job.getSandboxJobAdaptor().getOutputSandboxTransfer(nativeJobId)) {
            remove(job.getSession(), transfer.getFrom());
        }

        // remove base directory
        if (m_intermediaryDirectory != null) {
            m_intermediaryDirectory.remove(Flags.NONE.getValue());
        }
    }

    private static void transfer(Session session, SandboxTransfer transfer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        int append = (transfer.isAppend() ? Flags.APPEND : Flags.NONE).getValue();
        try {
            URL from = URLFactory.createURL(transfer.getFrom());
            URL to = URLFactory.createURL(transfer.getTo());
            File file = FileFactory.createFile(session, from, Flags.NONE.getValue());
            file.copy(to, append);
            file.close();
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    private static void remove(Session session, String url) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        try {
            URL urlToRemove = URLFactory.createURL(url);
            File file = FileFactory.createFile(session, urlToRemove, Flags.NONE.getValue());
            file.remove();
            file.close();
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }
}
