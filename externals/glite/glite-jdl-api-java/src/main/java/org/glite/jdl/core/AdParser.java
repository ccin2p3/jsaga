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
 * Version info: $Id: AdParser.java,v 1.3 2006/10/23 13:53:54 pandreet Exp $
 */

package org.glite.jdl.core;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;

public class AdParser extends org.glite.jdl.AdParser {

    public AdParser(){}

    protected Object getJobAd(RecordExpr jdlExpr) throws JobAdException {
        return new JobAd(jdlExpr);
    }

    protected Object getMPICHAd(RecordExpr jdlExpr) throws JobAdException {
        return new MPICHAd(jdlExpr);
    }

    protected Object getInteractiveAd(RecordExpr jdlExpr) throws JobAdException {
        return new InteractiveAd(jdlExpr);
    }

    protected Object getParametricAd(RecordExpr jdlExpr) throws JobAdException {
        return new ParametricAd(jdlExpr);
    }

    protected Object getCollectionAd(RecordExpr jdlExpr) throws JobAdException {
        throw new JobAdException("Unsupported Ad type");
    }
}
