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
     * Test if the attribute <code>attributeName</code> is contained within this usage instance
     * @param attributeName the name of the attribute to search for
     * @return true if the attribute is contained within this usage instance, else false
     */
    public boolean containsName(String attributeName);

    /**
     * Reset the weight of all attributes to -1
     */
    public void resetWeight();

    /**
     * Correct the value according to this usage
     * @param attributeName the name of the attribute to correct
     * @param attributeValue the value of the attribute to correct
     * @param attributeWeight the weight of the attribute
     * @return the corrected value
     * @throws DoesNotExist if the attribute is not contained within this usage instance
     */
    public String correctValue(String attributeName, String attributeValue, int attributeWeight) throws DoesNotExist;

    /**
     * Get the weight of this usage instance
     * @return the weight
     */
    public int getWeight();

    /**
     * @return true if the value of the attribute <code>attributeName</code> must be removed
     */
    public boolean removeValue(String attributeName);

    /**
     * Build a usage instance containing missing attributes only
     * @param attributes a map containing all the attributes
     * @return a usage instance containing missing attributes only
     */
    public Usage getMissingValues(Map attributes);

    /**
     * Prompt the user for entering missing attribute values
     * @param attributes a map containing the missing attributes
     * @param id the identifier of the associated SAGA object
     */
    public void promptForValues(Map attributes, String id) throws Exception;

    /**
     * @return a string representation of this usage instance
     */
    public String toString();
}
