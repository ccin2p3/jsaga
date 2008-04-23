package fr.in2p3.jsaga.adaptor.ssh.job;

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
		JobControlAdaptor, CleanableJobAdaptor {

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

			String jobId = UUID.randomUUID().toString();
			String cde = "eval '"+commandLine+" &' ; " +
					"MYPID=$! ; " +
					"echo $MYPID > ."+jobId+" ;" +
					"wait $MYPID;" +
					"ENDCODE=$?;" +
					"rm -f ."+ jobId + ";"  +
					"exit $ENDCODE;";
			
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
			e.printStackTrace();
			throw new NoSuccess(e);
		}
	}

	public void clean(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		ChannelExec channel = (ChannelExec) SSHAdaptorAbstract.sessionMap.get(nativeJobId);
		if(channel.isClosed()) {
			channel.disconnect();
		}
		SSHAdaptorAbstract.sessionMap.remove(channel);
	}

}
