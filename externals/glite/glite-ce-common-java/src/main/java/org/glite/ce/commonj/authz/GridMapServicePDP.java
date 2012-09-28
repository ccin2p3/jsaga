/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 */

package org.glite.ce.commonj.authz;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;
import org.glite.security.util.X500Principal;

public class GridMapServicePDP implements ServicePDP {
    private static final Logger logger = Logger.getLogger(GridMapServicePDP.class.getName());
    private static final Pattern mapFilePattern = Pattern.compile("^\\s*\"(/[^=/\"]+=[^/\"]+/[^\"]+)\"\\s+([^\\s]+)\\s*$");

    public static final String GRID_MAP_FILE = "gridMapFile";

    private String id;
    private String gridMapFile;
    private HashMap<String, String> dnTable;
    private long timestamp;

    public GridMapServicePDP() {
        this("undef");
    }

    public GridMapServicePDP(String id) {
        this.id = id;
        timestamp = 0;
        dnTable = new HashMap<String, String>(0);
    }

    public void initialize(ChainConfig config, String name, String id) throws InitializeException {
        String mapFile = (String) config.getProperty(name, GRID_MAP_FILE);
        
        if (mapFile == null) {
            logger.error("Gridmap file not specified");
            throw new InitializeException("Gridmap file not specified");
        }

        readGridmapFile(mapFile);
        gridMapFile = mapFile;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setProperty(String name, String value) throws InitializeException {
        if (name.equals(GRID_MAP_FILE)) {
            readGridmapFile(value);
            gridMapFile = value;
        }
    }

    public String getProperty(String name) {
        if (name.equals(GRID_MAP_FILE)) {
            return gridMapFile;
        }
        return null;
    }

    public String[] getProperties() {
        return new String[] { GRID_MAP_FILE };
    }

    public boolean isTriggerable(String name) {
        return name.equals(GRID_MAP_FILE);
    }

    public void readGridmapFile(String mapFile) throws InitializeException {
        logger.debug("Initializing gridmap service PDP with " + mapFile + "(" + this.hashCode() + ")");

        dnTable.clear();
        timestamp = System.currentTimeMillis();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mapFile));
            
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher matcher = mapFilePattern.matcher(line);
                
                if (matcher.matches()) {
                    String oldValue = dnTable.put(matcher.group(1), matcher.group(2));
                    
                    if (oldValue != null) {
                        logger.warn("Replaced value for " + matcher.group(1) + ": " + oldValue);
                    }
                    logger.debug("Registered DN: " + matcher.group(1) + "(" + this.hashCode() + ")");
                }
            }
        } catch (IOException ioEx) {
            logger.error(ioEx.getMessage(), ioEx);
            throw new InitializeException(ioEx.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public int getPermissionLevel(Subject peerSubject, MessageContext context, QName operation) throws AuthorizationException {
        Set<X500Principal> pSet = peerSubject.getPrincipals(X500Principal.class);
        
        if (pSet == null) {
            logger.warn("Cannot authorize: missing X500Principal in subject");
            return NO_DECISION;
        }

        for(X500Principal principal : pSet) {
            String identity = principal.getName();
            
            logger.debug("Checking identity: " + identity);
            
            if (dnTable.get(identity) != null) {
                logger.info("Identity authorized: " + identity + "(" + this.hashCode() + ")");
                return STRONG_ALLOWED;
            }
            
        }

        return DENIED;
    }

    public void close() throws CloseException {
    }

    public Object clone() {
        GridMapServicePDP result = new GridMapServicePDP(this.id);
        result.gridMapFile = this.gridMapFile;
        result.timestamp = this.timestamp;
        result.dnTable = (HashMap<String, String>) this.dnTable.clone();
        logger.debug("Cloned " + this.hashCode() + " in " + result.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GridMapServicePDP)) {
            return false;
        }
        
        GridMapServicePDP pdp = (GridMapServicePDP) obj;
        return (pdp.gridMapFile.equals(this.gridMapFile) && pdp.id.equals(this.id) && pdp.timestamp == this.timestamp);
    }

//    public static void main(String[] args) {
//        try {
//            org.apache.log4j.PropertyConfigurator.configure("./log4j.properties");
//
//            GridMapServicePDP pdp = new GridMapServicePDP();
//            pdp.readGridmapFile(args[0]);
//
//            Subject sbj = new Subject();
//            X500Principal tmpp = new X500Principal();
//            tmpp.setName(org.glite.security.util.DNHandler.getDN(args[1]));
//            sbj.getPrincipals().add(tmpp);
//
//            System.out.println("Permission level: " + pdp.getPermissionLevel(sbj, null, null));
//
//        } catch (Throwable th) {
//            th.printStackTrace();
//        }
//    }
}
