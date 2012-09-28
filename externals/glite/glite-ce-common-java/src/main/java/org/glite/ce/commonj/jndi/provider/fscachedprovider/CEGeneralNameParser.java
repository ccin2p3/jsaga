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
 * Version info: $Id: CEGeneralNameParser.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.CompoundName;
import javax.naming.NamingException;
import java.util.Properties;


class CEGeneralNameParser implements NameParser {

    private static final Properties syntax = new Properties();
    static {
        syntax.put("jndi.syntax.direction", "left_to_right");
	syntax.put("jndi.syntax.separator", "/");
        syntax.put("jndi.syntax.ignorecase", "false");
	syntax.put("jndi.syntax.escape", "\\");
	syntax.put("jndi.syntax.beginquote", "'");
    }

    public Name parse(String name) throws NamingException {
        return new CompoundName(name, syntax);
    }
}
