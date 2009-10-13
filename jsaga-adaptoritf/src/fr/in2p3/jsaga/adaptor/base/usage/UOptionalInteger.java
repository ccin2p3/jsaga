package fr.in2p3.jsaga.adaptor.base.usage;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UOptionalInteger
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   13 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class UOptionalInteger extends UOptional {
    public UOptionalInteger(String name) {
        super(name);
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        if (value != null) {
            return Integer.valueOf((String) value);
        } else {
            return null;
        }
    }
}
