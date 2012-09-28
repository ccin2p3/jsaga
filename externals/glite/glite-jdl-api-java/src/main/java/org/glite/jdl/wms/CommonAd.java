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
 * Version info: $Id: CommonAd.java,v 1.2 2006/10/26 14:52:49 pandreet Exp $
 */

package org.glite.jdl.wms;

import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.Constant;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

class CommonAd implements Cloneable{

    boolean modified;
    String lastMessage;

    private int retryCount = 0;
    private int shallowCount = 0;
    private int expiryTime = 0;

    public CommonAd(){
        modified = true;
        lastMessage = null;
    }

    public CommonAd(RecordExpr expr) throws JobAdException {
        build(expr);
        validate();
    }

    public int getRetryCount() throws JobAdException {
        validate();
        return retryCount;
    }

    public void setRetryCount(int count){
        retryCount = count;
        modified = true;
    }

    public int getShallowRetryCount() throws JobAdException {
        validate();
        return shallowCount;
    }

    public void setShallowRetryCount(int count){
        shallowCount = count;
        modified = true;
    }

    public int getExpiryTime() throws JobAdException {
        validate();
        return expiryTime;
    }

    public void setExpiryTime(int expTime) {
        expiryTime = expTime;
        modified = true;
    }

    public Object clone(){
        CommonAd result = new CommonAd();
        fillJobAd(result);
        return result;
    }



    void fillJobAd(CommonAd ad){
        ad.retryCount = retryCount;
        ad.shallowCount = shallowCount;
        ad.expiryTime = expiryTime;
    }

    String fillString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(Jdl.RETRYCOUNT).append("=").append(retryCount).append(";\n");
        buffer.append(Jdl.SHALLOWRETRYCOUNT).append("=").append(shallowCount).append(";\n");
        buffer.append(Jdl.EXPIRY_TIME).append("=").append(expiryTime).append(";\n");
        return buffer.toString();
    }

    void build(RecordExpr record) throws JobAdException {
        modified = true;
        lastMessage = null;

        try{
            Expr expr = record.lookup(Jdl.RETRYCOUNT);
            if( expr!=null )
                retryCount = ((Constant)expr).intValue();

            expr = record.lookup(Jdl.SHALLOWRETRYCOUNT);
            if( expr!=null )
                shallowCount = ((Constant)expr).intValue();

            expr = record.lookup(Jdl.EXPIRY_TIME);
            if( expr!=null )
                expiryTime = ((Constant)expr).intValue();

        }catch(Exception ex){
            raiseException(ex.getMessage());
        }

    }

    boolean validate() throws JobAdException {
        if( !modified ){
            if( lastMessage!=null ){
                throw new JobAdException(lastMessage);
            }else{
                return false;
            }
        }

        if( retryCount<0 )
            raiseException(Jdl.RETRYCOUNT + " cannot be negative");
        if( shallowCount<-1 )
            raiseException(Jdl.SHALLOWRETRYCOUNT + " must be greater than -1");
        if( expiryTime<=0 )
            raiseException(Jdl.EXPIRY_TIME + " wrong or undefined");

        modified = false;
        lastMessage = null;

        return true;
    }

    void raiseException(String msg) throws JobAdException {
        lastMessage = msg;
        throw new JobAdException(msg);
    }

}
