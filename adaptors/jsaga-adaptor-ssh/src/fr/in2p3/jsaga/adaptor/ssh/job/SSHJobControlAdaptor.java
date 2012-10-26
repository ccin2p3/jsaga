package fr.in2p3.jsaga.adaptor.ssh.job;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.local.LocalAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.local.LocalJobProcess;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;
import org.ogf.saga.error.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SSHJobControlAdaptor
 * Author: Nicolas DEMESY (nicolas.demesy@bt.com)
 * Date:   11 avril 2008
 * ***************************************************/

/**
 * TODO : Support of pre-requisite
 */
public class SSHJobControlAdaptor extends SSHAdaptorAbstract implements
		JobControlAdaptor, CleanableJobAdaptor, StagingJobAdaptorTwoPhase/*StreamableJobInteractiveSet*/ {

    private static final String ROOTDIR = "RootDir";

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
		String fullUrl = "";
		for (String d: SSHJobProcess.getRootDir().split("/")) {
			fullUrl += d + "/";
			try {
				m_sftp.mkdir(fullUrl);
			} catch (SftpException e) {
				if (e.id == ChannelSftp.SSH_FX_FAILURE) { // Already Exists
					// ignore
				} else if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
					m_sftp.disconnect();
					throw new AuthorizationFailedException(e);
				} else {
					m_sftp.disconnect();
					throw new NoSuccessException(e); 
				}
			}
		}
    }
    
    public String getType() {
		return "ssh";
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new SSHJobMonitorAdaptor();
	}

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/ssh.xsl");
        translator.setAttribute(ROOTDIR, SSHJobProcess.getRootDir());
        return translator;
    }

    private SSHJobProcess submit(String jobDesc, String uniqId)
			throws PermissionDeniedException, TimeoutException, BadResource, NoSuccessException {
    	SSHJobProcess sjp;
		try {
			sjp = new SSHJobProcess(uniqId, jobDesc, session.getHost(), session.getPort(), m_sftp.getHome());
		} catch (SftpException sftpe) {
			throw new NoSuccessException(sftpe);
		}
    	sjp.checkResources();
    	// create user working directory only if not specified
		try {
			if (!sjp.isUserWorkingDirectory()) {
				m_sftp.mkdir(sjp.getWorkingDirectory());
			}
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_FAILURE) { // Already Exists
				// ignore
			} else if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				m_sftp.disconnect();
				throw new PermissionDeniedException(e);
			} else {
				m_sftp.disconnect();
				throw new NoSuccessException(e); 
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
        sjp.setCreated(new Date());
		try {
            store(sjp, uniqId);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		return sjp;
	}

	public String submit(String jobDesc, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, BadResource, NoSuccessException {
    	return this.submit(jobDesc, uniqId).getJobId();
    }

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		ChannelExec channelCancel=null;
		try {
			channelCancel = (ChannelExec) session.openChannel("exec");
			// TODO: use SSHJobProcess to get pidfile
			channelCancel.setCommand("kill `cat " + SSHJobProcess.getRootDir() + "/" + nativeJobId+".pid`;");
			channelCancel.connect();
			while(!channelCancel.isClosed()) {
				Thread.sleep(100);
			}
			int error = channelCancel.getExitStatus();
			if (error > 0) {
				throw new Exception("Cancel command failed with error code: " + error);
			}
		} catch (Exception e) {
			throw new NoSuccessException(e);
		} finally {
			if (channelCancel != null) channelCancel.disconnect();
		}
	}

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		try {
			// TODO: use SSHJobProcess to get filename
			m_sftp.rm(SSHJobProcess.getRootDir() + "/" + nativeJobId + ".*");
			m_sftp.rmdir(SSHJobProcess.getRootDir() + "/" + nativeJobId);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	private class ShellScriptBuffer  {
		private String m_script = "";
		private final String EOL = "\n";
		public void append(String s) {
			m_script += s + EOL;
		}
		public String toString() {
			return m_script;
		}
	}

	public String getStagingDirectory(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// The staging directory is built at connect
		return null;
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			SSHJobProcess sshjp = restore(nativeJobId);
			return sshjp.getInputStaging();
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

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			SSHJobProcess sshjp = restore(nativeJobId);
			return sshjp.getOutputStaging();
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

	public void start(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		try {
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
	    	
	    	ShellScriptBuffer command = new ShellScriptBuffer();
			Properties jobProps = new Properties();
			SSHJobProcess sshjp = restore(nativeJobId);
			jobProps.load(new ByteArrayInputStream(sshjp.getJobDesc().getBytes()));
			String _exec = null;
	        Enumeration e = jobProps.propertyNames();
			Hashtable _envParams = new Hashtable();
	        while (e.hasMoreElements()){
	               String key = (String)e.nextElement();
	               String val = (String)jobProps.getProperty(key);
	               if (key.equals("_Executable")) {
	            	   // The following line does not work -> indexArrayOutOfBoundException
	            	   // _exec = val.replaceAll("\\$", "\\\\$");
	            	   _exec = Matcher.quoteReplacement(val);
	               } else {
	            	   // channel.setEnv does not work
	            	   _envParams.put(key, val);
	               }
	        }
	 	    // channel.setEnv does not work
	        Iterator i = _envParams.entrySet().iterator();
	
	        while(i.hasNext()){
	          Map.Entry me = (Map.Entry)i.next();
	          command.append(me.getKey() + "=" + me.getValue());
	        }

	        command.append(_exec);
			String cde = "cat << EOS | bash -s \n" + command.toString() + "EOS\n";
//			System.out.println("NEW command="+cde);
	        channel.setCommand(cde);
	
			channel.connect();
			Thread.sleep(1000);
			if (channel.isClosed()) {
				int error = channel.getExitStatus();
				// It is possible that the wrapper script failed without writing any .endcode file
				// Here this is simulated by storing returnCode into the serialized object
				sshjp.setReturnCode(error);
				store(sshjp, nativeJobId);
	            //System.out.println("channel is closed and return = " + error);
			}
		} catch (JSchException e) {
			throw new NoSuccessException(e);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		} catch (SftpException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		}
	
	}
}
