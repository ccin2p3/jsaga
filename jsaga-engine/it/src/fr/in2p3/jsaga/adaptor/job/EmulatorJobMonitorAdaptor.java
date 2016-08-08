package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.impl.EmulatorJobAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.impl.EmulatorJobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorJobMonitorAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorJobMonitorAdaptor extends EmulatorJobAdaptorAbstract implements QueryIndividualJob {
    public int getDefaultPort() {return 5678;}

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        File job = super.getJob(nativeJobId);
        if (job.exists()) {
            long endTime;
            try {
                // read file
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(job)));
                String line = in.readLine();
                in.close();

                // get end time
                endTime = Long.parseLong(line);
            } catch (Exception e) {
                SubState status = SubState.FAILED_ERROR;
                return new EmulatorJobStatus(nativeJobId, status, e);
            }
            SubState status;
            if (endTime == 0L) {
                status = SubState.CANCELED;
            } else if (System.currentTimeMillis() > endTime) {
                status = SubState.DONE;
            } else {
                status = SubState.RUNNING_ACTIVE;
            }
            return new EmulatorJobStatus(nativeJobId, status);
        } else {
            throw new NoSuccessException("Job does not exist: "+nativeJobId);
        }
    }
}
