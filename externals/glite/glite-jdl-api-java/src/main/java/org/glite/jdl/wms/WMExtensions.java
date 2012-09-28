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
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 * Version info: $Id: WMExtensions.java,v 1.2 2006/10/26 14:52:49 pandreet Exp $
 */

package org.glite.jdl.wms;

import org.glite.jdl.JobAdException;

interface WMExtensions {

    /**
       Return the retry count
       @return the retry count
    */
    public int getRetryCount() throws JobAdException;

    public void setRetryCount(int count);

    public int getShallowRetryCount() throws JobAdException;

    public void setShallowRetryCount(int count);

    public int getExpiryTime()  throws JobAdException;

    public void setExpiryTime(int expTime);

}
