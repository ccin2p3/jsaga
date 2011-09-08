package fr.in2p3.jsaga.impl.context.attrs;

import fr.in2p3.jsaga.impl.context.ContextImpl;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobServiceConfigAttribute
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * ***************************************************
 * Description:                                      */

public class JobServiceConfigAttribute extends ServiceConfigAttribute {

    public String getKey() {
        return ContextImpl.JOB_SERVICE_ATTRIBUTES;
    }

    public String getDescription() {
        return "array of attributes for job service used with this context";
    }


}
