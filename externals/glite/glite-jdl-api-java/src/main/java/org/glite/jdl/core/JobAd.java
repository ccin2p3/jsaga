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
 * Version info: $Id: JobAd.java,v 1.4 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl.core;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;
import condor.classad.Constant;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

public class JobAd extends CommonAd implements Cloneable {

    private String stdin = null;
    private String stdout = null;
    private String stderr = null;

    public JobAd(){
        super();
    }

    JobAd(RecordExpr expr) throws JobAdException {
        build(expr);
        validate();
    }








    public String getStandardInput() throws JobAdException {
        validate();
        return stdin;
    }

    public void setStandardInput(String sIn) {
        stdin = sIn;
        modified = true;
    }

    public String getStandardOutput() throws JobAdException {
        validate();
        return stdout;
    }

    public void setStandardOutput(String sOut) {
        stdout = sOut;
        modified = true;
    }

    public String getStandardError() throws JobAdException {
        validate();
        return stderr;
    }

    public void setStandardError(String sErr) {
        stderr = sErr;
        modified = true;
    }







    public Object clone(){
        JobAd result = new JobAd();
        fillJobAd(result);
        return result;
    }

    protected void fillJobAd(JobAd jAd){
        super.fillJobAd(jAd);
        jAd.stdin = stdin;
        jAd.stdout = stdout;
        jAd.stderr = stderr;
    }



    protected String fillString(){
        StringBuffer buff = new StringBuffer(super.fillString());

        if( stdin!=null && stdin.length()>0 )
            buff.append(Jdl.STDINPUT).append("=\"").append(stringNormalize(stdin)).append("\";\n");

        if( stdout!=null && stdout.length()>0 )
            buff.append(Jdl.STDOUTPUT).append("=\"").append(stringNormalize(stdout)).append("\";\n");

        if( stderr!=null && stderr.length()>0 )
            buff.append(Jdl.STDERROR).append("=\"").append(stringNormalize(stderr)).append("\";\n");

        return buff.toString();
    }














    protected void build(RecordExpr record) throws JobAdException {

        super.build(record);

        try{
            Expr expr = record.lookup(Jdl.STDINPUT);
            if( expr!=null )
                stdin = ((Constant)expr).stringValue();
        /* ***********************************************************************************

           Check if stdin is in the ISB

           ********************************************************************************* */

            expr = record.lookup(Jdl.STDOUTPUT);
            if( expr!=null )
                stdout = ((Constant)expr).stringValue();

            expr = record.lookup(Jdl.STDERROR);
            if( expr!=null )
                stderr = ((Constant)expr).stringValue();

        }catch(Exception ex){
            raiseException(ex.getMessage());
        }
    }













    protected boolean validate() throws JobAdException {

        if( super.validate() ){

            if( stdin!=null && checkWildChars(stdin) )
                raiseException("Cannot use wild chars in StdInput attribute");

            if( stdout!=null && checkWildChars(stdout) )
                raiseException("Cannot use wild chars in StdOutput attribute");

            if( stderr!=null && checkWildChars(stderr) )
                raiseException("Cannot use wild chars in StdError attribute");

            return true;
        }

        return false;
    }


}
