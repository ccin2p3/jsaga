package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.log4j.Logger;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.rsl.*;
import org.ogf.saga.error.*;

import java.io.*;
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
public class GatekeeperJobControlAdaptor extends GkCommonJobControlAdaptor implements JobControlAdaptor, CleanableJobAdaptor, StreamableJobInteractiveSet {
	private static final String SHELLPATH = "ShellPath";
    private Logger logger = Logger.getLogger(GatekeeperJobControlAdaptor.class);

    /** override super.getType() */
    public String getType() {
        return "gatekeeper";
    }

    /** override super.getUsage() */
    public Usage getUsage() {
        return new UAnd(new Usage[] {
        		new UOptional(SHELLPATH),
        		new UOptional(IP_ADDRESS),
        		new UOptional(TCP_PORT_RANGE)});
    }

    /** override super.getDefaults() */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	try {
			String defaultIp = InetAddress.getLocalHost().getHostAddress();
			String defaultTcpPortRange="40000,45000";
	    	return new Default[]{new Default(IP_ADDRESS, defaultIp),new Default(TCP_PORT_RANGE, defaultTcpPortRange)};
		} catch (UnknownHostException e) {
			return null;
		}
    }

    /** override super.getDefaultJobMonitor() */
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new GatekeeperJobMonitorAdaptor();
    }

    public String submitInteractive(String jobDesc, boolean checkMatch, InputStream stdin, OutputStream stdout, OutputStream stderr) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        RslNode rslTree;
        String gassURL;
        try {
            rslTree = RSLParser.parse(jobDesc);
            gassURL = startGassServer(stdout, stderr);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        // update RSL
        Bindings subst = new Bindings("rsl_substitution");
        subst.add(new Binding("GLOBUSRUN_GASS_URL", gassURL));
        rslTree.add(subst);
        if (stdin != null) {
            File stdinFile;
            try{stdinFile=File.createTempFile("stdin-",".txt",new File("./"));} catch(IOException e){throw new NoSuccessException(e);}
            //todo: remove stdinFile on cleanup() instead of on exit
            stdinFile.deleteOnExit();
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
        return super.submit(rslTree, checkMatch, true);
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

    private static void save(InputStream in, File file) throws NoSuccessException {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            for (int len; (len=in.read(buffer))>0; ) {
                out.write(buffer, 0, len);
            }
            out.close();
        } catch(IOException e) {
            throw new NoSuccessException(e);
        }
    }
}
