package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AttributeScalar
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public interface AttributeScalar extends Attribute {
    public void setValue(String value) throws NotImplementedException, IncorrectStateException, PermissionDeniedException, BadParameterException;
    public String getValue() throws NotImplementedException, IncorrectStateException, NoSuccessException;
}
