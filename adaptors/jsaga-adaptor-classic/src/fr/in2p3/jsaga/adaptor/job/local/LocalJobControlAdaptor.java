package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.SignalableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.SuspendableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.log4j.Logger;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

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
        JobControlAdaptor, CleanableJobAdaptor, StagingJobAdaptorTwoPhase, /*StreamableJobBatch,*/ SignalableJobAdaptor, SuspendableJobAdaptor  {

	private static Logger s_logger = Logger.getLogger(LocalJobControlAdaptor.class);
	
	private static final String SHELLPATH = "ShellPath";
    private String m_shellPath;
    
    private static final String ROOTDIR = "RootDir";

    private static final int SIGNAL_TERM = 15;
    private static final int SIGNAL_STOP = 19;
    private static final int SIGNAL_CONT = 18;
    
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
        translator.setAttribute(ROOTDIR, LocalJobProcess.getRootDir_Bash());
        return translator;
    }

    private LocalJobProcess submit(String jobDesc, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	LocalJobProcess ljp = new LocalJobProcess(uniqId, jobDesc);
		try {
            ljp.setCreated(new Date());
			LocalAdaptorAbstract.store(ljp);
			return ljp;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    	
    }

    public String submit(String commandLine, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	return this.submit(commandLine, uniqId, null).getJobId();
    	
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		this.signal(nativeJobId, LocalJobControlAdaptor.SIGNAL_TERM);
	}

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		LocalJobProcess ljp;
		try {
			ljp = LocalAdaptorAbstract.restore(nativeJobId);
			ljp.clean();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public boolean signal(String nativeJobId, int signum)
			throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		try {			
			LocalJobProcess ljp = LocalAdaptorAbstract.restore(nativeJobId);
			int ret = this.killProcess(Integer.parseInt(ljp.getPid()), signum);
			// could not run kill
			if (ret != 0) { return false;}
			// check if STOP or CONT worked
			if (signum == LocalJobControlAdaptor.SIGNAL_CONT || signum == LocalJobControlAdaptor.SIGNAL_STOP) {
				int processStatus = ljp.getProcessStatus();
				if (signum == LocalJobControlAdaptor.SIGNAL_STOP && processStatus == LocalJobProcess.PROCESS_STOPPED) { return true;}
				if (signum == LocalJobControlAdaptor.SIGNAL_CONT && processStatus == LocalJobProcess.PROCESS_RUNNING) { return true;}
				return false;
			}
			// OK
			return true;
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public boolean suspend(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		return this.signal(nativeJobId, LocalJobControlAdaptor.SIGNAL_STOP);
	}

	public boolean resume(String nativeJobId) throws PermissionDeniedException,	TimeoutException, NoSuccessException {
		return this.signal(nativeJobId, LocalJobControlAdaptor.SIGNAL_CONT);
	}

	public String getStagingDirectory(String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		return getStagingDirectory(uniqId);
	}

	public String getStagingDirectory(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		return "file://" + LocalJobProcess.getRootDir(); // + "/" + nativeJobId;
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		return LocalJobProcess.getStaging(nativeJobDescription, LocalJobProcess.STAGING_IN);
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		try {
			LocalJobProcess ljp = LocalAdaptorAbstract.restore(nativeJobId);
			return ljp.getInputStaging();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		try {
			LocalJobProcess ljp = LocalAdaptorAbstract.restore(nativeJobId);
			return ljp.getOutputStaging();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	private int killProcess(int pid, int signum) throws IOException, InterruptedException {
		String cde = "kill -" + String.valueOf(signum) + " " + String.valueOf(pid);
		Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde});
		p.waitFor();
		int ret = p.exitValue();
		if (ret != 0) {
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));  
            s_logger.warn("Could not kill process: " +br.readLine());
		}
		return ret;
	}

	public void start(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		LocalJobProcess ljp;
		Properties jobProps = new Properties();
		try {
			ljp = LocalAdaptorAbstract.restore(nativeJobId);
			jobProps.load(new ByteArrayInputStream(ljp.getJobDesc().getBytes()));
		} catch (ClassNotFoundException e1) {
			throw new NoSuccessException(e1);
		} catch (IOException e1) {
			throw new NoSuccessException(e1);
		}
		File _workDir = null;
		String cde = null;
		Enumeration e = jobProps.propertyNames();
		ArrayList<String> _envParams = new ArrayList<String>();
        while (e.hasMoreElements()){
               String key = (String)e.nextElement();
               String val = (String)jobProps.getProperty(key);
               if (key.equals("_WorkingDirectory")) { _workDir = new File(val);}
               else if (key.equals("_Executable")) { 
            	   cde = val;
               } else {
            	   _envParams.add(key + "=" + val);
               }
        }

		try {
	        Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde}, 
					(String[])_envParams.toArray(new String[]{}), 
					_workDir);
			// wait 100ms and try to get exitCode
			// if the process has not finished yet, IllegalThreadStateException is thrown
			// If the process has finished with code 255, simulate the IOException
			Thread.sleep(100);
			if (p.exitValue() == 255) {
				throw new IOException("Abnormal termintation");
			}
		} catch (IllegalThreadStateException itse) {
			// ignore: the process is not finished
		} catch (IOException ioe) {
			ljp.setReturnCode(2);
			
			FileOutputStream error;
			try {
				error = new FileOutputStream(new File(ljp.getErrfile()));
				error.write(ioe.getMessage().getBytes());
		        error.close();
			} catch (FileNotFoundException e1) {
                s_logger.warn("Could not write stderr: ", e1);
			} catch (IOException e1) {
                s_logger.warn("Could not write stderr: ", e1);
			}
//			try {
//				LocalAdaptorAbstract.store(ljp);
//			} catch (IOException e2) {
//				throw new NoSuccessException(e2);
//			}
		} catch (InterruptedException e1) {
			throw new NoSuccessException(e1);
		}
	}

}
