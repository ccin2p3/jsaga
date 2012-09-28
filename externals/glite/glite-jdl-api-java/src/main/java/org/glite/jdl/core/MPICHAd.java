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
 * Version info: $Id: MPICHAd.java,v 1.4 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl.core;

import java.util.ArrayList;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.Constant;
import condor.classad.FuncCall;
import condor.classad.AttrName;
import condor.classad.AttrRef;
import condor.classad.Op;
import condor.classad.SelectExpr;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

public class MPICHAd extends JobAd implements Cloneable {

    private int nodeNumber = -1;

    public MPICHAd(){
        super();
    }

    MPICHAd(RecordExpr expr) throws JobAdException {
        build(expr);
        validate();
    }








    public int getNodeNumber() throws JobAdException {
        validate();
        return nodeNumber;
    }

    public void setNodeNumber(int nodes) {
        nodeNumber = nodes;
        modified = true;
    }







    public Object clone(){
        MPICHAd result = new MPICHAd();
        fillJobAd(result);
        return result;
    }

    protected void fillJobAd(MPICHAd jAd){
        super.fillJobAd(jAd);
        jAd.nodeNumber = nodeNumber;
    }












    protected String fillString(){
        StringBuffer buff = new StringBuffer();
        buff.append(Jdl.TYPE).append("=\"").append(Jdl.TYPE_JOB).append("\";\n");
        buff.append(Jdl.JOBTYPE).append("=\"").append(Jdl.JOBTYPE_MPICH).append("\";\n");

        buff.append(super.fillString());

        buff.append(Jdl.NODENUMB).append("=\"").append(nodeNumber).append("\";\n");

        return buff.toString();
    }














    protected void build(RecordExpr record) throws JobAdException {

        super.build(record);

        try{
            Expr expr = record.lookup(Jdl.NODENUMB);
            if( expr!=null )
                nodeNumber = ((Constant)expr).intValue();

        }catch(Exception ex){
            raiseException(ex.getMessage());
        }
    }













    protected boolean validate() throws JobAdException {

        if( super.validate() ){

            if( nodeNumber<2 )
                raiseException("NodNumber attribute must be greater than 1");

            return true;
        }

        return false;
    }


    Expr fillInRequirements(Expr expr){

        AttrRef otherRef = new AttrRef("other");
        ArrayList arguments = new ArrayList();
        arguments.add(Constant.getInstance("MPICH"));
        arguments.add(new SelectExpr(otherRef, "GlueHostApplicationSoftwareRunTimeEnvironment"));
        Expr mExpr = FuncCall.getInstance(AttrName.fromString("member"),arguments);

        Expr cpuExpr = new Op(Expr.GREATER_EQ,
                              new SelectExpr(otherRef, "GlueCEInfoTotalCPUs"),
                              new AttrRef(Jdl.NODENUMB));

        return new Op(Expr.AND, new Op(Expr.AND, expr, mExpr), cpuExpr);
    }

}
