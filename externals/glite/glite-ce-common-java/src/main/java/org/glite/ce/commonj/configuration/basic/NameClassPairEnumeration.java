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
 * Version info: $Id: NameClassPairEnumeration.java,v 1.2 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.util.Iterator;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

public class NameClassPairEnumeration implements NamingEnumeration {
    private ConfigNodeList nodes;
    private Iterator<String> nameIdx;

    public NameClassPairEnumeration(ConfigItem source) throws NotContextException {
        if (source == null) {
            throw new NotContextException();
        }
        
        Object tmpo = source.getContent();
        
        if (tmpo == null || !(tmpo instanceof ConfigNodeList)) {
            throw new NotContextException(source.getName());
        }

        nodes = (ConfigNodeList) tmpo;
        nameIdx = nodes.keySet().iterator();
    }

    public Object next() throws NamingException {
        String name = (String) nameIdx.next();
        String className = (String) nodes.get(name);
        return new NameClassPair(name, className);
    }

    public boolean hasMore() throws NamingException {
        return nameIdx.hasNext();
    }

    public void close() throws NamingException {
    }

    public boolean hasMoreElements() {
        return nameIdx.hasNext();
    }

    public Object nextElement() {
        try {
            return next();
        } catch (NamingException nEx) {
        }
        return null;
    }
}
