package fr.in2p3.jsaga.adaptor.ssh.job;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/

public class SSHJobMonitorAdaptor extends SSHAdaptorAbstract implements QueryIndividualJob, ListableJobAdaptor, JobInfoAdaptor {

	private String m_host; // needs to keep it for getExecutionHosts
	
    public String getType() {
        return "ssh";
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	m_host = host;
    }
    
    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		Integer rc = getReturnCode(nativeJobId);
    	return new SSHJobStatus(nativeJobId, rc);
    }        

	public String[] list() throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			Vector v = m_sftp.ls(SSHJobProcess.getRootDir() + "/*." + SSHJobProcess.PROCESS_SUFFIX);
			String[] listOfJobs = new String[v.size()];
			for (int i=0; i<v.size(); i++) {
				String file = ((LsEntry)v.elementAt(i)).getFilename();
				listOfJobs[i] = file.substring(0, file.length()-8);
			}
			return listOfJobs;
		} catch (SftpException e) {
    		throw new NoSuccessException(e);
		}
	}

	public Integer getExitCode(String nativeJobId)
		throws NotImplementedException, NoSuccessException {
		Integer rc = getReturnCode(nativeJobId);
		if (rc == SSHJobProcess.PROCESS_RUNNING)
			throw new NoSuccessException("Process not finished, exit code not available");
		return rc;
	}
	
	public Date getCreated(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		try {
			SSHJobProcess sshjp = restore(nativeJobId);
			return sshjp.getCreated();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		} catch (JSchException e) {
			throw new NoSuccessException(e);
		} catch (SftpException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		try {
			SftpATTRS attrs = m_sftp.stat(new SSHJobProcess(nativeJobId).getPidFile());
			return new Date(attrs.getMTime());
		} catch (SftpException e) {
			throw new NoSuccessException("Cannot get started time, the job has not started yet");
		}
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		try {
			SftpATTRS attrs = m_sftp.stat(new SSHJobProcess(nativeJobId).getEndcodeFile());
			return new Date(attrs.getMTime());
		} catch (SftpException e) {
			throw new NoSuccessException("Cannot get finish time, the job may still be running");
		}
	}

	public String[] getExecutionHosts(String nativeJobId)
			throws NotImplementedException, NoSuccessException {
		return new String[]{m_host};
	}

	private Integer getReturnCode(String nativeJobId) throws NoSuccessException {
    	try {    	
			InputStream is = m_sftp.get(new SSHJobProcess(nativeJobId).getEndcodeFile());
	    	byte[] buf = new byte[4];
	    	int len = is.read(buf);
	    	is.close();
    		return new Integer(new String(buf).trim());
					
    	} catch (SftpException sftpe) {
    		// try first to get serialized object
    		try {
				SSHJobProcess sjp = this.restore(nativeJobId);
				return sjp.getReturnCode();
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			} catch (JSchException e) {
			} catch (SftpException e) {
			} catch (InterruptedException e) {
			}
    		return SSHJobProcess.PROCESS_RUNNING;
    	} catch (Exception e) {
    		throw new NoSuccessException(e);
		}
	}

}
