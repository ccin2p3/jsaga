package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.impl.job.streaming.mgr.StreamingManagerThroughSandboxOnePhase;
import fr.in2p3.jsaga.impl.job.streaming.mgr.StreamingManagerThroughSandboxTwoPhase;

import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManagerFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingManagerFactory {
    public static DataStagingManager create(JobControlAdaptor adaptor, JobDescription jobDesc, String uniqId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            // get FileTransfer
            String[] fileTransfer = jobDesc.getVectorAttribute(JobDescription.FILETRANSFER);

            // create data staging manager
            if (adaptor instanceof StagingJobAdaptorOnePhase) {
                return new DataStagingManagerThroughSandboxOnePhase((StagingJobAdaptorOnePhase) adaptor, uniqId);
            } else if (adaptor instanceof StagingJobAdaptorTwoPhase) {
           		return new DataStagingManagerThroughSandboxTwoPhase((StagingJobAdaptorTwoPhase) adaptor, uniqId);
            } else if (adaptor instanceof StreamableJobAdaptor) {
                return new DataStagingManagerThroughStream(fileTransfer);
            } else {
                throw new NotImplementedException("Adaptor can not handle attribute FileTransfer: "+adaptor.getClass());
            }
        } catch (DoesNotExistException e) {
        	// If job is interactive and adaptor does not implement streaming, we use the streaming manager
        	try {
				if ("true".equalsIgnoreCase(jobDesc.getAttribute(JobDescription.INTERACTIVE))) {
					if (adaptor instanceof StagingJobAdaptorTwoPhase) {
						return new StreamingManagerThroughSandboxTwoPhase((StagingJobAdaptorTwoPhase) adaptor, uniqId);
					} else if (adaptor instanceof StagingJobAdaptorOnePhase) {
						return new StreamingManagerThroughSandboxOnePhase((StagingJobAdaptorOnePhase) adaptor, uniqId);
					}
				}
			} catch (IncorrectStateException | DoesNotExistException e1) {
			}
            // create data staging manager
            return new DataStagingManagerDummy();
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    public static DataStagingManager create(JobControlAdaptor adaptor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        String uniqId = null;   //todo: remove this

        // create data staging manager
        if (adaptor instanceof StagingJobAdaptorOnePhase) {
            return new DataStagingManagerThroughSandboxOnePhase((StagingJobAdaptorOnePhase) adaptor, uniqId);
        } else if (adaptor instanceof StagingJobAdaptorTwoPhase) {
            return new DataStagingManagerThroughSandboxTwoPhase((StagingJobAdaptorTwoPhase) adaptor, uniqId);
        } else {
            return new DataStagingManagerDummy();
        }
    }
}
