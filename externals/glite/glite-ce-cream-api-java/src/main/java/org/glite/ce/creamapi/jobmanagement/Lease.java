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

package org.glite.ce.creamapi.jobmanagement;

import java.util.Calendar;

public class Lease {

	private String    leaseId   = null;
	private Calendar  leaseTime = null;
	private String    userId    = null;

    public Lease() {
    }
    
	public Lease(String leaseId, String userId, Calendar leaseTime) {
	    this.leaseId = leaseId;
	    this.userId = userId;
	    this.leaseTime = leaseTime;
	}

	public String getLeaseId() {
		return leaseId;
	}
	public void setLeaseId(String leaseId) {
		this.leaseId = leaseId;
	}
	public Calendar getLeaseTime() {
		return leaseTime;
	}
	public void setLeaseTime(Calendar leaseTime) {
		this.leaseTime = leaseTime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public boolean isExpired(){
		boolean leaseTimeExpired = false;
        Calendar now = Calendar.getInstance();

        if ((leaseTime != null) && (leaseTime.before(now))) {
        	leaseTimeExpired = true;
        }
        return leaseTimeExpired;
	}
}
