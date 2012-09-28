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
 * Version info: $Id: AdParser.java,v 1.4 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;

public class AdParser {

    private static String normalizeJdl(String jdl){
		int idx = jdl.indexOf("\\");
		while (idx != -1) {
			jdl = jdl.substring(0, idx) + ("\\\\") + jdl.substring(idx + 1);
			idx = jdl.indexOf("\\", idx + 2);
		}

		idx = jdl.indexOf("\\\"");
		while (idx != -1) {
			jdl = jdl.substring(0, idx) + jdl.substring(idx + 1);
			idx = jdl.indexOf("\\\"", idx + 1);
		}

        if (!jdl.startsWith("[")) { 
			jdl = "[ " + jdl + "]"; 
		}

        return jdl;
    }

    private static AdParser getParser() throws JobAdException {
        String className = System.getProperty("adparser.class");
        if( className==null )
            return new AdParser();

        try{
            return (AdParser)Class.forName(className).newInstance();
        }catch(Exception ex){
            throw new JobAdException(ex.getMessage());
        }
    }

    public static Object parseJdl(String jdl) throws JobAdException {

        AdParser parser = getParser();

        ClassAdParser cp = new ClassAdParser(normalizeJdl(jdl));
		Expr expr = cp.parse();
		if (expr == null) {
			throw new JobAdException("Unable to parse: doesn't seem to be a valid Expression");
		} else if (expr.type != Expr.RECORD) {
			throw new JobAdException("Unable to parse: the parsed expression is not a ClassAd");
		}

        RecordExpr jdlExpr = (RecordExpr)expr;

        String type = null;
        expr = jdlExpr.lookup(Jdl.TYPE);
        if( expr==null ) 
            type = Jdl.TYPE_JOB;
        else if( !expr.isConstant() )
            throw new JobAdException("Wrong type parameter");
        else
            type = expr.stringValue();

        if( type.equalsIgnoreCase(Jdl.TYPE_JOB) ){

            expr = jdlExpr.lookup(Jdl.JOBTYPE);
            if( expr==null )
                return parser.getJobAd(jdlExpr);

            if( !expr.isConstant() )
                throw new JobAdException("Missing or wrong JobType");

            String jobType = expr.stringValue();

            if( jobType.equalsIgnoreCase(Jdl.JOBTYPE_MPICH) )
                return parser.getMPICHAd(jdlExpr);

            if( jobType.equalsIgnoreCase(Jdl.JOBTYPE_INTERACTIVE) )
                return parser.getInteractiveAd(jdlExpr);

            if( jobType.equalsIgnoreCase(Jdl.JOBTYPE_PARAMETRIC) )
                return parser.getParametricAd(jdlExpr);

            return parser.getJobAd(jdlExpr);

        }else if( type.equalsIgnoreCase(Jdl.TYPE_COLLECTION ) ){

            return parser.getCollectionAd(jdlExpr);

        }else{
            throw new JobAdException("Unsupported type " + expr.toString());
        }
    }

    public AdParser(){}

    protected Object getJobAd(RecordExpr jdlExpr) throws JobAdException {
        return new JobAd(jdlExpr);
    }

    protected Object getMPICHAd(RecordExpr jdlExpr) throws JobAdException {
        return new JobAd(jdlExpr);
    }

    protected Object getInteractiveAd(RecordExpr jdlExpr) throws JobAdException {
        return new JobAd(jdlExpr);
    }

    protected Object getParametricAd(RecordExpr jdlExpr) throws JobAdException {
        return new ParametricAd(jdlExpr);
    }

    protected Object getCollectionAd(RecordExpr jdlExpr) throws JobAdException {
        return new CollectionAd(jdlExpr);
    }

}
