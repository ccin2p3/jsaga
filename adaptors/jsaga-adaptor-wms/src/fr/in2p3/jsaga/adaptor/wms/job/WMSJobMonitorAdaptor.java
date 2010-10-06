package fr.in2p3.jsaga.adaptor.wms.job;

import holders.StringArrayHolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobFlags;
import org.glite.wsdl.types.lb.JobFlagsValue;
import org.glite.wsdl.types.lb.QueryAttr;
import org.glite.wsdl.types.lb.QueryConditions;
import org.glite.wsdl.types.lb.QueryOp;
import org.glite.wsdl.types.lb.QueryRecValue;
import org.glite.wsdl.types.lb.QueryRecord;
import org.glite.wsdl.types.lb.StatName;
import org.glite.wsdl.types.lb.StateEnterTimesItem;
import org.glite.wsdl.types.lb.holders.JobStatusArrayHolder;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.HTTPSSender;
import org.globus.gsi.TrustedCertificates;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobMonitorAdaptor
* Authors: Nicolas DEMESY (nicolas.demesy@bt.com)
* 		   Jerome Revillard (jrevillard@maatg.com) - MAAT France
* Date:   18 fev. 2008
* Updated: 8 Mai  2010 (Jerome)
* ***************************************************/

public class WMSJobMonitorAdaptor extends WMSJobAdaptorAbstract implements QueryIndividualJob, ListableJobAdaptor, JobInfoAdaptor {
    protected String m_wmsServerUrl;
    protected String m_lbHost;
    protected int m_lbPort;

	// Should never be invoked 
	public int getDefaultPort() {
		return 9003;
	}

    public String getType() {
        return "wms";
    }

    /** this method is ignored */
    public Usage getUsage() {
        return null;
    }

    /** this method is ignored */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }
  
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_wmsServerUrl = "https://"+host+":"+port+basePath;
        m_lbHost = WMStoLB.getInstance().getLBHost(m_wmsServerUrl);
        // jobIdUrl port can not be used for invoking web service, use default port instead...
        m_lbPort = 9003;
    }

    public void disconnect() throws NoSuccessException {
        m_wmsServerUrl = null;
        m_lbHost = null;
        m_lbPort = -1;
	}
    
    /**
	 * Get one job status
	 */
    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        org.glite.wsdl.types.lb.JobStatus jobInfo = this.getJobInfo(nativeJobId);
        return new WMSJobStatus(nativeJobId, jobInfo);
    }
    
    public Integer getExitCode(String nativeJobId) throws NotImplementedException, NoSuccessException {
        return new Integer(this.getJobInfo(nativeJobId).getExitCode());
    }

    public Date getCreated(String nativeJobId) throws NotImplementedException, NoSuccessException {
        StateEnterTimesItem[] times = this.getJobInfo(nativeJobId).getStateEnterTimes();
        return this.find(times, StatName.SUBMITTED);
    }
    public Date getStarted(String nativeJobId) throws NotImplementedException, NoSuccessException {
        StateEnterTimesItem[] times = this.getJobInfo(nativeJobId).getStateEnterTimes();
        return this.find(times, StatName.RUNNING);
    }
    public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException {
        StateEnterTimesItem[] times = this.getJobInfo(nativeJobId).getStateEnterTimes();
        Date date = this.find(times, StatName.DONE);
        if (date == null) {
            date = this.find(times, StatName.CLEARED);
        }
        if (date == null) {
            date = this.find(times, StatName.ABORTED);
        }
        if (date == null) {
            date = this.find(times, StatName.CANCELLED);
        }
        return date;
    }

    public String[] getExecutionHosts(String nativeJobId) throws NotImplementedException, NoSuccessException {
        return new String[]{this.getJobInfo(nativeJobId).getCeNode()};
    }

    private org.glite.wsdl.types.lb.JobStatus getJobInfo(String nativeJobId) throws NoSuccessException {
    	try {
    		// Store the LB host if not exists. This is the case for Job.getjob(nativejobid)
    		// to monitor a job that was not submitted by JSAGA.
    		if (m_lbHost == null) {
    			WMStoLB.getInstance().setLBHost(m_wmsServerUrl, nativeJobId);
    		}
    		
    		// get stub
	        LoggingAndBookkeepingPortType stub = getLBStub(m_credential);
	        
	        // get job Status
	        JobFlagsValue[] jobFlagsValue = new JobFlagsValue[1];
	        jobFlagsValue[0] = JobFlagsValue.CLASSADS;
	        JobFlags jobFlags = new JobFlags(jobFlagsValue);
	        org.glite.wsdl.types.lb.JobStatus jobInfo = stub.jobStatus(nativeJobId,jobFlags );
	        if(jobInfo == null) {
	            throw new NoSuccessException("Unable to get information about job: "+nativeJobId);
	        }
            return jobInfo;
    	}
    	catch (MalformedURLException e) {
    		throw new NoSuccessException(e);
    	} catch (ServiceException e) {
    		throw new NoSuccessException(e);
		} catch (GenericFault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
    }
    private Date find(StateEnterTimesItem[] times, StatName state) throws NoSuccessException {
        for (int i=0; times!=null && i<times.length; i++) {
            if (times[i].getState().equals(state)) {
                Calendar cal = times[i].getTime();
                if (cal!=null && cal.getTimeInMillis()>0) {
                    return cal.getTime();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

	/**
	 * For ListableJobAdaptor: Problematic when the user has thousands of jobs
	 * registered in the LB. Out of memory exceptions can happened.
	 */
	public String[] list() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            // get stub
            LoggingAndBookkeepingPortType stub = getLBStub(m_credential);

            // get list of jobids
            JobFlagsValue[] jobFlagsValue = new JobFlagsValue[1];
            jobFlagsValue[0] = JobFlagsValue.CLASSADS;
            JobFlags jobFlags = new JobFlags(jobFlagsValue);

            JobStatusArrayHolder jobStatusResult = new JobStatusArrayHolder();
            StringArrayHolder jobNativeIdResult = new StringArrayHolder();

            QueryConditions[] queryConditions = new  QueryConditions[1];
            queryConditions[0] = new QueryConditions();
            queryConditions[0].setAttr(QueryAttr.JOBID);

            QueryRecord[] qR = new QueryRecord[1];
            QueryRecValue value1 = new QueryRecValue();
            value1.setC("https://"+m_lbHost+"/");
            qR[0] = new QueryRecord(QueryOp.UNEQUAL, value1, null );
            queryConditions[0].setRecord(qR);
            // Cannot use stub.userJobs() because not yet implemented (version > 1.8 needed)
	        stub.queryJobs(queryConditions, jobFlags, jobNativeIdResult, jobStatusResult);
            return jobNativeIdResult.value;
        } catch (MalformedURLException e) {
            throw new NoSuccessException(e);
        } catch (ServiceException e) {
            throw new NoSuccessException(e);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
    
	protected LoggingAndBookkeepingPortType getLBStub(GSSCredential m_credential) throws MalformedURLException, ServiceException, NoSuccessException {
        // set LB url
        if (m_lbHost == null) {
            // second chance to get the lbHost
            m_lbHost = WMStoLB.getInstance().getLBHost(m_wmsServerUrl);

            // if still null then fails
            if (m_lbHost == null) {
                throw new NoSuccessException("No LB found for WMS: "+m_wmsServerUrl);
            }
        }
        URL lbURL = new URL("https", m_lbHost, m_lbPort , "/");

		// Set provider
        SimpleProvider provider = new SimpleProvider();
        SimpleTargetedChain c = new SimpleTargetedChain(new HTTPSSender());
        provider.deployTransport("https",c);
        c = new SimpleTargetedChain(new HTTPSSender());
        provider.deployTransport("http",c);
        
        // get LB Stub
        LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocator(provider);
        LoggingAndBookkeepingPortType loggingAndBookkeepingPortType = loc.getLoggingAndBookkeeping(lbURL);
        ((Stub)loggingAndBookkeepingPortType)._setProperty(GSIConstants.GSI_CREDENTIALS, m_credential);
        ((Stub)loggingAndBookkeepingPortType)._setProperty(GSIConstants.GSI_TRANSPORT, GSIConstants.ENCRYPTION);
        ((Stub)loggingAndBookkeepingPortType)._setProperty(GSIConstants.TRUSTED_CERTIFICATES, TrustedCertificates.load(m_certRepository.getAbsolutePath()));
        ((Stub)loggingAndBookkeepingPortType).setTimeout(120 * 1000); //2 mins
        return loggingAndBookkeepingPortType;
	}
}
