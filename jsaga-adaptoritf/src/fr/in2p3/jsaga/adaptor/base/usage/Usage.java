package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;

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
     * Set the weight of this usage instance
     * @param weights the weights of all the attributes
     */
    public void setWeight(Map weights);

    /**
     * Get the weight of this usage instance
     * @return the weight
     */
    public int getWeight();

    /**
     * Correct the value according to this usage
     * @param attributeName the name of the attribute to correct
     * @param attributeValue the value of the attribute to correct
     * @return the corrected value
     * @throws DoesNotExist if the attribute is not contained within this usage instance
     * @throws NoSuccess if the correction failed
     */
    public String correctValue(String attributeName, String attributeValue) throws DoesNotExist, NoSuccess;

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
