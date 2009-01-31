package fr.in2p3.jsaga.adaptor.ssh.job;

import java.util.Map;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import com.jcraft.jsch.ChannelExec;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
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
		JobControlAdaptor, CleanableJobAdaptor, StreamableJobInteractiveSet {

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

	public String submit(String commandLine, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {

			ChannelExec channel = (ChannelExec) session.openChannel("exec");

			String jobId = UUID.randomUUID().toString();
			
			//commandLine
			channel.setCommand(prepareCde(commandLine, jobId));

			// start job
			channel.connect();	
			
			// add channel in sessionMap
			SSHAdaptorAbstract.sessionMap.put(jobId, channel);
			return jobId;
			
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

    public String submitInteractive(String commandLine, boolean checkMatch, InputStream stdin, OutputStream stdout, OutputStream stderr) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			ChannelExec channel = (ChannelExec) session.openChannel("exec");

			String jobId = UUID.randomUUID().toString();
			
			// TODO: must use prepareCde, else no cancel possible
			//channel.setCommand(prepareCde(commandLine, jobId));
			channel.setCommand(commandLine);
			
            // set streams
            if (stdin != null) {
                channel.setInputStream(stdin);
            }
            channel.setOutputStream(stdout);
            channel.setErrStream(stderr);

            // start job
			channel.connect();
			
			// add channel in sessionMap
			SSHAdaptorAbstract.sessionMap.put(jobId, channel);
			return jobId;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	private String prepareCde(String commandLine, String jobId) {
		return 	"eval '"+commandLine+" &' ; " +
			"MYPID=$! ; " +
			"echo $MYPID > ."+jobId+" ;" +
			"wait $MYPID;" +
			"ENDCODE=$?;" +
			"rm -f ."+ jobId + ";"  +
			"exit $ENDCODE;";
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
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
			throw new NoSuccessException(e);
		}
	}

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		ChannelExec channel = (ChannelExec) SSHAdaptorAbstract.sessionMap.get(nativeJobId);
		channel.disconnect();
		SSHAdaptorAbstract.sessionMap.remove(channel);
	}
}
