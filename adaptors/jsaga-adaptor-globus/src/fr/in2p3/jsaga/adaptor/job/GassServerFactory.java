// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package fr.in2p3.jsaga.adaptor.job;

import org.globus.common.CoGProperties;
import org.globus.io.gass.server.GassServer;
import org.ietf.jgss.GSSCredential;

import java.util.*;

public class GassServerFactory {
    private static Map mapping = new HashMap();
    private static Map count = new HashMap();
    private static String cogIP = CoGProperties.getDefault().getIPAddress();

    public synchronized static GassServer getGassServer(GSSCredential credential) throws Exception {
        if (cogIP == null) {
            if (CoGProperties.getDefault().getIPAddress() == null) {
                throw new Exception("Could not determine this host's IP address. Please set an IP address in cog.properties");
            } else {
                cogIP = CoGProperties.getDefault().getIPAddress();
            }
        } else if (!cogIP.equalsIgnoreCase(CoGProperties.getDefault().getIPAddress())) {
            cogIP = CoGProperties.getDefault().getIPAddress();
            shutdownGassServers();
        }
        GassServer server;
        if (mapping.containsKey(credential)) {
            server = (GassServer) mapping.get(credential);
        } else {
            try {
                server = new GassServer(credential, 0);
            } catch (Exception e) {
                throw new Exception("Cannot start a gass server", e);
            }
            mapping.put(credential, server);
        }
        increaseUsageCount(server);
        return server;
    }

    private static synchronized void increaseUsageCount(GassServer server) {
        Integer i = (Integer) count.get(server);
        if (i == null) {
            i = new Integer(1);
        } else {
            i = new Integer(i.intValue() + 1);
        }
        count.put(server, i);
    }

    public static synchronized void decreaseUsageCount(GassServer server) {
        Integer i = (Integer) count.get(server);
        if (i == null) {
            throw new IllegalStateException("No registered usage for server ("
                    + server + ")");
        } else if (i.intValue() == 1) {
            count.remove(server);
            mapping.remove(server.getCredentials());
            server.shutdown();
        } else {
            count.put(server, new Integer(i.intValue() - 1));
        }
    }

    private static synchronized void shutdownGassServers() {
        Iterator iterator = mapping.values().iterator();
        while (iterator.hasNext()) {
            GassServer gs = (GassServer) iterator.next();
            gs.shutdown();
        }
        mapping.clear();
        count.clear();
    }
}
