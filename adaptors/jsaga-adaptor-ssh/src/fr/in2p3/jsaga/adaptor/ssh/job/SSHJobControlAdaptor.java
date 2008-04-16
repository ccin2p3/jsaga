package fr.in2p3.jsaga.adaptor.ssh.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;

import org.ggf.schemas.jsdl.posix.Argument;
import org.ggf.schemas.jsdl.posix.POSIXApplication_Type;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import com.jcraft.jsch.ChannelExec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

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
		JobControlAdaptor {

	public String getType() {
		return "ssh";
	}

	public String[] getSupportedSandboxProtocols() {
		return null; // no sandbox management
	}

	public String getTranslator() {
		return "xsl/job/ssh.xsl";
	}

	public Map getTranslatorParameters() {
		return null;
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new SSHJobMonitorAdaptor();
	}

	public String submit(String commandLine, boolean checkMatch)
			throws PermissionDenied, Timeout, NoSuccess {
		try {

			String jobId = String.valueOf(new Date().getTime());

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(commandLine);

			// set stderr / stdout
  			FileOutputStream stdout =new FileOutputStream(System.getProperty("user.home")+ File.separator + jobId+ ".stdout");
  			channel.setOutputStream(stdout);
			FileOutputStream stderr =new FileOutputStream(System.getProperty("user.home")+ File.separator + jobId+ ".stderr");
			channel.setErrStream(stderr);
			
			// start job
			channel.connect();
			
			// add channel in sessionMap
			SSHAdaptorAbstract.sessionMap.put(jobId+"-id", channel);
			SSHAdaptorAbstract.sessionMap.put(jobId+"-stdout", stdout);
			SSHAdaptorAbstract.sessionMap.put(jobId+"-stderr", stderr);
			
			return jobId;
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}

	public void cancel(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		try {

		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}

}
