package fr.in2p3.jsaga.impl.job.staging;

import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingDescription
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   26 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingDescription {
    private DataStagingList m_stagingList;
    private String m_executable;
    private String[] m_arguments;
    private StringBuffer m_redirections;

    public DataStagingDescription(JobDescription jobDesc, String[] supportedProtocolsArray) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        // get supported protocols
        Set<String> supportedProtocols = new HashSet<String>();
        for (int i=0; supportedProtocolsArray!=null && i<supportedProtocolsArray.length; i++) {
            supportedProtocols.add(supportedProtocolsArray[i]);
        }

        // init
        List<String> pluginStagingList = new ArrayList<String>();
        m_stagingList = new DataStagingList();
        try {
            // get fileTransfer
            String[] fileTransfer = jobDesc.getVectorAttribute(JobDescription.FILETRANSFER);

            // split fileTransfer into pluginStagingList and stagingList
            for (String ft : fileTransfer) {
                AbstractDataStaging dataStaging = DataStagingFactory.create(ft);
                if (supportedProtocols.contains(dataStaging.getLocalProtocol())) {
                    pluginStagingList.add(ft);
                } else {
                    m_stagingList.add(dataStaging);
                }
            }

            // modify job description
            if (pluginStagingList.size() == 0) {
                jobDesc.removeAttribute(JobDescription.FILETRANSFER);
            } else if (pluginStagingList.size() < fileTransfer.length) {
                String[] pluginStagingArray = pluginStagingList.toArray(new String[pluginStagingList.size()]);
                jobDesc.setVectorAttribute(JobDescription.FILETRANSFER, pluginStagingArray);
            }
        } catch (DoesNotExistException e) {
            // ignore
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    public JobDescription modifyJobDescription(JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            m_redirections = new StringBuffer();
            if (m_stagingList.needsStdin()) {
                // check
                if (hasAttribute(jobDesc, JobDescription.INTERACTIVE) && "true".equalsIgnoreCase(jobDesc.getAttribute(JobDescription.INTERACTIVE))) {
                    throw new BadParameterException("Option "+JobDescription.FILETRANSFER+" can not be used with option "+JobDescription.INTERACTIVE);
                }

                // clone jobDesc and modify clone
                JobDescription newJobDesc = (JobDescription) jobDesc.clone();
                newJobDesc.setAttribute(JobDescription.INTERACTIVE, "true");

                m_executable = jobDesc.getAttribute(JobDescription.EXECUTABLE);
                newJobDesc.setAttribute(JobDescription.EXECUTABLE, "/bin/sh");
                try {
                    m_arguments = jobDesc.getVectorAttribute(JobDescription.ARGUMENTS);
                    newJobDesc.removeAttribute(JobDescription.ARGUMENTS);
                } catch (DoesNotExistException e) {
                    m_arguments = null;
                }
                try {
                    String input = jobDesc.getAttribute(JobDescription.INPUT);
                    newJobDesc.removeAttribute(JobDescription.INPUT);
                    m_redirections.append(" <");
                    m_redirections.append(input);
                } catch (DoesNotExistException e) {
                    // ignore
                }
                try {
                    String output = jobDesc.getAttribute(JobDescription.OUTPUT);
                    newJobDesc.removeAttribute(JobDescription.OUTPUT);
                    m_redirections.append(" >");
                    m_redirections.append(output);
                } catch (DoesNotExistException e) {
                    // ignore
                }
                try {
                    String error = jobDesc.getAttribute(JobDescription.ERROR);
                    newJobDesc.removeAttribute(JobDescription.ERROR);
                    m_redirections.append(" 2>");
                    m_redirections.append(error);
                } catch (DoesNotExistException e) {
                    // ignore
                }

                // uncomment this to enable debugging job wrapper script
//                newJobDesc.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/cat");
//                newJobDesc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"> /tmp/job-wrapper.sh"});

                // returns
                return newJobDesc;
            } else {
                return jobDesc;
            }
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (CloneNotSupportedException e) {
            throw new NoSuccessException(e);
        }
    }

    public void preStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_stagingList.preStaging(job, m_executable, m_arguments, m_redirections.toString());
    }

    public void postStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_stagingList.postStaging(job);
    }

    public void cleanup(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_stagingList.cleanup(job);
    }

    private static boolean hasAttribute(JobDescription jobDesc, String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            jobDesc.getAttribute(key);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }
}
