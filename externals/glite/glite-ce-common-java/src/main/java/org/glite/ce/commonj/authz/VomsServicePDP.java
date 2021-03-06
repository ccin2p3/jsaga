/*
 * Copyright (c) Members of the EGEE Collaboration. 2004. 
 * See http://www.eu-egee.org/partners/ for details on the copyright
 * holders.  
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 */

package org.glite.ce.commonj.authz;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;
import org.glite.ce.commonj.Constants;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;

public class VomsServicePDP implements ServicePDP {
    private static final Logger logger = Logger.getLogger(VomsServicePDP.class.getName());
    private static final Pattern mapFilePattern = Pattern.compile("^\\s*\"((/[^=/\"]+)+(/[^/\"]+)*)\"\\s+([^\\s]+)\\s*$");

    public static final String GRID_MAP_FILE = "gridMapFile";

    private String id;
    private String gridMapFile;
    private HashMap<FQANPattern, String> fqanTable;
    private long timestamp;

    public VomsServicePDP() {
        this("undef");
    }

    public VomsServicePDP(String id) {
        this.id = id;
        timestamp = 0;
        fqanTable = new HashMap<FQANPattern, String>(0);
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
        logger.debug("Initializing gridmap service PDP with " + mapFile);

        fqanTable.clear();
        timestamp = System.currentTimeMillis();
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mapFile));
            
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher matcher = mapFilePattern.matcher(line);
                
                if (matcher.matches()) {
                    try {
                        FQANPattern fqanPattern = new FQANPattern(matcher.group(1));
                        String oldValue = fqanTable.put(fqanPattern, matcher.group(2));
                        logger.debug("Registered \"" + fqanPattern + "\"");

                        if (oldValue != null) {
                            logger.warn("Replaced value for " + matcher.group(1) + ": " + oldValue);
                        }
                    } catch (IllegalArgumentException ex) {
                        logger.debug(ex.getMessage());
                    }
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

    private int checkList(List<VOMSAttribute> vomsList) {
        if(vomsList != null) {
            for(VOMSAttribute vomsAttribute : vomsList) {            
                List<String> fqanList = (List<String>) vomsAttribute.getFullyQualifiedAttributes();

                for(String fqanStr : fqanList) {
                    for(FQANPattern pattern : fqanTable.keySet()) {
                        logger.debug("Checking fqan: " + fqanStr + " against " + pattern.toString());

                        if (pattern.matches(fqanStr)) {
                            logger.info("VOMS attribute authorized: " + fqanStr);
                            return ALLOWED;
                        }
                    }
                }
            }
        }

        return DENIED;
    }

    private void logMessageForFQAN(List<VOMSAttribute> vomsList, String msg) {
        if(logger.isDebugEnabled() && vomsList != null && msg != null) {
            logger.debug(msg + " with fqan list [ ");
            
            for(VOMSAttribute vomsAttribute : vomsList) {
                List<String> fqanList = (List<String>) vomsAttribute.getFullyQualifiedAttributes();
                
                for(String fqanStr : fqanList) {
                    logger.debug(fqanStr + "; ");                    
                }
            }
            logger.debug("]");
        }
    }

    public int getPermissionLevel(Subject peerSubject, MessageContext context, QName operation) throws AuthorizationException {
        List<VOMSAttribute> vomsList = (List<VOMSAttribute>) context.getProperty(Constants.USER_VOMSATTRS_LABEL);

        if (vomsList != null && vomsList.size() > 0) {
            logger.debug("Found VOMS attribute list in the message context");
            return checkList(vomsList);
        }

        logger.debug("No VOMS attribute list in the message context, trying to parse proxy");

        Set<Object> certChainSet = peerSubject.getPublicCredentials();
        
        for(Object tmpo : certChainSet) {            
            if (tmpo instanceof X509Certificate[]) {
                X509Certificate[] certChain = (X509Certificate[]) tmpo;
                
                VOMSValidator vv = new VOMSValidator(certChain, AuthZContextListener.getACValidator()).validate();
                
                vomsList = (List<VOMSAttribute>) vv.getVOMSAttributes();
                
                if (vomsList != null && vomsList.size() > 0 && checkList(vomsList) == ALLOWED) {
                    /*
                     * TODO remove NULL from VOMSAttribute
                     */
                    context.setProperty(Constants.USER_VOMSATTRS_LABEL, vomsList);
                    return ALLOWED;
                }
            }
        }

        if (vomsList != null && vomsList.size() > 0) {
            logMessageForFQAN(vomsList, "Authorization failed");
        }

        return DENIED;
    }

    public void close() throws CloseException {
        fqanTable.clear();
    }

    public Object clone() {
        VomsServicePDP result = new VomsServicePDP(this.id);
        result.gridMapFile = this.gridMapFile;
        result.timestamp = this.timestamp;
        result.fqanTable = (HashMap<FQANPattern, String>) this.fqanTable.clone();
        return result;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof VomsServicePDP)) {
            return false;
        }
        
        VomsServicePDP pdp = (VomsServicePDP) obj;
        return (pdp.gridMapFile.equals(this.gridMapFile) && pdp.id.equals(this.id) && pdp.timestamp == this.timestamp);
    }

}
