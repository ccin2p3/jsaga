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
 * Authors: Luigi Zangrando, <luigi.zangrando@pd.infn.it>
 *
 * Version info: $Id: CacheEntry.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import org.glite.ce.commonj.CEResource;

public class CacheEntry implements Comparable {
    private long timestamp = 0;
  //  private int size = 0;
    private CEResource ceResource;



    public CacheEntry(long timestamp, CEResource resource) throws Exception {
        if(timestamp < 0) {
            throw (new Exception("The timestamp value must be > 0"));
        }

        if(resource == null) {
            throw (new Exception("The resource obj must be not null"));
        }

        this.timestamp = timestamp;
        this.ceResource = resource;
    //    this.size = resource.toString().getBytes().length;
    }



    public CEResource getCEResource() {
        return ceResource;
    }



    public int getSize() {
       // return size;
        return ceResource.toString().getBytes().length;
    }



    public long getTimestamp() {
        return timestamp;
    }



    public int compareTo(Object obj) {
        if(obj instanceof CacheEntry) {
            if(timestamp < ((CacheEntry) obj).getTimestamp()) {
                return -1;
            } else {
                return timestamp == ((CacheEntry) obj).getTimestamp() ? 0 : 1;
            }
        }
        return 0;
    }

}
