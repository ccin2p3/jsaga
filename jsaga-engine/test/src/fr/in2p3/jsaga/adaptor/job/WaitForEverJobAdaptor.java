package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.*;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverJobAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WaitForEverJobAdaptor extends WaitForEverAdaptorAbstract
        implements JobControlAdaptor, QueryIndividualJob, ListableJobAdaptor, StreamableJobInteractiveSet,
        SuspendableJobAdaptor, CheckpointableJobAdaptor, SignalableJobAdaptor
{
    public String getType() {
        return "waitforever";
    }

    /////////////////////////////////////////// interface JobAdaptor ///////////////////////////////////////////

    public String getTranslator() {
        return "xsl/job/hang.xsl";
    }
    public Map getTranslatorParameters() {
        return null;
    }
    public int getDefaultPort() {
        return 0;
    }
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return this;
    }

    //////////////////////////////////////// interface Streamable* ////////////////////////////////////////

    public String submitInteractive(String jobDesc, boolean checkMatch, InputStream stdin, OutputStream stdout, OutputStream stderr) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        mayHang(jobDesc);
        return "myjobid";
    }

    //////////////////////////////////////// interface JobControlAdaptor ////////////////////////////////////////

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        mayHang(jobDesc);
        return "myjobid";
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
    }

    //////////////////////////////////////// interface QueryIndividualJob ////////////////////////////////////////

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        hang();
        return new JobStatus(nativeJobId, null, null){
            public String getModel() {return "hang";}
            public SubState getSubState() {return SubState.RUNNING_ACTIVE;}
        };
    }

    //////////////////////////////////////// interface ListableJobAdaptor ////////////////////////////////////////

    public String[] list() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
        return new String[0];
    }

    ////////////////////////////////////// interface SuspendableJobAdaptor //////////////////////////////////////

    public boolean suspend(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
        return true;
    }

    public boolean resume(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
        return true;
    }

    ///////////////////////////////////// interface CheckpointableJobAdaptor /////////////////////////////////////

    public boolean checkpoint(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
        return true;
    }

    /////////////////////////////////////// interface SignalableJobAdaptor ///////////////////////////////////////

    public boolean signal(String nativeJobId, int signum) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
        return true;
    }
}
