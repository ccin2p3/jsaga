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
 * Version info: $Id: ParameterList.java,v 1.1 2006/10/05 09:26:28 pandreet Exp $
 */

package org.glite.jdl.core;

import java.util.Iterator;
import java.util.ArrayList;

public class ParameterList implements Parameters {

    private ArrayList params;

    public ParameterList(){
        params = new ArrayList();
    }

    public Iterator getParameters(){
        return params.iterator();
    }

    public int size(){
        return params.size();
    }

    public Object clone(){
        ParameterList result = new ParameterList();
        result.params = (ArrayList)params.clone();
        return result;
    }

    public void add(String param){
        params.add(param);
    }

}

