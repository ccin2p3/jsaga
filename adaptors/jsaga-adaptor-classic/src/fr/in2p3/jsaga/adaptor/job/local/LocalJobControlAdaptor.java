package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
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
        JobControlAdaptor, CleanableJobAdaptor, StreamableJobBatch {

	private static Logger s_logger = Logger.getLogger(LocalJobControlAdaptor.class);
	
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

    private LocalJobProcess submit(String jobDesc, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	LocalJobProcess ljp = new LocalJobProcess(uniqId);
		try {
  s_logger.warn(jobDesc);

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
			/*try {
				new File(ljp.getOutfile()).createNewFile();
			} catch (IOException e) {
				// ignore
			}
			try {
				new File(ljp.getErrfile()).createNewFile();
			} catch (IOException e) {
				// ignore
			}*/
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
s_logger.warn(cde);
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
		}
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

}
