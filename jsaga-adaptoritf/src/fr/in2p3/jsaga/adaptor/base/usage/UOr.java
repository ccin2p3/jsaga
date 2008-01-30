package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExist;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UOr
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UOr implements Usage {
    private Usage[] m_or;

    public UOr(Usage[] usage) {
        m_or = usage;
    }

    public final boolean containsName(String attributeName) {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            if (m_or[i].containsName(attributeName)) {
                return true;
            }
        }
        return false;
    }

    public void resetWeight() {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            m_or[i].resetWeight();
        }
    }

    public String correctValue(String attributeName, String attributeValue, int attributeWeight) throws DoesNotExist {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            try {
                return m_or[i].correctValue(attributeName, attributeValue, attributeWeight);
            } catch(DoesNotExist e) {
                // next iteration
            }
        }
        throw new DoesNotExist("Attribute not found: "+attributeName);
    }

    /**
     * @return the max weight of sub-usages
     */
    public int getWeight() {
        int maxWeight = -1;
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            int weight = m_or[i].getWeight();
            if (weight > maxWeight) {
                maxWeight = weight;
            }
        }
        return maxWeight;
    }

    /**
     * @return true if the attribute is not contained in the selected alternative, but contained in another one
     */
    public boolean removeValue(String attributeName) {
        // selected alternative = alternative with max weight
        int selectedAlternative = 0;
        for (int i=0,maxWeight=-1; m_or!=null && i<m_or.length; i++) {
            int weight = m_or[i].getWeight();
            if (weight > maxWeight) {
                selectedAlternative = i;
                maxWeight = weight;
            }
        }
        // test alternatives
        if (m_or[selectedAlternative].containsName(attributeName)) {
            return false;
        } else {
            for (int i=0; m_or!=null && i<m_or.length; i++) {
                if (i!=selectedAlternative && m_or[i].containsName(attributeName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Usage getMissingValues(Map attributes) {
        List missing = new ArrayList();
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            Usage m = m_or[i].getMissingValues(attributes);
            if (m != null) {
                missing.add(m);
            }
        }
        if (missing.size() < m_or.length) {
            return null;
        } else if (missing.size() == 1) {
            return (Usage) missing.get(0);
        } else {
            return new UOr((Usage[]) missing.toArray(new Usage[missing.size()]));
        }
    }

    public void promptForValues(Map attributes, String id) throws Exception {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            if (i > 0) {
                System.out.println(" => Trying next alternative: "+m_or[i].toString());
            }
            try {
                m_or[i].promptForValues(attributes, id);
                return; //found a valid alternative
            } catch(Exception e) {
            }
        }
        throw new Exception("Missing attributes: "+this.getMissingValues(attributes).toString());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            if(i>0) buf.append(" | ");
            buf.append(m_or[i].toString());
        }
        buf.append(')');
        return buf.toString();
    }
}
