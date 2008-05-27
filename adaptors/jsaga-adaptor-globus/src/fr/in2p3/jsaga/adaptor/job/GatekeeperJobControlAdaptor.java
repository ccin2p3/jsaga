package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.log4j.Logger;
import org.globus.gram.*;
import org.globus.gram.internal.GRAMConstants;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.rsl.*;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;

import java.io.*;
import java.lang.Exception;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GatekeeperJobControlAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 * TODO: remove stdin file and stop gass server when cleanup
 */
public class GatekeeperJobControlAdaptor extends GatekeeperJobAdaptorAbstract implements JobControlAdaptor, CleanableJobAdaptor, StreamableJobInteractiveSet {
	private static final String SHELLPATH = "ShellPath";
    private Logger logger = Logger.getLogger(GatekeeperJobControlAdaptor.class);
    private Map m_parameters;
    private boolean twoPhaseUsed = false;    
    
    public Usage getUsage() {
        return new UAnd(new Usage[] {
        		new UOptional(SHELLPATH),
        		new UOptional(IP_ADDRESS)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	try {
			String defaultIp = InetAddress.getLocalHost().getHostAddress();
	    	return new Default[]{new Default(IP_ADDRESS, defaultIp)};
		} catch (UnknownHostException e) {
			return null;
		}
    }

    public String[] getSupportedSandboxProtocols() {
        return null;    // no sandbox management
    }

    public String getTranslator() {
        return "xsl/job/rsl-1.0.xsl";
    }

    public Map getTranslatorParameters() {
        return m_parameters;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new GatekeeperJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        super.connect(userInfo, host, port, basePath, attributes);
        m_parameters = attributes;
    }

    public void disconnect() throws NoSuccess {
        super.disconnect();
        m_parameters = null;
    }

    public String submit(String jobDesc, boolean checkMatch)  throws PermissionDenied, Timeout, NoSuccess, BadResource {
    	RslNode rslTree;
        try {
        	rslTree = RSLParser.parse(jobDesc);
        } catch (ParseException e) {
            throw new NoSuccess(e);
        }
        return submit(rslTree, checkMatch, false);
    }

    public String submitInteractive(String jobDesc, boolean checkMatch, InputStream stdin, OutputStream stdout, OutputStream stderr) throws PermissionDenied, Timeout, NoSuccess {
        RslNode rslTree;
        String gassURL;
        try {
            rslTree = RSLParser.parse(jobDesc);
            gassURL = startGassServer(stdout, stderr);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        // update RSL
        Bindings subst = new Bindings("rsl_substitution");
        subst.add(new Binding("GLOBUSRUN_GASS_URL", gassURL));
        rslTree.add(subst);
        if (stdin != null) {
            File stdinFile;
            try{stdinFile=File.createTempFile("stdin-",".txt",new File("./"));} catch(IOException e){throw new NoSuccess(e);}
            save(stdin, stdinFile);
            NameOpValue stdinUrl = new NameOpValue("stdin", NameOpValue.EQ,
                    new VarRef("GLOBUSRUN_GASS_URL", null, new Value("/"+stdinFile.getName())));
            rslTree.add(stdinUrl);
        }
        NameOpValue stdoutUrl = new NameOpValue("stdout", NameOpValue.EQ,
                new VarRef("GLOBUSRUN_GASS_URL", null, new Value("/dev/stdout-rgs")));
        rslTree.add(stdoutUrl);
        NameOpValue stderrUrl = new NameOpValue("stderr", NameOpValue.EQ,
                new VarRef("GLOBUSRUN_GASS_URL", null, new Value("/dev/stderr-rgs")));
        rslTree.add(stderrUrl);
        return this.submit(rslTree, checkMatch, true);
    }
    
    private String submit(RslNode rslTree, boolean checkMatch, boolean isInteractive) throws PermissionDenied, Timeout, NoSuccess, BadResource {
        if(checkMatch) {
			logger.debug("CheckMatch not supported");
		}
        GramJob job = new GramJob(m_credential,rslTree.toRSL(true));
        try {
        	try {
            	Gram.request(m_serverUrl, job, isInteractive);
            	logger.warn("'two_phase' attribute was ignored.");
        	}
        	catch (WaitingForCommitException e) {
        		// send signal to start job
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		job.signal(GRAMConstants.SIGNAL_COMMIT_REQUEST);
        		twoPhaseUsed = true;        		
        	}
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
        return job.getIDAsString();
    }

    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        GramJob job = getGramJobById(nativeJobId);
        try {
            job.cancel();
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
    }

    /*public boolean suspend(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        return this.signal(nativeJobId, GRAMConstants.SIGNAL_SUSPEND);
    }

    public boolean resume(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        return this.signal(nativeJobId, GRAMConstants.SIGNAL_RESUME);
    }

    public boolean signal(String nativeJobId, int signal) throws PermissionDenied, Timeout, NoSuccess {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }
        int errorCode = -1;
        try {
            errorCode = job.signal(signal);
        } catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        }
        return errorCode==0;
    }*/

    private void rethrowException(GramException e) throws PermissionDenied, Timeout, NoSuccess , BadResource{
    	switch(e.getErrorCode()) {
    		case GRAMProtocolErrorConstants.BAD_DIRECTORY:
    			throw new BadResource(e);
            case GRAMProtocolErrorConstants.ERROR_AUTHORIZATION:
                throw new PermissionDenied(e);
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new Timeout(e);
            default:
                throw new NoSuccess(e);
        }
    }

	public void clean(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
		try {
			if(twoPhaseUsed ) {
				GramJob job = getGramJobById(nativeJobId);			
				// Send signal to clean jobmanager
				job.signal(GRAMConstants.SIGNAL_COMMIT_END);
			}
		} catch (GramException e) {
			throw new NoSuccess("Unable to send commit end signal", e);
		} catch (GSSException e) {
			throw new NoSuccess("Unable to send commit end signal", e);
		}
	}

    private String startGassServer(OutputStream stdout, OutputStream stderr) throws Exception {
        GassServer gassServer;
        try {
            gassServer = GassServerFactory.getGassServer(m_credential);
            gassServer.registerDefaultDeactivator();
        } catch (Exception e) {
            throw new Exception("Problems while creating a Gass Server", e);
        }
        String gassURL = gassServer.getURL();
        gassServer.registerJobOutputStream("out-rgs", new JobOutputStream(new GatekeeperJobOutputListener(stdout)));
        gassServer.registerJobOutputStream("err-rgs", new JobOutputStream(new GatekeeperJobOutputListener(stderr)));
        logger.debug("Started the GASS server");
        return gassURL;
    }

    private static void save(InputStream in, File file) throws NoSuccess {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            for (int len; (len=in.read(buffer))>0; ) {
                out.write(buffer, 0, len);
            }
            out.close();
        } catch(IOException e) {
            throw new NoSuccess(e);
        }
    }
}
