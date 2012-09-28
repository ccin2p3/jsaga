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

package org.glite.ce.creamapi.jobmanagement.cmdexecutor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;

import org.glite.ce.common.db.DatabaseException;
import org.glite.ce.creamapi.eventmanagement.Event;
import org.glite.ce.creamapi.eventmanagement.EventManagerException;
import org.glite.ce.creamapi.eventmanagement.EventManagerInterface;
import org.glite.ce.creamapi.jobmanagement.db.JobDBInterface;

public class JobStatusEventManager implements EventManagerInterface {
	
	public final static String TYPE_PROPERTYNAME           = "type";
	public final static String EXIT_CODE_PROPERTYNAME      = "exitCode";
	public final static String FAILURE_REASON_PROPERTYNAME = "failureReason";
	public final static String DESCRIPTION_PROPERTYNAME    = "description";
	public final static String JOB_ID_PROPERTYNAME         = "jobId";
	public final static String GRID_JOB_ID_PROPERTYNAME    = "gridJobId";
	public final static String WORKER_NODE_PROPERTYNAME    = "workerNode";
	
	public static final String MANAGER_TYPE = "JOBSTATUS";
	
    private static final Logger logger = Logger.getLogger(JobStatusEventManager.class.getName());
	
    private JobDBInterface jobDB = null;
    private int maxEvents = 1;
    
    public JobStatusEventManager(JobDBInterface jobDB, int maxEvents) {
    	if (jobDB == null){
    		throw new IllegalArgumentException("JobStatusEventManager: parameter must be not null!");
    	}
    	this.jobDB = jobDB;
    	this.maxEvents = maxEvents;
    	logger.debug("maxEvents = " + maxEvents);
    }

	public void deleteEvent(String eventId, String userId) throws IllegalArgumentException, EventManagerException {
		logger.warn("Method not implemented!");
		throw new EventManagerException("Method not implemented!");
	}

	public void deleteEvents(String userId) throws IllegalArgumentException, EventManagerException {
		logger.warn("Method not implemented!");
		throw new EventManagerException("Method not implemented!");
	}

	public Event getEvent(String eventId, String userId) throws IllegalArgumentException, EventManagerException {
        Event event = null;
		if (eventId == null){
			throw new IllegalArgumentException("paramenter eventId must be not null!");
		}
		List<Event> eventList = null;
		try{
			eventList = jobDB.retrieveJobStatusAsEvent(eventId, null, null, null, 1, userId);
		} catch (DatabaseException de){
			logger.error("Error retrieving events fom database: " + de.getMessage());
			throw new EventManagerException("Error retrieving events fom database: " + de.getMessage());
		}
		if (eventList.size() != 0) {  
		  event = eventList.get(0);
		}
		return event;
	}

	public List<Event> getEvents(String fromEventId, String toEventId, Calendar fromDate, Calendar toDate, int maxEvents, String userId) throws IllegalArgumentException, EventManagerException {
		List<Event> eventList = new ArrayList<Event>(0);
		try{
			eventList = jobDB.retrieveJobStatusAsEvent(fromEventId, toEventId, fromDate, toDate, Math.min(maxEvents, this.maxEvents), userId);
		} catch (DatabaseException de){
			logger.error("Error retrieving events fom database: " + de.getMessage());
			throw new EventManagerException("Error retrieving events fom database: " + de.getMessage());
		}
		return eventList;
	}

	public void insertEvent(Event event, String userId) throws IllegalArgumentException, EventManagerException {
		logger.warn("Method not implemented!");
		throw new EventManagerException("Method not implemented!");
	}
    

}
