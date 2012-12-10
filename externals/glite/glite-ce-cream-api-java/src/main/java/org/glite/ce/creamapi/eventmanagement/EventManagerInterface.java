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
 * Authors: Eric Frizziero (eric.frizziero@pd.infn.it), Luigi Zangrando (luigi.zangrando@pd.infn.it) 
 */

package org.glite.ce.creamapi.eventmanagement;

import java.util.Calendar;
import java.util.List;

public interface EventManagerInterface {
    public void insertEvent(Event event, String userId) throws IllegalArgumentException, EventManagerException;
    public void deleteEvent(String eventId, String userId) throws IllegalArgumentException, EventManagerException;
    public void deleteEvents(String userId) throws IllegalArgumentException, EventManagerException;
    public Event getEvent(String eventId, String userId) throws IllegalArgumentException, EventManagerException;
    public List<Event> getEvents(String fromEventId, String toEventId, Calendar fromDate, Calendar toDate, int maxEvents, String userId) throws IllegalArgumentException, EventManagerException;
}
