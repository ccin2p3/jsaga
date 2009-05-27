package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;

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

    public DataStagingDescription(JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            String[] fileTransfer = jobDesc.getVectorAttribute(JobDescription.FILETRANSFER);
            m_stagingList = new DataStagingList(fileTransfer);

            if (m_stagingList.needsStdin()) {
                // check
                if (hasAttribute(jobDesc, JobDescription.INTERACTIVE) && "true".equalsIgnoreCase(jobDesc.getAttribute(JobDescription.INTERACTIVE))) {
                    throw new BadParameterException("Option "+JobDescription.FILETRANSFER+" can not be used with option "+JobDescription.INTERACTIVE);
                }
                if (hasAttribute(jobDesc, JobDescription.INPUT)) {
                    throw new BadParameterException("Option "+JobDescription.FILETRANSFER+" can not be used with option "+JobDescription.INPUT);
                }
                if (m_stagingList.needsStdout()) {
                    if (hasAttribute(jobDesc, JobDescription.OUTPUT)) {
                        throw new BadParameterException("Option "+JobDescription.FILETRANSFER+" can not be used with option "+JobDescription.OUTPUT);
                    }
                    if (hasAttribute(jobDesc, JobDescription.ERROR)) {
                        throw new BadParameterException("Option "+JobDescription.FILETRANSFER+" can not be used with option "+JobDescription.ERROR);
                    }
                }

                // update jobDesc
                m_executable = jobDesc.getAttribute(JobDescription.EXECUTABLE);
                jobDesc.setAttribute(JobDescription.EXECUTABLE, "/bin/sh");
                m_arguments = jobDesc.getVectorAttribute(JobDescription.ARGUMENTS);
                jobDesc.removeAttribute(JobDescription.ARGUMENTS);
                jobDesc.setAttribute(JobDescription.INTERACTIVE, "true");

                // uncomment this to enable debugging job wrapper script
//                jobDesc.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/cat");
//                jobDesc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"> /tmp/job-wrapper.sh"});
            }
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        }
    }

    public void preStaging(Job job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_stagingList.preStaging(job, m_executable, m_arguments);
    }

    public void postStaging(Job job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_stagingList.postStaging(job);
    }

    public void cleanup(Job job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
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
