package fr.in2p3.jsaga.adaptor.ssh.job;

import com.jcraft.jsch.ChannelExec;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

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
		JobControlAdaptor, CleanableJobAdaptor/*, StreamableJobInteractiveSet*/ {

    private static final String ROOTDIR = "RootDir";

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

    private SSHJobProcess submit(String jobDesc, String uniqId, InputStream stdin)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {

			ChannelExec channel = (ChannelExec) session.openChannel("exec");

	    	SSHJobProcess sjp = new SSHJobProcess(uniqId, channel);
	    	
            sjp.setCreated(new Date());
            SSHAdaptorAbstract.store(sjp, uniqId);

			//LSString jobId = UUID.randomUUID().toString();
			
			//commandLine
			Properties jobProps = new Properties();
			jobProps.load(new ByteArrayInputStream(jobDesc.getBytes()));
			File _workDir = null;
			String cde = null;
			String executable = null;
            Enumeration e = jobProps.propertyNames();
			Hashtable _envParams = new Hashtable();
            while (e.hasMoreElements()){
                   String key = (String)e.nextElement();
                   String val = (String)jobProps.getProperty(key);
                   if (key.equals("_WorkingDirectory")) { _workDir = new File(val);}
                   else if (key.equals("_Script")) { 
                	   cde = val;
                   } else {
                	   _envParams.put(key, val);
                   }
            }
			//LSchannel.setCommand(prepareCde(commandLine, uniqId));
            channel.setCommand(cde);
            channel.setEnv(_envParams);
	    	// TODO : scp input file

			// start job
			channel.connect();	
			
			// add channel in sessionMap
			//SSHAdaptorAbstract.sessionMap.put(uniqId, channel);
			return sjp;
			
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

    public String submit(String commandLine, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	return this.submit(commandLine, uniqId, null).getJobId();
    }

    /*
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
	*/

    /*
	private String prepareCde(String commandLine, String jobId) {
		return 	"eval '"+commandLine+" &' ; " +
			"MYPID=$! ; " +
			"echo $MYPID > ."+jobId+" ;" +
			"wait $MYPID;" +
			"ENDCODE=$?;" +
			"rm -f ."+ jobId + ";"  +
			"exit $ENDCODE;";
	}
	*/
    
	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		SSHJobProcess sjp;
		try {
			sjp = SSHAdaptorAbstract.restore(nativeJobId);
			//sjp.kill();
			ChannelExec channelCancel = (ChannelExec) session.openChannel("exec");
			//channelCancel.setCommand("MYPID=`cat ."+nativeJobId+"`; kill $MYPID ;");
			channelCancel.setCommand("kill " + sjp.getPid());
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
		SSHJobProcess sjp;
		try {
			sjp = SSHAdaptorAbstract.restore(nativeJobId);
			//sjp.clean();
			ChannelExec channelClean = (ChannelExec) session.openChannel("exec");
			//channelCancel.setCommand("MYPID=`cat ."+nativeJobId+"`; kill $MYPID ;");
			channelClean.setCommand("/bin/rm -rf " + SSHJobProcess.getRootDir() + "/" + sjp.getJobId() + ".*");
			// start cancel
			channelClean.connect();
			while(!channelClean.isClosed()) {
				Thread.sleep(100);
			}
			channelClean.disconnect();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		/*		
		ChannelExec channel = (ChannelExec) SSHAdaptorAbstract.sessionMap.get(nativeJobId);
        if (channel == null) {
            throw new NoSuccessException("Job id not found in current JVM: "+nativeJobId);
        }
		channel.disconnect();
		SSHAdaptorAbstract.sessionMap.remove(channel);
		*/
	}
}
