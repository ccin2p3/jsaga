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
 * 
 * Authors: Eric Frizziero (eric.frizziero@pd.infn.it) 
 */

package org.glite.ce.creamapi.eventmanagement;

import java.util.Hashtable;

import org.apache.log4j.Logger;

public class EventManagerFactory {
	
	private static final Logger logger = Logger.getLogger(EventManagerFactory.class.getName());
	private static Hashtable<String, EventManagerInterface> eventManagers = new Hashtable<String, EventManagerInterface>(0);
	
	public static EventManagerInterface getEventManager(String type) throws IllegalArgumentException {
		if ((type == null) || "".equals(type)){
			throw new IllegalArgumentException("Parameter type must be not empty or null!");
		}
		return eventManagers.get(type);
	}

   public static void addEventManager(String type, EventManagerInterface eventManager) throws IllegalArgumentException {
		if ((type == null) || "".equals(type)){
			throw new IllegalArgumentException("Parameter type must be not empty or null!");
		}
	    if (eventManager == null){
	    	throw new IllegalArgumentException("Parameter eventManager must be not null");
	    }
	    if (eventManagers.containsKey(type)) {
	    	logger.error("EventManager for type= " + type + " already exists!");
	    	throw new IllegalArgumentException("EventManager for type= " + type + " already exists!");
	    }
	    eventManagers.put(type, eventManager);
	    logger.info("Added eventManager for type= " + type);
   }
   
}
