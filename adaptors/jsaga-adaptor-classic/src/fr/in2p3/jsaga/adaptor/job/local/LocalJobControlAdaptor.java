package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.ArrayList;
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
        JobControlAdaptor, CleanableJobAdaptor {

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

    private String submit(String jobDesc, String uniqId/*, boolean isInteractive*/) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	// TODO create object LocalJobProcess with uniqId
		try {
System.out.println(jobDesc);
			Properties jobProps = new Properties();
			jobProps.load(new ByteArrayInputStream(jobDesc.getBytes()));
			File _workDir = null;
			String cde = null;
            Enumeration e = jobProps.propertyNames();
			ArrayList _envParams = new ArrayList();
            while (e.hasMoreElements()){
                   String key = (String)e.nextElement();
                   String val = (String)jobProps.getProperty(key);
                   if (key.equals("_WorkingDirectory")) { _workDir = new File(val);}
                   else if (key.equals("_Executable")) { 
                	   //if (!isInteractive) {
                		   cde = prepareCde(val, uniqId);
                	   //} else {
                		//   cde = val;
                	   //}
                   } else {
                	   _envParams.add(key + "=" + val);
                	   // TODO: set stdout stderr to LocalJobprocess object
                   }
            }
			//System.out.println(m_shellPath);
			//System.out.println("-c");
			//System.out.println(cde);
			//System.out.println(_envParams);
			Process p = Runtime.getRuntime().exec(new String[]{m_shellPath, "-c", cde}, 
													(String[])_envParams.toArray(new String[]{}), 
													_workDir);
			// TODO: store LocalJobProcess
			return uniqId;
		} catch (IOException ioe) {
			//LocalAdaptorAbstract.sessionMap.put(jobId, new LocalJobProcess(ioe.getMessage()));
			/*try {
				LocalAdaptorAbstract.store(new LocalJobProcess(ioe.getMessage()), jobId);
			} catch (IOException e1) {
				throw new NoSuccessException(e1);
			}*/
			// 
			FileOutputStream endcode;
			try {
				endcode = new FileOutputStream(new File(getEndcodeFile(uniqId)));
				endcode.write('2');
				endcode.write('\n');
				endcode.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileOutputStream error;
			try {
				error = new FileOutputStream(new File(getStderrFile(uniqId)));
				error.write(ioe.getMessage().getBytes());
		        error.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO: set stdout and stderr to LocalJobprocess and store it
			return uniqId;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    	
    }

    public String submit(String commandLine, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		File input = new File(getStdinFile(uniqId));
        OutputStream out;
		try {
			new File(getStdinFile(uniqId)).createNewFile();
			// TODO set stdin to LocalJobProcess ????
		} catch (IOException e) {
			throw new NoSuccessException("Standard input could not be written to local file: " + input);
		}
    	return this.submit(commandLine, uniqId/*, false*/);
	}

    /*
	public JobIOHandler submit(String commandLine, boolean checkMatch,
			String uniqId, InputStream stdin) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		String jobId = UUID.randomUUID().toString();
		File input = new File(getStdinFile(uniqId));
        OutputStream out;
		try {
			out = new FileOutputStream(input);
			if (stdin != null) {
				int n;
				byte[] buffer = new byte[1024];
				while ((n = stdin.read(buffer)) != -1) {
					out.write(buffer, 0, n);
				}
			} else  {
				// TODO check this
				out.write(' ');
			}
	        out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new NoSuccessException("Standard input could not be written to local file: " + input);
		}
		// TODO create stdout and stderr
		try {
			new File(getStdoutFile(jobId)).createNewFile();
			new File(getStderrFile(jobId)).createNewFile();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
		jobId = this.submit(commandLine, jobId, true);
		return new LocalJobIOHandler(jobId);
	}
	*/

    /*
	public JobIOGetterInteractive submitInteractive(String commandLine,
			boolean checkMatch) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		
		String jobId = this.submit(commandLine, UUID.randomUUID().toString(), true);
		// TODO create stdout and stderr
		try {
			new File(getStdoutFile(jobId)).createNewFile();
			new File(getStderrFile(jobId)).createNewFile();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
		return new LocalJobIOHandler(jobId);
		
	}
	*/
    
	private String prepareCde(String commandLine, String jobId) {
		// TODO xsl
		return 	"eval '" + commandLine + " < " + getStdinFile(jobId) +
			" > " + getStdoutFile(jobId) + 
			" 2> " + getStderrFile(jobId) + "&' ; " +
			"MYPID=$! ; " +
			"echo $MYPID > " + getPidFile(jobId) + " ;" +
			"wait $MYPID;" +
			"ENDCODE=$?;" +
			"echo $ENDCODE > " + getEndcodeFile(jobId) + " ;" +
			"/bin/rm -f "+ getPidFile(jobId) + ";"  +
			"exit $ENDCODE;";
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
		try {			
			String cde = m_shellPath+" -c 'echo \"MYPID=`cat " + getPidFile(nativeJobId) + "`; kill $MYPID ;'";
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
		//Process p = (Process) LocalAdaptorAbstract.sessionMap.get(nativeJobId);
		/*Process p;
		try {
			p = LocalAdaptorAbstract.restore(nativeJobId);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccessException(e);
		}
		*/
		//p.destroy();
		//LocalAdaptorAbstract.sessionMap.remove(p);
		// TODO get files from LocalJobprocess
		new File(getPidFile(nativeJobId)).delete();
		new File(getStdinFile(nativeJobId)).delete();
		new File(getStdoutFile(nativeJobId)).delete();
		new File(getStderrFile(nativeJobId)).delete();
		new File(getEndcodeFile(nativeJobId)).delete();
	}

}
