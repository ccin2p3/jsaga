package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExist;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UAnd
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UAnd implements Usage {
    private Usage[] m_and;

    public UAnd(Usage[] usage) {
        m_and = usage;
    }

    public final boolean containsName(String attributeName) {
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            if (m_and[i].containsName(attributeName)) {
                return true;
            }
        }
        return false;
    }

    public void resetWeight() {
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            m_and[i].resetWeight();
        }
    }

    public String correctValue(String attributeName, String attributeValue, int attributeWeight) throws DoesNotExist {
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            try {
                return m_and[i].correctValue(attributeName, attributeValue, attributeWeight);
            } catch(DoesNotExist e) {
                // next iteration
            }
        }
        throw new DoesNotExist("Attribute not found: "+attributeName);
    }

    /**
     * @return -1 if the weight of at least one sub-usage is -1, else the max weight of sub-usages
     */
    public int getWeight() {
        int maxWeight = -1;
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            int weight = m_and[i].getWeight();
            if (weight == -1) {
                return -1;
            } else if (weight > maxWeight) {
                maxWeight = weight;
            }
        }
        return maxWeight;
    }

    /**
     * @return true if at least one sub-usage returns true
     */
    public boolean removeValue(String attributeName) {
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            if (m_and[i].removeValue(attributeName)) {
                return true;
            }
        }
        return false;
    }

    public Usage getMissingValues(Map attributes) {
        List missing = new ArrayList();
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            Usage m = m_and[i].getMissingValues(attributes);
            if (m != null) {
                missing.add(m);
            }
        }
        if (missing.isEmpty()) {
            return null;
        } else if (missing.size() == 1) {
            return (Usage) missing.get(0);
        } else {
            return new UAnd((Usage[]) missing.toArray(new Usage[missing.size()]));
        }
    }

    public void promptForValues(Map attributes, String id) throws Exception {
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            m_and[i].promptForValues(attributes, id);
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            if(i>0) buf.append(" ");
            buf.append(m_and[i].toString());
        }
        buf.append(')');
        return buf.toString();
    }
}
