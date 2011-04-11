package fr.in2p3.jsaga.impl.job.streaming.mgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.job.JobDescription;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManager;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManagerThroughSandboxTwoPhase;
import fr.in2p3.jsaga.impl.job.streaming.LocalFileFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StreamingManagerThroughSandboxTwoPhase
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   7 avril 2011
* ***************************************************
* Description:                                      */

public class StreamingManagerThroughSandboxTwoPhase extends DataStagingManagerThroughSandboxTwoPhase implements StreamingManagerThroughSandbox {

    private String m_uuid;

    public StreamingManagerThroughSandboxTwoPhase(StagingJobAdaptorTwoPhase adaptor,
			String uniqId) throws NotImplementedException,
			BadParameterException, NoSuccessException {
		super(adaptor, uniqId);
        m_uuid = uniqId;
	}

    @Override
	public JobDescription modifyJobDescription(JobDescription jobDesc)
			throws NotImplementedException, AuthenticationFailedException,
			AuthorizationFailedException, PermissionDeniedException,
			BadParameterException, TimeoutException, NoSuccessException {
		// clone jobDesc and modify clone
        try {
			JobDescription newJobDesc = (JobDescription) jobDesc.clone();
			// set Interactive to false
            newJobDesc.setAttribute(JobDescription.INTERACTIVE, JobDescription.FALSE);

            // build FileTransfer
            newJobDesc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{
            		LocalFileFactory.getLocalInputFile(m_uuid) + " > " + getWorker("input"),
            		LocalFileFactory.getLocalOutputFile(m_uuid) + " < " + getWorker("output"),
            		LocalFileFactory.getLocalErrorFile(m_uuid) + " < " + getWorker("error")
            });
            
            // set INPUT OUTPUT and ERROR
            newJobDesc.setAttribute(JobDescription.INPUT, getWorker("input").toString());
            newJobDesc.setAttribute(JobDescription.OUTPUT, getWorker("output").toString());
            newJobDesc.setAttribute(JobDescription.ERROR, getWorker("error").toString());

			return newJobDesc;
		} catch (CloneNotSupportedException e) {
            throw new NoSuccessException(e);
		} catch (Exception e) {
            throw new NoSuccessException(e);
		}
	}

    @Override
    public void cleanup(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
    	try {
    		File input = LocalFileFactory.getLocalInputFile(m_uuid);
    		input.delete();
    	} catch (Exception e) {
    	}
    	try {
    		File output = LocalFileFactory.getLocalOutputFile(m_uuid);
    		output.delete();
    	} catch (Exception e) {
    	}
    	try {
    		File error = LocalFileFactory.getLocalErrorFile(m_uuid);
    		error.delete();
    	} catch (Exception e) {
    	}
    	super.cleanup(job, nativeJobId);
    }	

    protected String getWorker(String suffix) {
        return "worker-"+m_uuid+"."+suffix;
    }


}
