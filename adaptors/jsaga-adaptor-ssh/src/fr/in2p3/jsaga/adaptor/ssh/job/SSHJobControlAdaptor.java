package fr.in2p3.jsaga.adaptor.ssh.job;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;
import org.ogf.saga.error.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
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
		JobControlAdaptor, CleanableJobAdaptor, StreamableJobInteractiveSet {

    private static final String ROOTDIR = "RootDir";

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
		// Create temp directories on server
		try {
			ChannelSftp channelMkdir = (ChannelSftp) session.openChannel("sftp");
			channelMkdir.connect();
			String fullUrl = "";
			for (String d: SSHJobProcess.getRootDir().split("/")) {
				fullUrl += d + "/";
				try {
					channelMkdir.mkdir(fullUrl);
				} catch (SftpException e) {
					if (e.id == ChannelSftp.SSH_FX_FAILURE) { // Already Exists
						// ignore
					} else if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
						channelMkdir.disconnect();
						throw new AuthorizationFailedException(e);
					} else {
						channelMkdir.disconnect();
						throw new NoSuccessException(e); 
					}
				}
			}
			channelMkdir.disconnect();
		} catch (JSchException e) {
			throw new NoSuccessException(e); 
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

    private SSHJobProcess submit(String jobDesc, String uniqId, InputStream stdin, OutputStream stdout, OutputStream stderr)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {

			ChannelExec channel = (ChannelExec) session.openChannel("exec");

	    	SSHJobProcess sjp = new SSHJobProcess(uniqId);
	    	
            sjp.setCreated(new Date());
            store(sjp, uniqId);

			Properties jobProps = new Properties();
			jobProps.load(new ByteArrayInputStream(jobDesc.getBytes()));
			File _workDir = null;
			String cde = null;
            Enumeration e = jobProps.propertyNames();
			Hashtable _envParams = new Hashtable();
            while (e.hasMoreElements()){
                   String key = (String)e.nextElement();
                   String val = (String)jobProps.getProperty(key);
                   if (key.equals("_WorkingDirectory")) { _workDir = new File(val);}
                   else if (key.equals("_Script")) { 
                	   cde = val;
                   } else {
						// setEnv does not work
                       //channel.setEnv(key.getBytes(), val.getBytes());
                	   _envParams.put(key, val);
                   }
            }
            // setEnv does not work
			//channel.setEnv("JOBID".getBytes(), uniqId.getBytes());
            // so we have to do this
            cde = "JOBID="+uniqId+"; "+cde;
            // and this
            Iterator i = _envParams.entrySet().iterator();

            while(i.hasNext()){
              Map.Entry me = (Map.Entry)i.next();
              cde = me.getKey() + "=" + me.getValue() + "; " + cde;
            }
			//cde = "#!/bin/bash\r\n " + cde;
            //cde = "echo '" + cde + "' | /bin/bash ";
System.out.println(cde);
            channel.setCommand(cde);

			if (stdout != null) channel.setOutputStream(stdout);
			if (stderr != null) channel.setErrStream(stderr);
			// set input stream before job starts
			if (stdin != null) {
				channel.setInputStream(stdin);
			}
			channel.connect();
			// close null stdin after job started
			if (stdin == null) {
				channel.getOutputStream().close();
			}
			
			Thread.sleep(500);
			if (channel.isClosed()) {
				int error = channel.getExitStatus();
				// It is possible that the wrapper script failed without writing any .endcode file
				// Here this is simulated by storing returnCode into the serialized object
				sjp.setReturnCode(error);
	            store(sjp, uniqId);
				System.out.println("channel is closed and return = " + error);
				/*if (error != 0) {
					InputStream err = channel.getErrStream();
					byte[] buf = new byte[2048];
					int len = err.read(buf);
					err.close();
					System.out.println(new String(buf).trim());
				}*/
			}
			return sjp;
			
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

    public String submit(String jobDesc, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	return this.submit(jobDesc, uniqId, null, null, null).getJobId();
    }

    public String submitInteractive(String jobDesc, boolean checkMatch, InputStream stdin, OutputStream stdout, OutputStream stderr) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	SSHJobProcess sjp = this.submit(jobDesc, UUID.randomUUID().toString(), stdin, stdout, stderr);
    	return sjp.getJobId();
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		try {
			ChannelExec channelCancel = (ChannelExec) session.openChannel("exec");
			channelCancel.setCommand("MYPID=`cat " + SSHJobProcess.getRootDir() + "/" + nativeJobId+".pid`; kill $MYPID ;");
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
		try {
			ChannelExec channelClean = (ChannelExec) session.openChannel("exec");
			channelClean.setCommand("/bin/rm -rf " + SSHJobProcess.getRootDir() + "/" + nativeJobId + ".*");
			channelClean.connect();
			while(!channelClean.isClosed()) {
				Thread.sleep(100);
			}
			channelClean.disconnect();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

}
