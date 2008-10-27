package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveGet;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.ogf.saga.error.*;

import java.io.IOException;
import java.lang.Exception;
import java.util.Map;
import java.util.UUID;

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
 */
public class LocalJobControlAdaptor extends LocalAdaptorAbstract implements
        JobControlAdaptor, CleanableJobAdaptor, StreamableJobInteractiveGet {

	private static final String SHELLPATH = "ShellPath";
    private String m_shellPath;
    private Map m_parameters;

	public Usage getUsage() {
        return new UOptional(SHELLPATH);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{new Default(SHELLPATH, "/bin/sh")};
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
	
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
        m_shellPath = (String) attributes.get(SHELLPATH);
        m_parameters = attributes;
    }

	public String submit(String commandLine, boolean checkMatch)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {

			String jobId = UUID.randomUUID().toString();
			String cde = prepareCde(commandLine, jobId);
			Process p = Runtime.getRuntime().exec(m_shellPath+" -c \""+cde+"\"");
			// add process in sessionMap
			LocalAdaptorAbstract.sessionMap.put(jobId, p);
			return jobId;
			
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
	
	public JobIOGetterInteractive submitInteractive(String commandLine,
			boolean checkMatch) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		
		try {
		
			String jobId = UUID.randomUUID().toString();
            //fixme: workaround for passing stdin to the user command
//			String cde = prepareCde(commandLine, jobId);
			Process p = Runtime.getRuntime().exec(m_shellPath+" -c \""+commandLine+"\"");
			// add process in sessionMap
			LocalAdaptorAbstract.sessionMap.put(jobId, p);
			return new LocalJobIOHandler(p, jobId);
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
			"/bin/rm -f ."+ jobId + ";"  +
			"exit $ENDCODE;";
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		try {			
			String cde = m_shellPath+" -c 'echo \"MYPID=`cat ."+nativeJobId+"`; kill $MYPID ;'";
			Process p = Runtime.getRuntime().exec(cde);
			p.waitFor();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		}
	}

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		Process p = (Process) LocalAdaptorAbstract.sessionMap.get(nativeJobId);
		p.destroy();
		LocalAdaptorAbstract.sessionMap.remove(p);
	}
}
