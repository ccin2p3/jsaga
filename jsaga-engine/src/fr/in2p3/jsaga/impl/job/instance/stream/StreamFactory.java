package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StreamFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 * TODO: not used yet...
 */
public class StreamFactory {
    public static Stdin createStdin(AbstractSyncJobImpl job, JobControlAdaptor adaptor) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (adaptor instanceof StreamableJobInteractiveSet) {
            return new PostconnectedStdinOutputStream(job);
        } else {
            return new JobStdinOutputStream(job);
        }
    }

    public static Stdout createStdout(AbstractSyncJobImpl job, JobControlAdaptor adaptor, JobIOHandler ioHandler) throws NotImplementedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (adaptor instanceof StreamableJobInteractiveGet) {
            return new GetterInputStream(((JobIOGetterInteractive)ioHandler).getStdout());
        } else if (adaptor instanceof StreamableJobInteractiveSet) {
            return new PreconnectedStdoutInputStream(job);
        } else if (adaptor instanceof StreamableJobBatch) {
            return new JobStdoutInputStream(job, ioHandler);
        } else {
            throw new NotImplementedException("Unsupported streamable interface: "+adaptor.getClass().getName());
        }
    }

    public static Stdout createStderr(AbstractSyncJobImpl job, JobControlAdaptor adaptor, JobIOHandler ioHandler) throws NotImplementedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (adaptor instanceof StreamableJobInteractiveGet) {
            return new GetterInputStream(((JobIOGetterInteractive)ioHandler).getStderr());
        } else if (adaptor instanceof StreamableJobInteractiveSet) {
            return new PreconnectedStderrInputStream(job);
        } else if (adaptor instanceof StreamableJobBatch) {
            return new JobStderrInputStream(job, ioHandler);
        } else {
            throw new NotImplementedException("Unsupported streamable interface: "+adaptor.getClass().getName());
        }
    }
}
