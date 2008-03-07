/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.tools.jndi;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

public class ParameterRuleSet extends RuleSetBase
{
    protected String prefix = null;
    
    public ParameterRuleSet()
    {
        this("");
    }
    
    public ParameterRuleSet(String prefix)
    {
        super();
        this.prefix = prefix;
        this.namespaceURI = "http://wsrf.globus.org/jndi/config";
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.digester.RuleSet#addRuleInstances(org.apache.commons.digester.Digester)
     */
    public void addRuleInstances(Digester digester)
    {
        digester.addObjectCreate(prefix + "resourceParams", ResourceParameters.class);
        digester.addSetProperties(prefix + "resourceParams");
        digester.addSetNext(prefix + "resourceParams", "setParameters");
        digester.addCallMethod(prefix + "resourceParams/parameter",
            "addParameter", 2);
        digester.addCallParam(prefix + "resourceParams/parameter/name", 0);
        digester.addCallParam(prefix + "resourceParams/parameter/value", 1);
    }

}
