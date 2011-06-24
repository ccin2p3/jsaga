package fr.in2p3.jsaga.adaptor.ssh.job;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
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
		JobControlAdaptor, CleanableJobAdaptor, StreamableJobInteractiveSet {

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

    private SSHJobProcess submit(String jobDesc, String uniqId, InputStream stdin, OutputStream stdout, OutputStream stderr)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	SSHJobProcess sjp = new SSHJobProcess(uniqId);
		try {
//System.out.println(jobDesc);
			if (stdin != null) {
				m_sftp.put(stdin, sjp.getInfile());
			} else {
				m_sftp.put(sjp.getInfile()).close();
			}

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
	    	
            sjp.setCreated(new Date());
            store(sjp, uniqId);

        	ShellScriptBuffer command = new ShellScriptBuffer();
    		Properties jobProps = new Properties();
    		jobProps.load(new ByteArrayInputStream(jobDesc.getBytes()));
    		File _workDir = null;
    		String _exec = null;
            Enumeration e = jobProps.propertyNames();
    		Hashtable _envParams = new Hashtable();
            while (e.hasMoreElements()){
                   String key = (String)e.nextElement();
                   String val = (String)jobProps.getProperty(key);
                   if (key.equals("_WorkingDirectory")) { 
                	   _workDir = new File(val);
                   } else if (key.equals("_Executable")) {
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
        	if (_workDir != null) {
			   command.append("if [ ! -d " + _workDir + " ] ");
			   command.append("then exit 1");
			   command.append("fi");
			   command.append("cd "+_workDir);
        	}
   			command.append("eval '" + _exec + " < " + m_sftp.getHome() + "/" +sjp.getInfile() + " &' ");
        	command.append("MYPID=\\$!");
        	command.append("echo \\$MYPID > " + m_sftp.getHome() + "/" + SSHJobProcess.getRootDir() + "/" + uniqId + ".pid");
        	command.append("wait \\$MYPID");
    		command.append("errcode=\\$?");
        	command.append("echo \\$errcode > " + m_sftp.getHome() + "/" + SSHJobProcess.getRootDir() + "/" + uniqId + ".endcode");
        	command.append("exit \\$errcode");

			String cde = "cat << EOS | bash -s \n" + command.toString() + "EOS\n";
//System.out.println("NEW command="+cde);
            channel.setCommand(cde);

			if (stdout != null) channel.setOutputStream(stdout);
			//else channel.setOutputStream(System.out);
			if (stderr != null) channel.setErrStream(stderr);
			//else channel.setErrStream(System.err);
			channel.connect();
			
			Thread.sleep(500);
			if (channel.isClosed()) {
				int error = channel.getExitStatus();
				// It is possible that the wrapper script failed without writing any .endcode file
				// Here this is simulated by storing returnCode into the serialized object
				sjp.setReturnCode(error);
	            store(sjp, uniqId);
	            //System.out.println("channel is closed and return = " + error);
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
		ChannelExec channelCancel=null;
		try {
			channelCancel = (ChannelExec) session.openChannel("exec");
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
			m_sftp.rm(SSHJobProcess.getRootDir() + "/" + nativeJobId + ".*");
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
}
