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
        JobControlAdaptor, CleanableJobAdaptor, StreamableJobBatch, SignalableJobAdaptor, SuspendableJobAdaptor  {

	private static Logger s_logger = Logger.getLogger(LocalJobControlAdaptor.class);
	
	private static final String SHELLPATH = "ShellPath";
    private String m_shellPath;

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
        return translator;
    }

    private LocalJobProcess submit(String jobDesc, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	LocalJobProcess ljp = new LocalJobProcess(uniqId);
		try {
            File input = new File(ljp.getInfile());
			try {
				if (stdin == null) {
					input.createNewFile();
				} else {
			        OutputStream out = new FileOutputStream(input);
					int n;
					byte[] buffer = new byte[1024];
					while ((n = stdin.read(buffer)) != -1) {
						out.write(buffer, 0, n);
					}
			        out.close();
				}
			} catch (IOException e) {
				throw new NoSuccessException("Standard input could not be written to local file: " + input);
			}

			Properties jobProps = new Properties();
			jobProps.load(new ByteArrayInputStream(jobDesc.getBytes()));
			File _workDir = null;
			String cde = null;
			String executable = null;
            Enumeration e = jobProps.propertyNames();
			ArrayList _envParams = new ArrayList();
            while (e.hasMoreElements()){
                   String key = (String)e.nextElement();
                   String val = (String)jobProps.getProperty(key);
                   if (key.equals("_WorkingDirectory")) { _workDir = new File(val);}
                   else if (key.equals("_Executable")) { 
                	   executable = val;
                   } else {
                	   _envParams.add(key + "=" + val);
                   }
            }
 		   cde = "eval ' " + executable + " < " + ljp.getInfile() +
			" > " + ljp.getOutfile() + 
			" 2> " + ljp.getErrfile() + " &' ; " +
			"MYPID=$! ; " +
			"echo $MYPID > " + ljp.getPidfile() + " ;" +
			"wait $MYPID;" +
			"ENDCODE=$?;" +
			"echo $ENDCODE > " + ljp.getEndcodefile() + " ;" +
			//"/bin/rm -f "+ ljp.getPidfile() + ";"  +
			"exit $ENDCODE;";
            ljp.setCreated(new Date());
			LocalAdaptorAbstract.store(ljp);
			Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde}, 
													(String[])_envParams.toArray(new String[]{}), 
													_workDir);
			return ljp;
		} catch (IOException ioe) {
			ljp.setReturnCode(2);
			
			FileOutputStream error;
			try {
				error = new FileOutputStream(new File(ljp.getErrfile()));
				error.write(ioe.getMessage().getBytes());
		        error.close();
			} catch (FileNotFoundException e) {
                s_logger.warn("Could not write stderr: ", e);
			} catch (IOException e) {
                s_logger.warn("Could not write stderr: ", e);
			}
			try {
				LocalAdaptorAbstract.store(ljp);
			} catch (IOException e) {
				throw new NoSuccessException(e);
			}
			return ljp;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    	
    }

    public String submit(String commandLine, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	return this.submit(commandLine, uniqId, null).getJobId();
    	
	}

	public JobIOHandler submit(String jobDesc, boolean checkMatch,
			String uniqId, InputStream stdin) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		LocalJobProcess ljp = this.submit(jobDesc, uniqId, stdin);
		return new LocalJobIOHandler(ljp);
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		/*
		try {			
			LocalJobProcess ljp = LocalAdaptorAbstract.restore(nativeJobId);
			String cde = "kill " + ljp.getPid();
			Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde});
			p.waitFor();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		}*/
		this.signal(nativeJobId, this.SIGNAL_TERM);
	}

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
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
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {			
			LocalJobProcess ljp = LocalAdaptorAbstract.restore(nativeJobId);
			int ret = this.killProcess(Integer.parseInt(ljp.getPid()), signum);
			// could not run kill
			if (ret != 0) { return false;}
			// check if STOP or CONT worked
			if (signum == this.SIGNAL_CONT || signum == this.SIGNAL_STOP) {
				int processStatus = ljp.getProcessStatus();
				if (signum == this.SIGNAL_STOP && processStatus == LocalJobProcess.PROCESS_SUSPENDED) { return true;}
				if (signum == this.SIGNAL_CONT && processStatus == LocalJobProcess.PROCESS_RUNNING) { return true;}
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
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		return this.signal(nativeJobId, this.SIGNAL_STOP);
	}

	public boolean resume(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		return this.signal(nativeJobId, this.SIGNAL_CONT);
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
	
	private boolean checkProcessStatus(int pid, int signum) throws IOException, InterruptedException {
/*		String cde = "ps h -o stat -p " + String.valueOf(pid);
		Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde});
		p.waitFor();
System.out.println("exitCode="+p.exitValue()+"**");
		if (p.exitValue() != 0) { return false;}
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
		//StringBuffer sb = new StringBuffer();  
		String status = br.readLine();
System.out.println("status="+status+"**");
		//String status = sb.toString();
		*/
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/proc/"+String.valueOf(pid)+"/stat"))));
		String status = br.readLine().split(" ")[2];

		return false;
	}
}
