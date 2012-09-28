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
 * Version info: $Id: ConfigNodes.java,v 1.2 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.util.HashMap;
import java.util.Iterator;

import org.glite.ce.commonj.configuration.CEConfigResource;

public class ConfigNodes extends HashMap<String, Object> implements CEConfigResource {
    private static final long serialVersionUID = 1L;

    public ConfigNodes() {
        super();
    }

    public void putConfigItem(ConfigItem item) {
        put(item.getName(), item);
    }

    public ConfigNodeList getList() {
        ConfigNodeList result = new ConfigNodeList();
        
        for (String tmpName : keySet()) {
            result.put(tmpName, get(tmpName).getClass().getName());
        }
        
        return result;
    }

}
