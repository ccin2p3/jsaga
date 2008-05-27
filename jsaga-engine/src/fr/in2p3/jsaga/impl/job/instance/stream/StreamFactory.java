package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.impl.job.instance.JobImpl;
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
    public static Stdin createStdin(JobImpl job, JobControlAdaptor adaptor) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        if (adaptor instanceof StreamableJobInteractiveSet) {
            return new PostconnectedStdinOutputStream(job);
        } else {
            return new JobStdinOutputStream(job);
        }
    }

    public static Stdout createStdout(JobImpl job, JobControlAdaptor adaptor, JobIOHandler ioHandler) throws NotImplemented, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        if (adaptor instanceof StreamableJobInteractiveGet) {
            return new GetterInputStream(((JobIOGetterInteractive)ioHandler).getStdout());
        } else if (adaptor instanceof StreamableJobInteractiveSet) {
            return new PreconnectedStdoutInputStream(job);
        } else if (adaptor instanceof StreamableJobBatch) {
            return new JobStdoutInputStream(job, ioHandler);
        } else {
            throw new NotImplemented("Unsupported streamable interface: "+adaptor.getClass().getName());
        }
    }

    public static Stdout createStderr(JobImpl job, JobControlAdaptor adaptor, JobIOHandler ioHandler) throws NotImplemented, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        if (adaptor instanceof StreamableJobInteractiveGet) {
            return new GetterInputStream(((JobIOGetterInteractive)ioHandler).getStderr());
        } else if (adaptor instanceof StreamableJobInteractiveSet) {
            return new PreconnectedStderrInputStream(job);
        } else if (adaptor instanceof StreamableJobBatch) {
            return new JobStderrInputStream(job, ioHandler);
        } else {
            throw new NotImplemented("Unsupported streamable interface: "+adaptor.getClass().getName());
        }
    }
}
