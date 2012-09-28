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
 * Version info: $Id: ConfigNameParser.java,v 1.2 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.CompoundName;
import javax.naming.NamingException;
import java.util.Properties;
import org.glite.ce.commonj.configuration.basic.ConfigDataBase;


public class ConfigNameParser implements NameParser {

    public static final Properties syntax = new Properties();
    static {
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", ConfigDataBase.NAME_PATH_SEPARATOR);
        syntax.put("jndi.syntax.ignorecase", "false");
        syntax.put("jndi.syntax.escape", "\\");
        syntax.put("jndi.syntax.beginquote", "'");
    }

    public ConfigNameParser() {}

    public Name parse(String name) throws NamingException {
        return new CompoundName(name, syntax);
    }
}
