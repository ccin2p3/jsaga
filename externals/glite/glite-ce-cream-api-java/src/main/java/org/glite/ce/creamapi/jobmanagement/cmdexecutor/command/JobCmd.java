package org.glite.ce.creamapi.jobmanagement.cmdexecutor.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.glite.ce.creamapi.cmdmanagement.Command;
import org.glite.ce.creamapi.cmdmanagement.CommandException;
import org.glite.ce.creamapi.jobmanagement.JobCommandConstant;
import org.glite.ce.creamapi.jobmanagement.JobEnumeration;
import org.glite.ce.creamapi.jobmanagement.JobStatus;

public class JobCmd extends Command {
    public static final String DELEGATION_PROXY_ID = "DELEGATION_PROXY_ID";
    public static final String LEASE_ID = "LEASE_ID";
    public static final String JOB_ID = "JOB_ID";
    public static final String JOB_ID_LIST = "JOB_ID_LIST";
    public static final String JOB_STATUS_LIST = "JOB_STATUS_LIST";
    public static final String TO_DATE = "TO_DATE";
    public static final String FROM_DATE = "FROM_DATE";
    public static final String IS_ADMIN = "IS_ADMIN";
    public static final String USER_DN = "USER_DN";
    public static final String USER_FQAN = "USER_FQAN";
    public static final String USER_FQAN_LIST = "USER_FQAN_LIST";
    public static final String REMOTE_REQUEST_ADDRESS = "REMOTE_REQUEST_ADDRESS";

 
    private int type;

    public JobCmd() {
        super();
        setCategory(JobCommandConstant.JOB_MANAGEMENT);
    }

    public JobCmd(int type) throws CommandException {
        super();
        
        setType(type);
        setCategory("JOB_MANAGEMENT");
    }

    public void addJobId(String  jobId) {
        if(jobId == null) {
            return;
        }
        
        List<String> jobIdList;
        
        if(containsParameterKey(JOB_ID_LIST)) {
            jobIdList = getParameterMultivalue(JOB_ID_LIST);
            jobIdList.add(jobId);
        } else {
            jobIdList = new ArrayList<String>(1);
            jobIdList.add(jobId);

            addParameter(JOB_ID_LIST, jobId);
        }
    }

    public String getDelegationProxyId() {
        return getParameterAsString(DELEGATION_PROXY_ID);
    }
    
    public Calendar getFromDate() {
        if (containsParameterKey(FROM_DATE)) {
            Long timestamp = Long.parseLong(getParameterAsString(FROM_DATE));
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(timestamp);

            return date;
        }
        return null;
    }
    
    public List<JobIdFilterResult> getJobIdFilterResult() {
        return (List<JobIdFilterResult>)getResult().getParameter("JOBID_FILTER_RESULT_LIST");
    }
    
    public List<String> getJobIdFound() {
        JobEnumeration jobEnum = (JobEnumeration)getResult().getParameter("JOB_ENUM");
        if(jobEnum != null) {
            return jobEnum.getJobIdList();
        }
        
        return new ArrayList(0);
    }

    public List<String> getJobIdList() {
        return getParameterMultivalue(JOB_ID_LIST);
    }
    
    public List<String> getJobStatus() {
        return getParameterMultivalue(JOB_STATUS_LIST);
    }

    public int[] getJobStatusAsInt() {
        List<String> statusList = getParameterMultivalue(JOB_STATUS_LIST);
        if(statusList == null) {
            return null;
        }
        
        int[] status = new int[statusList.size()];
        for(int i=0; i<statusList.size(); i++) {
            for(int x=0; x<JobCommandConstant.cmdName.length; x++) {
                if(statusList.get(i).equals(JobCommandConstant.cmdName[x])) {
                    status[i] = x;
                    continue;
                }
            }
        }
        
        return status;
    }

    public String getLeaseId() {
        return getParameterAsString(LEASE_ID);
    }

    public String getRemoteRequestAddress() {
        return getParameterAsString(REMOTE_REQUEST_ADDRESS);
    }
    
    public Calendar getToDate() {
        if (containsParameterKey(TO_DATE)) {
            Long timestamp = Long.parseLong(getParameterAsString(TO_DATE));
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(timestamp);

            return date;
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public String getUserDN() {
        return getParameterAsString(USER_DN);
    }
    
    public String getUserFQAN() {
        return getParameterAsString(USER_FQAN);
    }

    public List<String> getUserFQANList() {
        return getParameterMultivalue(USER_FQAN_LIST);
    }
    
    public boolean isAdmin() {
        String b = getParameterAsString(IS_ADMIN);
        if(b ==null) {
            return false;
        }
        
        return b.equalsIgnoreCase("true");
    }

    public void setDelegationProxyId(String delegId) {
        addParameter(DELEGATION_PROXY_ID, delegId);
    }
    
    public void setFromDate(Calendar date) {
        if (date != null) {
            addParameter(FROM_DATE, "" + date.getTimeInMillis());
        }
    }

    public void setIsAdmin(boolean b) {
        addParameter(IS_ADMIN, Boolean.toString(b));
    }

    public void setJobId(String jobId) {
        addParameter(JOB_ID, jobId);
        deleteParameter(JOB_ID_LIST);
    }

    public void setJobIdList(List<String> jobId) {
        deleteParameter(JOB_ID);
        addParameter(JOB_ID_LIST, jobId);
    }
    
    public void setJobIdList(String[] jobId) {
        if(jobId == null) {
            return;
        }
        deleteParameter(JOB_ID);
        
        List<String> jobIdList = new ArrayList<String>(0);
        
        for(int i=0; i<jobId.length; i++) {
            jobIdList.add(jobId[i]);
        }
        
        addParameter(JOB_ID_LIST, jobIdList);
    }

    public void setJobStatus(int[] status) {
        if (status != null) {
            List<String> statusList = new ArrayList<String>(0);
            for(int i=0; i<status.length; i++) {
                statusList.add(JobStatus.statusName[status[i]]);
            }
            addParameter(JOB_STATUS_LIST, statusList);
        }
    }

    public void setJobStatus(List<String> status) {
        if (status != null) {
            addParameter(JOB_STATUS_LIST, status);
        }
    }
    
    public void setJobStatus(String[] status) {
        if (status != null) {
            List<String> jobStatusList = new ArrayList<String>(0);
            for(int i=0; i<status.length; i++) {
                jobStatusList.add(status[i]);
            }
            addParameter(JOB_STATUS_LIST, jobStatusList);
        }        
    }
    
    public void setLeaseId(String leaseId) {
        addParameter(LEASE_ID, leaseId);
    }

    public void setRemoteRequestAddress(String remoteAddress) {
        addParameter(REMOTE_REQUEST_ADDRESS, remoteAddress);
    }
    
    public void setToDate(Calendar date) {
        if (date != null) {
            addParameter(TO_DATE, "" + date.getTimeInMillis());
        }
    }
    
    protected void setType(int type) throws CommandException {
        if (type >= 0 && type < JobCommandConstant.cmdName.length) {
            this.type = type;
            setName(JobCommandConstant.cmdName[type]);
        } else {
            throw new CommandException("Job Command error: command type out of range (type=" + type + ")");
        }
    }

    public void setUserDN(String dn) {
        addParameter(USER_DN, dn);
    }
    
    public void setUserFQAN(String fqan) {
        addParameter(USER_FQAN, fqan);
    }
    
    public void setUserFQANList(List<String> fqan) {
        addParameter(USER_FQAN_LIST, fqan);
    }
}
