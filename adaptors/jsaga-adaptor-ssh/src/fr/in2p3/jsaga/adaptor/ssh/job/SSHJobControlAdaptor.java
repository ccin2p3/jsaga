package fr.in2p3.jsaga.adaptor.ssh.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import com.jcraft.jsch.ChannelExec;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;

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
		JobControlAdaptor, CleanableJobAdaptor{
//, InteractiveJobAdaptor, JobIOGetter 
	private String jobId;
	private File stdoutFile, stderrFile, stdinFile;
	
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

			ChannelExec channel = (ChannelExec) session.openChannel("exec");

			jobId = UUID.randomUUID().toString();
			String cde = prepareCde(commandLine);
			
			//commandLine
			channel.setCommand(cde);
			// start job
			channel.connect();	
			
			// add channel in sessionMap
			SSHAdaptorAbstract.sessionMap.put(jobId, channel);
			return jobId;
			
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}
	/*
	public JobIOHandler submitInteractive(String commandLine,
			boolean checkMatch) throws PermissionDenied, Timeout, NoSuccess {
		
		try {
			//ChannelShell channel = (ChannelShell) session.openChannel("shell");
			ChannelExec channel = (ChannelExec) session.openChannel("exec");

			jobId = UUID.randomUUID().toString();
			
			// set stdout
			stdoutFile = File.createTempFile(jobId, ".stdout");
			stdoutFile.deleteOnExit();
			//OutputStream outputStream = new FileOutputStream(stdoutFile);
			channel.setOutputStream(System.out);
			
			// set stderr
			stderrFile = File.createTempFile(jobId, ".stderr");
			stderrFile.deleteOnExit();
			OutputStream errorStream = new FileOutputStream(stderrFile);
			channel.setErrStream(System.err);		
	
			// set stdin
			//stdinFile = File.createTempFile(jobId, ".stdin");
			//stdinFile.deleteOnExit();
			stdinFile = new File("d://test.sh");
			InputStream inputStream = new FileInputStream(stdinFile);
			channel.setInputStream(inputStream);	
			
			//commandLine
			channel.setCommand("sh");
			
			// start job
			channel.connect();
			inputStream.close();
			
			// add channel in sessionMap
			SSHAdaptorAbstract.sessionMap.put(jobId, channel);
			return this;
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}*/

	private String prepareCde(String commandLine) {
		return 	"eval '"+commandLine+" &' ; " +
			"MYPID=$! ; " +
			"echo $MYPID > ."+jobId+" ;" +
			"wait $MYPID;" +
			"ENDCODE=$?;" +
			"rm -f ."+ jobId + ";"  +
			"exit $ENDCODE;";
	}

	public void cancel(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		try {
			
			ChannelExec channelCancel = (ChannelExec) session.openChannel("exec");
			channelCancel.setCommand("MYPID=`cat ."+nativeJobId+"`; kill $MYPID ;");
			// start cancel
			channelCancel.connect();
			while(!channelCancel.isClosed()) {
				Thread.sleep(100);
			}
			channelCancel.disconnect();
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}

	public void clean(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		ChannelExec channel = (ChannelExec) SSHAdaptorAbstract.sessionMap.get(nativeJobId);
		channel.disconnect();
		SSHAdaptorAbstract.sessionMap.remove(channel);
	}

	public String getJobId() {
		return jobId;
	}

	public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess {
		InputStream iS;
		try {
			iS = new FileInputStream(stderrFile);
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
		return iS;
	}

	public OutputStream getStdin() throws PermissionDenied, Timeout, NoSuccess {
		OutputStream oS;
		try {
			oS = new FileOutputStream(stdinFile);
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
		return oS;
	}

	public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess {
		InputStream iS;
		try {
			iS = new FileInputStream(stdoutFile);
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
		return iS;
	}
}
