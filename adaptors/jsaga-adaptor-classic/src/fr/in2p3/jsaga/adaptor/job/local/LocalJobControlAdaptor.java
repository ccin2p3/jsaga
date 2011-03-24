package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveGet;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
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

	public Usage getUsage() {
        return new UOptional(SHELLPATH);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        //NOTE: sh may be more restrictive than bash
        return new Default[]{new Default(SHELLPATH, "/bin/bash")};
    }

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new LocalJobMonitorAdaptor();
	}
	
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
        m_shellPath = (String) attributes.get(SHELLPATH);
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/sh.xsl");
        translator.setAttribute(SHELLPATH, m_shellPath);
        return translator;
    }

    private String submit(String commandLine, boolean isInteractive) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		String jobId = UUID.randomUUID().toString();
		try {

			Properties jobProps = new Properties();
			jobProps.load(new ByteArrayInputStream(commandLine.getBytes()));
			File _workDir = null;
			String cde = null;
            Enumeration e = jobProps.propertyNames();
			ArrayList _envParams = new ArrayList();
            while (e.hasMoreElements()){
                   String key = (String)e.nextElement();
                   String val = (String)jobProps.getProperty(key);
                   if (key.equals("_WorkingDirectory")) { _workDir = new File(val);}
                   else if (key.equals("_Executable")) { 
                	   if (!isInteractive) {
                		   cde = prepareCde(val, jobId);
                	   } else {
                		   cde = val;
                	   }
                   } else {
                	   _envParams.add(key + "=" + val);
                   }
            }
			
			Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde}, 
													(String[])_envParams.toArray(new String[]{}), 
													_workDir);
			// add process in sessionMap
			LocalAdaptorAbstract.sessionMap.put(jobId, p);
			return jobId;
		} catch (IOException ioe) {
			LocalAdaptorAbstract.sessionMap.put(jobId, new LocalJobProcess(ioe.getMessage()));
			return jobId;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    	
    }

    public String submit(String commandLine, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	return this.submit(commandLine, false);
	}
	
	public JobIOGetterInteractive submitInteractive(String commandLine,
			boolean checkMatch) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		
		String jobId = this.submit(commandLine, true);
		return new LocalJobIOHandler((Process)LocalAdaptorAbstract.sessionMap.get(jobId), jobId);
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
