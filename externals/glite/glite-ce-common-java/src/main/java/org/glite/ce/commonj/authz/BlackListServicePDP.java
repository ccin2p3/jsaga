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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import org.glite.security.util.X500Principal;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;

public class BlackListServicePDP implements ServicePDP {
	
    private static Logger logger = Logger.getLogger(BlackListServicePDP.class.getName());
	
    public static final String BLACK_LIST_FILE = "blackListFile";
	
    private String id;
    private String blackListFile;
    private HashSet<String> dnTable;
    private long timestamp;
	
    public BlackListServicePDP(){
	this.id = "undef";
	timestamp = 0;
    }
	
    public BlackListServicePDP(String id){
	this.id = id;
	timestamp = 0;
    }
	
    public void initialize(ChainConfig config, String name, String id) throws InitializeException {
		
	String bannerFile = (String)config.getProperty(name, BLACK_LIST_FILE);
        if (bannerFile == null) {
	    logger.error("Blacklist file not specified");
            throw new InitializeException("Blacklist file not specified");
        }
        
        readBannerFile(bannerFile);
        blackListFile = bannerFile;
        this.id = id;
        
    }
	
    public String getId(){
	return id;
    }
	
    public void setProperty(String name, String value) throws InitializeException{
        if( name.equals(BLACK_LIST_FILE) ){
            readBannerFile(value);
            blackListFile = value;
        }
    }
    
    public String getProperty(String name){
        if( name.equals(BLACK_LIST_FILE) ){
            return blackListFile;
        }
        return null;
    }
    
    public String[] getProperties(){
        return new String[]{ BLACK_LIST_FILE };
    }
    
    public boolean isTriggerable(String name){
        return name.equals(BLACK_LIST_FILE);
    }
    	
    private void readBannerFile(String bannerFile) throws InitializeException {
                
        logger.debug("Initializing blacklist service PDP with " + bannerFile);
        
	dnTable = new HashSet<String>();
	timestamp = System.currentTimeMillis();
		
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(bannerFile));
            String line = reader.readLine();
            while( line != null ) {
                line = line.trim();

                if( line.length()>0 && !line.startsWith("#") ){
		    
		    if( line.startsWith("\"") )
			line = line.substring(1);
		    if( line.endsWith("\"") )
			line = line.substring(0,line.length()-1);
                    dnTable.add(line);
                    logger.debug("Registered DN: " + line);
                }
                line = reader.readLine();
            }
        }catch(IOException ioEx){
	    logger.error(ioEx.getMessage(), ioEx);
	    throw new InitializeException(ioEx.getMessage());
        }finally{
	    if( reader!=null ) {
                try { reader.close(); } catch (Exception ex) {}
            }
        }
        
    }

    public int getPermissionLevel(Subject peerSubject,
                                  MessageContext context,
                                  QName operation) throws AuthorizationException {
		
	Set<X500Principal> pSet = peerSubject.getPrincipals(X500Principal.class);
	if( pSet==null ){
	    logger.warn("Cannot authorize: missing X500Principal in subject");
	    return NO_DECISION;
	}
		
	Iterator<X500Principal> allPrincipals = pSet.iterator();
	while( allPrincipals.hasNext() ){
	    String identity = allPrincipals.next().getName();
	    logger.debug("Checking identity: " + identity);
	    if( dnTable.contains(identity) ){
		logger.info("Identity banned: " + identity);
		return STRONG_DENIED;
	    }
	}

	return NO_DECISION;
    }

    public void close() throws CloseException {
    }
	
    public Object clone(){
	BlackListServicePDP result = new BlackListServicePDP(this.id);
	result.id = this.id;
	result.blackListFile = this.blackListFile;
	result.timestamp = this.timestamp;
	result.dnTable = (HashSet<String>) this.dnTable.clone();
	return result;
    }
	
    public boolean equals(Object obj){
	if( !(obj instanceof BlackListServicePDP) )
	    return false;
	BlackListServicePDP pdp = (BlackListServicePDP)obj;
	return (pdp.blackListFile.equals(this.blackListFile)
		&& pdp.id.equals(this.id)
		&& pdp.timestamp==this.timestamp);
    }
	
}
