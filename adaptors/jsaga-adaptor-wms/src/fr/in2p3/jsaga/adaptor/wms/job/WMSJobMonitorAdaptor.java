package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import holders.StringArrayHolder;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.glite.lb.LoggingAndBookkeepingLocatorClient;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.*;
import org.glite.wsdl.types.lb.holders.JobStatusArrayHolder;
import org.globus.axis.transport.HTTPSSender;
import org.globus.axis.util.Util;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/

public class WMSJobMonitorAdaptor extends WMSJobAdaptorAbstract implements QueryIndividualJob, QueryFilteredJob, ListableJobAdaptor, JobInfoAdaptor {
    private String m_wmsServerUrl;
    private String m_lbHost;
	private int m_lbPort;

	// Should never be invoked 
	public int getDefaultPort() {
		return 9003;
	}

    public String getType() {
        return "wms";
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{new U(MONITOR_PORT)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(MONITOR_PORT, "9003")};
    }
  
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_wmsServerUrl = "https://"+host+":"+port+basePath;
        m_lbHost = WMStoLB.getInstance().getLBHost(m_wmsServerUrl);
        // jobIdUrl port can not be used for invoking web service, use default port instead...
        m_lbPort = Integer.parseInt((String) attributes.get(MONITOR_PORT));
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
	 * Get all jobs for authenticated user 
	 */
	public JobStatus[] getFilteredStatus(Object[] filters) throws TimeoutException, NoSuccessException {
		try {
			
			// get stub
			LoggingAndBookkeepingPortType stub = getLBStub(m_credential);
			
	        // get Jobs Status
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
	        
	        if(jobNativeIdResult != null && jobNativeIdResult.value != null) {
	        	JobStatus[] filterJobs = new WMSJobStatus[jobNativeIdResult.value.length];
	        	for (int i = 0; i < filterJobs.length; i++) {
                    org.glite.wsdl.types.lb.JobStatus jobInfo = jobStatusResult.value[i];
	        		filterJobs[i] = new WMSJobStatus(jobNativeIdResult.value[i], jobInfo);
				}
		        return filterJobs;
	        }
	        // TODO : exception or null ?
	        return null;
    	} catch (Exception e) {
    		throw new NoSuccessException(e);
    	}
	}

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
        } catch (GenericFault e) {
            throw new NoSuccessException(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e);
        }
    }

	private LoggingAndBookkeepingPortType getLBStub(GSSCredential m_credential) throws MalformedURLException, ServiceException, NoSuccessException {
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
        SimpleTargetedChain c = null;
        c = new SimpleTargetedChain(new HTTPSSender());
        provider.deployTransport("https",c);
        c = new SimpleTargetedChain(new HTTPSender());
        provider.deployTransport("http",c);
        Util.registerTransport();
        
        // get LB Stub
        LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocatorClient(provider, m_credential);
        return loc.getLoggingAndBookkeeping(lbURL);
	}
}
