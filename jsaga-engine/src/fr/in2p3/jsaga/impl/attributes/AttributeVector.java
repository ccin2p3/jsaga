package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AttributeVector
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public interface AttributeVector extends Attribute {
    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException, BadParameterException;
    public String[] getValues() throws NotImplementedException, IncorrectStateException, NoSuccessException;
}
