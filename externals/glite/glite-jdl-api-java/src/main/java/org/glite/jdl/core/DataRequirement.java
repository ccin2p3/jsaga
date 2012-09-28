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
 * Version info: $Id: DataRequirement.java,v 1.2 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl.core;

import java.util.ArrayList;
import java.util.Iterator;

import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.Constant;
import condor.classad.ListExpr;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

public class DataRequirement {

    private String type;
    private String catalog;
    private ArrayList inputData;

    public DataRequirement(String type) throws JobAdException {

        if( !type.equals("RLS") || !type.equals("SI") || !type.equals("DLI") )
            throw new JobAdException("Wrong data requirement type: " + type);

        this.type = type;
        catalog = null;
        inputData = new ArrayList();

    }

    public DataRequirement(RecordExpr req) throws JobAdException {

        Expr expr = req.lookup(Jdl.DATA_CATALOG_TYPE);
        if( expr==null || expr.type!=Expr.STRING )
            throw new JobAdException("Wrong or missing data requirement type");

        type = ((Constant)expr).stringValue();
        if( !type.equals("RLS") || !type.equals("SI") || !type.equals("DLI") )
            throw new JobAdException("Wrong data requirement type: " + type);

        expr = req.lookup(Jdl.DATA_CATALOG);
        if( expr==null )
            catalog = null;
        else if( expr.type!=Expr.STRING )
            throw new JobAdException("Wrong data catalog");
        catalog = ((Constant)expr).stringValue();

        expr = req.lookup(Jdl.INPUTDATA);
        if( expr==null || expr.type!=Expr.LIST )
            throw new JobAdException("Wrong attribute " + Jdl.INPUTDATA);
        Iterator items = ((ListExpr)expr).iterator();
        while( items.hasNext() )
            addInputData(items.next().toString());

    }

    public void addInputData(String uri) throws JobAdException {

        int idx = uri.indexOf(":");
        if( idx<1 )
            throw new JobAdException("Wrong input data uri: " + uri);

        String prefix = uri.substring(0,idx).toLowerCase();
        if( prefix.equals("lfn") || prefix.equals("guid") ||
            ( prefix.equals("lds") && type.equals("DLI") ) ||
            ( prefix.equals("query") && type.equals("DLI") ) ){

            inputData.add(uri);

        }else{
            throw new JobAdException("Wrong input data uri: " + uri);
        }
    }

    public Iterator getInputDataIterator(){
        return inputData.iterator();
    }

    public int getInputDataListSize(){
        return inputData.size();
    }

    public void clearInputDataListSize(){
        inputData.clear();
    }

    public String getCatalogType(){
        return type;
    }

    public String getCatalog(){
        return catalog;
    }

    public String toString(){
        StringBuffer buff = new StringBuffer("[\n");
        buff.append("]\n");
        return buff.toString();
    }
}
