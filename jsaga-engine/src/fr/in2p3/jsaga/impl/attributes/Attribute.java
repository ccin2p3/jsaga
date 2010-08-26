package fr.in2p3.jsaga.impl.attributes;

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
    /** using ';' instead of ',' because ',' can be used in file paths */
    public static final String SEPARATOR = ";";

    public String getKey();
    public boolean isReadOnly();

    public boolean equals(Object o);
    public Attribute clone() throws CloneNotSupportedException;
}
