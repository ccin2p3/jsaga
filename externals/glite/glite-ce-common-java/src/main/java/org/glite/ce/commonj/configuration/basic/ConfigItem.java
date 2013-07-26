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
 * Version info: $Id: ConfigItem.java,v 1.2 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.util.HashMap;

import org.glite.ce.commonj.configuration.CEConfigResource;

public class ConfigItem extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String path;
    private CEConfigResource content;

    public ConfigItem(String name, String path) {
        super();

        this.name = name;
        this.path = path;
        content = null;

    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return new String(path);
    }

    public void setContent(CEConfigResource c) {
        content = c;
    }

    public CEConfigResource getContent() {
        return content;
    }

    public Object clone() {
        return internalClone(false);
    }

    public ConfigItem deepClone() {
        return (ConfigItem) internalClone(true);
    }

    protected Object internalClone(boolean deepClone) {
        ConfigItem dup = new ConfigItem(name, path);

        if ((content instanceof ConfigNodes) && !deepClone) {
            /*
             * All references to children must be removed
             */
            dup.setContent(((ConfigNodes) content).getList());
        } else {
            dup.setContent((CEConfigResource) content.clone());
        }

        for(String key : keySet()) {
            dup.put(key, get(key));
        }

        return dup;
    }
}
