package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.StagingJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobAdaptor;
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
    public static DataStagingManager create(JobControlAdaptor adaptor, JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        // get FileTransfer
        String[] fileTransfer;
        try {
            fileTransfer = jobDesc.getVectorAttribute(JobDescription.FILETRANSFER);
        } catch (DoesNotExistException e) {
            fileTransfer = new String[]{};
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }

        // create data staging manager
        if (adaptor instanceof StagingJobAdaptor) {
            return new DataStagingManagerDelegated(fileTransfer, (StagingJobAdaptor) adaptor);
        } else if (adaptor instanceof StreamableJobAdaptor) {
            return new DataStagingManagerThroughStream(fileTransfer);
        } else {
            throw new NotImplementedException("Adaptor can not handle attribute FileTransfer: "+adaptor.getClass());
        }
    }
}
