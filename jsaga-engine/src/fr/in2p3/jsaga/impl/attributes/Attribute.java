package fr.in2p3.jsaga.impl.attributes;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Attribute
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Attribute extends Cloneable {
    public String getKey();
    public boolean isReadOnly();

    public void setValue(String value) throws NotImplementedException, IncorrectStateException, PermissionDeniedException;
    public String getValue() throws NotImplementedException, IncorrectStateException;
    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException;
    public String[] getValues() throws NotImplementedException, IncorrectStateException;

    public boolean equals(Object o);
    public Attribute clone() throws CloneNotSupportedException;
}
