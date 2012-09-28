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
 * Version info: $Id: InteractiveAd.java,v 1.6 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl.core;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.Constant;
import condor.classad.AttrRef;
import condor.classad.Op;
import condor.classad.SelectExpr;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

public class InteractiveAd extends CommonAd implements Cloneable {

    public InteractiveAd(){
        super();
    }

    InteractiveAd(RecordExpr expr) throws JobAdException {
        build(expr);
        validate();
    }


    public Object clone(){
        InteractiveAd result = new InteractiveAd();
        fillJobAd(result);
        return result;
    }

    protected String fillString(){
        StringBuffer buff = new StringBuffer();
        buff.append(Jdl.TYPE).append("=\"").append(Jdl.TYPE_JOB).append("\";\n");
        buff.append(Jdl.JOBTYPE).append("=\"").append(Jdl.JOBTYPE_INTERACTIVE).append("\";\n");

        buff.append(super.fillString());
        return buff.toString();
    }

    protected void build(RecordExpr record) throws JobAdException {

        super.build(record);

        Expr expr = record.lookup(Jdl.STDINPUT);
        if( expr!=null )
            raiseException(Jdl.STDINPUT + " attribute not allowed for interactive jobs");

        expr = record.lookup(Jdl.STDOUTPUT);
        if( expr!=null )
            raiseException(Jdl.STDOUTPUT + " attribute not allowed for interactive jobs");

        expr = record.lookup(Jdl.STDERROR);
        if( expr!=null )
            raiseException(Jdl.STDERROR + " attribute not allowed for interactive jobs");
    }

    Expr fillInRequirements(Expr expr){

        return new Op(Expr.AND, expr, 
                      new SelectExpr(new AttrRef("other"),"GlueHostNetworkAdapterOutboundIP"));

    }
}
