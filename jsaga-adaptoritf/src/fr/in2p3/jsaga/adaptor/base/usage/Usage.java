package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExist;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Usage
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Usage {
    /**
     * Correct the value according to this usage
     * @param attributeName the name of the attribute to correct
     * @param attributeValue the value of the attribute to correct
     * @return the corrected value
     * @throws DoesNotExist if the attribute is not contained within this usage instance
     */
    public String correctValue(String attributeName, String attributeValue) throws DoesNotExist;

    /**
     * @param attributes a map containing all the attributes
     * @return the first matching usage
     * @throws DoesNotExist if no usage matches the attributes
     */
    public int getFirstMatchingUsage(Map attributes) throws DoesNotExist;

    /**
     * Build a usage instance containing missing attributes only
     * @param attributes a map containing all the attributes
     * @return a usage instance containing missing attributes only
     */
    public Usage getMissingValues(Map attributes);

    /**
     * @return a string representation of this usage instance
     */
    public String toString();
}
