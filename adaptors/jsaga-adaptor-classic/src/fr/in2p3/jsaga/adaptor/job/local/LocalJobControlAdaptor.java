package fr.in2p3.jsaga.adaptor.job.local;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LocalJobControlAdaptor
 * Author: Nicolas DEMESY (nicolas.demesy@bt.com)
 * Date:   29 avril 2008
 * ***************************************************/

/**
 * TODO : Support of pre-requisite
 * TODO : Get ShellPath
 */
public class LocalJobControlAdaptor extends LocalAdaptorAbstract implements
		JobControlAdaptor, CleanableJobAdaptor {
	//, InteractiveJobAdaptor, JobIOGetter

	private static final String SHELLPATH = "ShellPath";
	private Map m_parameters;    
	private String jobId;
		
	public Usage getUsage() {
        return new UOptional(SHELLPATH);
    }

	public String[] getSupportedSandboxProtocols() {
		return null; // no sandbox management
	}

	public String getTranslator() {
		return "xsl/job/sh.xsl";
	}

	public Map getTranslatorParameters() {
		return m_parameters;
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new LocalJobMonitorAdaptor();
	}
	
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    	super.connect(userInfo, host, port, basePath, attributes);
    	m_parameters = attributes;
    }

	public String submit(String commandLine, boolean checkMatch)
			throws PermissionDenied, Timeout, NoSuccess {
		try {

			jobId = UUID.randomUUID().toString();
			String cde = prepareCde(commandLine);
			Process p = Runtime.getRuntime().exec("d:/ndemesy/cygwin/bin/sh.exe -c \""+cde+"\"");
			// add process in sessionMap
			LocalAdaptorAbstract.sessionMap.put(jobId, p);
			return jobId;
			
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}
	
	public JobIOHandler submitInteractive(String commandLine,
			boolean checkMatch) throws PermissionDenied, Timeout, NoSuccess {
		
		try {
		
			jobId = UUID.randomUUID().toString();
			String cde = prepareCde(commandLine);
			
			Process p = Runtime.getRuntime().exec("d:/ndemesy/cygwin/bin/sh.exe -c \""+cde+"\"");
			// TODO
			OutputStream stdin = p.getOutputStream();
			stdin.write("Test".getBytes());
			stdin.close();
			// add process in sessionMap
			LocalAdaptorAbstract.sessionMap.put(jobId, p);
			return new LocalJobIOHandler(p, jobId);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}

	private String prepareCde(String commandLine) {
		return 	"eval '"+commandLine+" &' ; " +
			"MYPID=$! ; " +
			"echo $MYPID > ."+jobId+" ;" +
			"wait $MYPID;" +
			"ENDCODE=$?;" +
			"/bin/rm -f ."+ jobId + ";"  +
			"exit $ENDCODE;";
	}

	public void cancel(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		try {			
			String cde = "d:/ndemesy/cygwin/bin/sh.exe -c 'echo \"MYPID=`cat ."+nativeJobId+"`; kill $MYPID ;'";
			Process p = Runtime.getRuntime().exec(cde);
			p.waitFor();
		} catch (IOException e) {
			throw new NoSuccess(e);
		} catch (InterruptedException e) {
			throw new NoSuccess(e);
		}
	}

	public void clean(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		Process p = (Process) LocalAdaptorAbstract.sessionMap.get(nativeJobId);
		p.destroy();
		LocalAdaptorAbstract.sessionMap.remove(p);
	}
}
