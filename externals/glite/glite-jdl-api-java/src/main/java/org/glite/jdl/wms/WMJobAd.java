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
 * Version info: $Id: WMJobAd.java,v 1.2 2006/10/26 14:52:49 pandreet Exp $
 */

package org.glite.jdl.wms;

import condor.classad.RecordExpr;

import org.glite.jdl.core.JobAd;
import org.glite.jdl.JobAdException;

public class WMJobAd extends JobAd implements WMExtensions {

    private CommonAd innerAd;

    public WMJobAd(){
        super();
        innerAd = new CommonAd();
    }

    public WMJobAd(RecordExpr expr) throws JobAdException {

        innerAd = new CommonAd();

        super.build(expr);
        innerAd.build(expr);
        validate();
        innerAd.validate();
    }

    public int getRetryCount() throws JobAdException {
        validate();
        return innerAd.getRetryCount();
    }

    public void setRetryCount(int count){
        innerAd.setRetryCount(count);
    }

    public int getShallowRetryCount() throws JobAdException {
        validate();
        return innerAd.getShallowRetryCount();
    }

    public void setShallowRetryCount(int count){
        innerAd.setShallowRetryCount(count);
    }

    public int getExpiryTime() throws JobAdException {
        validate();
        return innerAd.getExpiryTime();
    }

    public void setExpiryTime(int expTime) {
        innerAd.setExpiryTime(expTime);
    }

    public Object clone(){
        WMJobAd result = new WMJobAd();
        fillJobAd(result);
        result.innerAd = (CommonAd)innerAd.clone();
        return result;
    }

    protected String fillString(){
        StringBuffer buff = new StringBuffer(super.fillString());
        buff.append(innerAd.fillString());
        return buff.toString();
    }

}
