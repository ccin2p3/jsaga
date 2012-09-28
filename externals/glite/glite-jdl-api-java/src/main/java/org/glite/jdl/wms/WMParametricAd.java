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
 * Version info: $Id: WMParametricAd.java,v 1.2 2006/10/26 14:52:49 pandreet Exp $
 */

package org.glite.jdl.wms;

import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.Constant;

import org.glite.jdl.core.JobAd;
import org.glite.jdl.core.ParametricAd;
import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

public class WMParametricAd extends ParametricAd implements WMExtensions {

    private CommonAd innerAd;
    private boolean nodesCollocation = false;

    public WMParametricAd(){
        super();
        innerAd = new CommonAd();
    }

    public WMParametricAd(RecordExpr expr) throws JobAdException {

        innerAd = new CommonAd();

        build(expr);
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

    public boolean isNodesCollocation() throws JobAdException {
        validate();
        innerAd.validate();
        return nodesCollocation;
    }

    public void setNodesCollocation(boolean nc){
        nodesCollocation = nc;
        modified = true;
    }

    public Object clone(){
        WMParametricAd result = new WMParametricAd();
        super.fillJobAd(result);
        result.innerAd = (CommonAd)innerAd.clone();
        result.nodesCollocation = nodesCollocation;
        return result;
    }

    protected String fillString(){
        StringBuffer buff = new StringBuffer(super.fillString());
        buff.append(innerAd.fillString());
        buff.append(Jdl.NODES_COLLOCATION).append("=").append(nodesCollocation).append(";\n");
        return buff.toString();
    }

    protected JobAd getNewAd(){
        return new WMJobAd();
    }

    protected void fillInJobList() throws JobAdException {
        super.fillInJobList();
    }

    protected void build(RecordExpr record) throws JobAdException {

        super.build(record);

        try{
            Expr expr = record.lookup(Jdl.NODES_COLLOCATION);
            if( expr!=null )
                nodesCollocation = ((Constant)expr).booleanValue();
        }catch(Exception ex){
            raiseException(ex.getMessage());
        }

    }
}
