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
 * Version info: $Id: SearchResultEnumeration.java,v 1.2 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.util.Enumeration;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

public class SearchResultEnumeration implements NamingEnumeration {
    private Enumeration<SearchResult> queryResults;

    public SearchResultEnumeration(ConfigItem[] source) throws NamingException {
        if (source == null) {
            throw new NamingException();
        }

        Vector<SearchResult> tmpv = new Vector<SearchResult>(source.length);
        
        for (int k = 0; k < source.length; k++) {
            Attributes attrToReturn = new BasicAttributes();
            
            for (String attrName : source[k].keySet()) {
                attrToReturn.put(new BasicAttribute(attrName, source[k].get(attrName)));
            }

            tmpv.add(new SearchResult(source[k].getName(), source[k].getContent(), attrToReturn));
        }

        queryResults = tmpv.elements();
    }

    public Object next() throws NamingException {
        return queryResults.nextElement();
    }

    public boolean hasMore() throws NamingException {
        return queryResults.hasMoreElements();
    }

    public void close() throws NamingException {
    }

    public boolean hasMoreElements() {
        return queryResults.hasMoreElements();
    }

    public Object nextElement() {
        return queryResults.nextElement();
    }
}
