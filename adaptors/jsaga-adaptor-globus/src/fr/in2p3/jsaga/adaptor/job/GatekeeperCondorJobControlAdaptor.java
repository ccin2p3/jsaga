package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.log4j.Logger;
import org.globus.io.gass.server.GassServer;
import org.globus.rsl.*;
import org.ogf.saga.error.*;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/*
 * Aurelien Croc (CEA Saclay, FRANCE)
 * May, 1st 2010
 *
 * LCGCE+ Job Control adaptor
 *
 * This LCGCE adaptor provides the upload of the standard input stream for each
 * job through a unique GASS server loaded once.
 */

public class GatekeeperCondorJobControlAdaptor extends GkCommonJobControlAdaptor implements JobControlAdaptor, CleanableJobAdaptor {
    private Logger logger = Logger.getLogger(GatekeeperCondorJobControlAdaptor.class);

    /** override super.getType() */
    public String getType() {
        return "gatekeeper-condor";
    }

    /** override super.getUsage() */
    public Usage getUsage() {
        return new UAnd.Builder()
                .and(new UOptional(IP_ADDRESS))
                .and(new UOptional(TCP_PORT_RANGE))
                .build();
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
        return new GatekeeperCondorJobMonitorAdaptor();
    }

    protected String submit(RslNode rslTree, boolean checkMatch, boolean isInteractive) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        String gassURL;

        if (rslTree.getParam("stdin") != null && rslTree.getParam("stdin").
                getValues().size() > 0) {
            String inputFilename;
            Bindings subst;
            File inputFile;

            // Start the GASS server if it's not started yet
            try {
                gassURL = startGassServer();
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }

            // Check the validity of the input file and get its absolute path
            inputFilename = rslTree.getParam("stdin").getValues().get(0).
                toString();
            inputFile = new File(inputFilename);
            if (!(inputFile.isFile()))
                throw new NoSuccessException("The input file " + inputFilename +
                    " is not accessible");
            // Bug workaround?? The first slash is eaten by the Gass server for
            // unknown reason!
            inputFilename = "/" + inputFile.getAbsolutePath();

            // Update the RSL to use the GASS server to access to the input file
            subst = new Bindings("rsl_substitution");
            subst.add(new Binding("GLOBUSRUN_GASS_URL", gassURL));
            rslTree.add(subst);
            NameOpValue stdinUrl = new NameOpValue("stdin", NameOpValue.EQ,
                new VarRef("GLOBUSRUN_GASS_URL", null,
                new Value(inputFilename)));
            rslTree.add(stdinUrl);

        }

        // Submit the job
        return super.submit(rslTree, checkMatch, isInteractive);
    }

    private String startGassServer() throws Exception {
        GassServer gassServer;

        try {
            gassServer = GassServerFactory.getGassServer(m_credential);
            gassServer.registerDefaultDeactivator();
        } catch (Exception e) {
            throw new Exception("Problems while creating a Gass Server", e);
        }
        String gassURL = gassServer.getURL();
        logger.debug("Started the GASS server");
        return gassURL;
    }

}
