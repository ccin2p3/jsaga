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
 * Version info: $Id: ParametricAd.java,v 1.8 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl.core;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Method;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.ListExpr;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

public class ParametricAd extends JobAd implements Cloneable {

    private final static String PARAM_LABEL = "_PARAM_";
    private static Pattern pattern = Pattern.compile(PARAM_LABEL);

    private Parameters params = null;
    private ArrayList jobList = null;

    public ParametricAd(){
        super();
    }

    ParametricAd(RecordExpr expr) throws JobAdException {
        build(expr);
        validate();
    }


    public Parameters getParameters() throws JobAdException {
        validate();
        return (Parameters)params.clone();
    }

    public void setParameterList(Parameters params){
        this.params = (Parameters)params.clone();
        modified = true;
    }

    public Iterator getJobIterator() throws JobAdException {

        if( validate() || jobList==null ){
            fillInJobList();
        }

        return jobList.iterator();
    }














    public Object clone(){
        ParametricAd result = new ParametricAd();
        fillJobAd(result);
        return result;
    }

    protected void fillJobAd(ParametricAd jAd){
        super.fillJobAd(jAd);
        jAd.params = (Parameters)params.clone();
        jAd.jobList = jobList!=null ? (ArrayList)jobList.clone() : null;
    }






    protected String fillString(){
        StringBuffer buff = new StringBuffer();
        buff.append(Jdl.TYPE).append("=\"").append(Jdl.TYPE_JOB).append("\";\n");
        buff.append(Jdl.JOBTYPE).append("=\"").append(Jdl.JOBTYPE_PARAMETRIC).append("\";\n");

        buff.append(super.fillString());

        if( params instanceof ParameterRange ){
            ParameterRange pRange = (ParameterRange)params;
            buff.append(Jdl.PARAMETRIC_PARAMS_START).append("=").append(pRange.getBegin()).append(";\n");
            buff.append(Jdl.PARAMETRIC_PARAMS).append("=").append(pRange.getEnd()).append(";\n");
            buff.append(Jdl.PARAMETRIC_PARAMS_STEP).append("=").append(pRange.getStep()).append(";\n");
        }else if( params.size()>0 ){
            Iterator pItems = params.getParameters();
            buff.append(Jdl.PARAMETRIC_PARAMS).append("={\n  ").append(pItems.next().toString());
            while( pItems.hasNext() ){
                buff.append(",\n  ").append(pItems.next().toString());
            }
            buff.append("\n};\n");
        }

        return buff.toString();
    }






















    protected void build(RecordExpr record) throws JobAdException {

        super.build(record);

        try{
            Expr expr = record.lookup(Jdl.PARAMETRIC_PARAMS);
            if( expr==null )
                throw new IllegalArgumentException(Jdl.PARAMETRIC_PARAMS + " attribute missing");

            if( expr.type==Expr.LIST ){

                ParameterList pList = new ParameterList();

                ListExpr lExpr = (ListExpr)expr;
                for(int k=0; k<lExpr.size(); k++){
                    Expr tExpr = lExpr.sub(k);
                    if( tExpr.type==Expr.ATTRIBUTE )
                        pList.add(tExpr.toString());
                    else
                        throw new IllegalArgumentException("Wrong type for parameter " + tExpr.toString());
                }

                params = pList;

            }else if( expr.type==Expr.INTEGER ){

                int begin = 0;
                int end = expr.intValue();
                int step = 1;

                expr = record.lookup(Jdl.PARAMETRIC_PARAMS_START);
                if( expr!=null ){
                    if( expr.type==Expr.INTEGER )
                        begin = expr.intValue();
                    else
                        throw new IllegalArgumentException("Wrong type for attribute " + Jdl.PARAMETRIC_PARAMS_START);
                }

                expr = record.lookup(Jdl.PARAMETRIC_PARAMS_STEP);
                if( expr!=null ){
                    if( expr.type==Expr.INTEGER )
                        step = expr.intValue();
                    else
                        throw new IllegalArgumentException("Wrong type for attribute " + Jdl.PARAMETRIC_PARAMS_STEP);
                }

                params = new ParameterRange(begin, end, step);

            }else{
                throw new IllegalArgumentException("Wrong type for attribute " + Jdl.PARAMETRIC_PARAMS);
            }
        }catch(Exception ex){
            raiseException(ex.getMessage());
        }
    }






    protected boolean validate() throws JobAdException {

        if( super.validate() ){
            if( params==null )
                raiseException("Missing definition for parameters");

            /* *************************************************************************

               Check if _PARAM_ is present

            ************************************************************************* */
            return true;
        }

        return false;
    }

    protected JobAd getNewAd(){
        return new JobAd();
    }


    protected void fillInJobList() throws JobAdException {

        jobList = new ArrayList(params.size());
        for(int k=0; k<params.size(); k++)
            jobList.add(getNewAd());

        Class[] aClass = new Class[]{String.class};
        Class[] rClass = new Class[]{Expr.class};
        Class jClass = JobAd.class;

        try{

            

            fillInStringAttr(getExecutable(), jClass.getMethod("setExecutable",aClass));
            fillInStringAttr(getArguments(), jClass.getMethod("setArguments",aClass));
            fillInStringAttr(getPrologue(), jClass.getMethod("setPrologue",aClass));
            fillInStringAttr(getPrologueArguments(), jClass.getMethod("setPrologueArguments",aClass));
            fillInStringAttr(getEpilogue(), jClass.getMethod("setEpilogue",aClass));
            fillInStringAttr(getEpilogueArguments(), jClass.getMethod("setEpilogueArguments",aClass));
            fillInStringAttr(getVirtualOrganization(), jClass.getMethod("setVirtualOrganization",aClass));
            fillInStringAttr(getStandardInput(), jClass.getMethod("setStandardInput",aClass));
            fillInStringAttr(getStandardOutput(), jClass.getMethod("setStandardOutput",aClass));
            fillInStringAttr(getStandardError(), jClass.getMethod("setStandardError",aClass));

            Iterator vars = getEnvKeys();
            while( vars.hasNext() ){
                String key = (String)vars.next();
                Matcher matcher1 = pattern.matcher(key);
                Matcher matcher2 = pattern.matcher(getEnvValue(key));

                Iterator currentParam = params.getParameters();
                for(int k=0; k<params.size(); k++){
                    String token = currentParam.next().toString();
                    ((JobAd)jobList.get(k)).setEnvValue(matcher1.replaceAll(token), matcher2.replaceAll(token));
                }                
            }


            Iterator isb = getInputSandboxIterator();
            while( isb.hasNext() )
                fillInStringAttr((String)isb.next(), jClass.getMethod("addInputSandboxURI",aClass));

            Iterator osb = getOutputSandboxIterator();
            while( osb.hasNext() ){
                OutputSandboxPair pair = (OutputSandboxPair)osb.next();
                Matcher matcher1 = pattern.matcher(pair.getRelativePath());
                Matcher matcher2 = pattern.matcher(pair.getDestinationURI());

                Iterator currentParam = params.getParameters();
                for(int k=0; k<params.size(); k++){
                    JobAd currentJob = (JobAd)jobList.get(k);
                    String token = currentParam.next().toString();
                    currentJob.addOutputSandboxPair(new OutputSandboxPair(matcher1.replaceAll(token),
                                                                          matcher2.replaceAll(token)));
                }
            }

            fillInExprAttr(getRequirements(), jClass.getMethod("setRequirements",rClass));
            fillInExprAttr(getRank(), jClass.getMethod("setRank",rClass));

        }catch(Exception ex){}

    }



    protected void fillInStringAttr(String source, Method method){

        Matcher matcher = source!=null && source.indexOf(PARAM_LABEL)>=0 ? pattern.matcher(source) : null;

        Iterator currentParam = params.getParameters();
        for(int k=0; k<params.size(); k++){
            Object[] args = new Object[1];
            if( matcher!=null )
                args[0] = matcher.replaceAll(currentParam.next().toString());
            else
                args[0] = source;

            try{
                method.invoke(jobList.get(k), args);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }


    protected void fillInExprAttr(Expr expr, Method method){
        String eStr = expr.toString();
        Iterator currentParam = params.getParameters();
        for(int k=0; k<params.size(); k++){
            Object[] args = new Object[1];
            if( eStr.indexOf(PARAM_LABEL)>=0 ){
                Matcher matcher = pattern.matcher(eStr);
                ClassAdParser cp = new ClassAdParser(matcher.replaceAll(currentParam.next().toString()));
                args[0] = cp.parse();
            }else{
                args[0] = cloneExpr(expr);
            }

            try{
                method.invoke(jobList.get(k), args);
            }catch(Throwable th){
                th.printStackTrace();
            }

        }

    }





}
