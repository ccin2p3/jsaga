package fr.in2p3.jsaga.adaptor.base.usage;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UOptionalBoolean
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   13 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class UOptionalBoolean extends UOptional {
    public UOptionalBoolean(String name) {
        super(name);
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        if (value != null) {
            return Boolean.parseBoolean((String) value);
        } else {
            return null;
        }
    }
}

