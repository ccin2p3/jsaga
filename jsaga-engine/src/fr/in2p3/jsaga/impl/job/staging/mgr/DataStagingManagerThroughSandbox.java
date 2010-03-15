package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.advanced.StagingJobAdaptor;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import fr.in2p3.jsaga.impl.job.staging.*;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.namespace.Flags;
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
    // managed by plugin
    private List<String> m_managedByPlugin;

    // managed by engine
    private List<InputDataStagingToRemote> m_inputToRemote;
    private List<OutputDataStagingFromRemote> m_outputFromRemote;

    // info
    private Set<String> m_supportedProtocols;
    private URL m_intermediaryURL;
    private Directory m_intermediaryDirectory;

    public DataStagingManagerThroughSandbox(String[] fileTransfer, StagingJobAdaptor adaptor, String uniqId) throws NotImplementedException, BadParameterException, NoSuccessException {
        // create
        m_managedByPlugin = new ArrayList<String>();
        m_inputToRemote = new ArrayList<InputDataStagingToRemote>();
        m_outputFromRemote = new ArrayList<OutputDataStagingFromRemote>();

        // get protocols and intermediary
        m_supportedProtocols = new HashSet<String>(Arrays.asList(adaptor.getStagingProtocols()));
        m_intermediaryURL = URLFactory.createURL(adaptor.getStagingIntermediaryBaseURL()+"/"+uniqId+"/");

        // init
        for (String ft : fileTransfer) {
            AbstractDataStaging dataStaging = DataStagingFactory.create(ft, m_intermediaryURL);
            if (m_supportedProtocols.contains(dataStaging.getLocalProtocol())) {
                m_managedByPlugin.add(ft);
            } else {
                if (dataStaging instanceof InputDataStagingToIntermediary) {
                    m_inputToRemote.add((InputDataStagingToRemote) dataStaging);
                    String postStaging = ((InputDataStagingToIntermediary) dataStaging).getFileTransferForPlugin();
                    m_managedByPlugin.add(postStaging);
                } else if (dataStaging instanceof InputDataStagingToRemote) {
                    m_inputToRemote.add((InputDataStagingToRemote) dataStaging);
                } else if (dataStaging instanceof OutputDataStagingFromIntermediary) {
                    m_outputFromRemote.add((OutputDataStagingFromRemote) dataStaging);
                    String preStaging = ((OutputDataStagingFromIntermediary) dataStaging).getFileTransferForPlugin();
                    m_managedByPlugin.add(preStaging);
                } else if (dataStaging instanceof OutputDataStagingFromRemote) {
                    m_outputFromRemote.add((OutputDataStagingFromRemote) dataStaging);
                } else {
                    throw new BadParameterException("[INTERNAL ERROR] Unexpected class: "+dataStaging.getClass());
                }
            }
        }
    }

    public Set<String> getSupportedProtocols() {
        return m_supportedProtocols;
    }

    public URL getIntermediaryURL() {
        return m_intermediaryURL;
    }

    public JobDescription modifyJobDescription(final JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            // clone jobDesc and modify clone
            JobDescription newJobDesc = (JobDescription) jobDesc.clone();

            // replace FileTransfer
            try {
                String[] managedByPlugin = m_managedByPlugin.toArray(new String[m_managedByPlugin.size()]);
                if (managedByPlugin.length > 0) {
                    newJobDesc.setVectorAttribute(JobDescription.FILETRANSFER, managedByPlugin);
                }
            } catch (DoesNotExistException e) {
                // ignore
            }

            // returns
            return newJobDesc;
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (CloneNotSupportedException e) {
            throw new NoSuccessException(e);
        }
    }

    public void preStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // create intermediary directory
        try {
            m_intermediaryDirectory = FileFactory.createDirectory(job.getSession(), m_intermediaryURL, Flags.CREATE.getValue());
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        }

        // for each inputToRemote
        for (InputDataStagingToRemote staging : m_inputToRemote) {
            staging.preStaging(job.getSession());
        }
    }

    public void postStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // for each outputFromRemote
        for (OutputDataStagingFromRemote staging : m_outputFromRemote) {
            staging.postStaging(job.getSession());
        }
    }

    public void cleanup(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // for each inputToRemote
        for (InputDataStagingToRemote staging : m_inputToRemote) {
            staging.cleanup(job.getSession());
        }

        // for each outputFromRemote
        for (OutputDataStagingFromRemote staging : m_outputFromRemote) {
            staging.cleanup(job.getSession());
        }

        // remove base directory
        if (m_intermediaryDirectory != null) {
            m_intermediaryDirectory.remove(Flags.RECURSIVE.getValue());
        }
    }
}
