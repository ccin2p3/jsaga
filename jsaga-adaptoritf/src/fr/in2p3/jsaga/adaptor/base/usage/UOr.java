package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;

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
    private int m_weight;

    public UOr(Usage[] usage) {
        m_or = usage;
        m_weight = -1;
    }

    public final boolean containsName(String attributeName) {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            if (m_or[i].containsName(attributeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set weight (equals to the max weight of sub-usages)
     */
    public void setWeight(Map weights) {
        m_weight = -1;
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            m_or[i].setWeight(weights);
            int weight = m_or[i].getWeight();
            if (weight > m_weight) {
                m_weight = weight;
            }
        }
    }

    public int getWeight() {
        return m_weight;
    }

    public String correctValue(String attributeName, String attributeValue) throws DoesNotExist, NoSuccess {
        // try with selected alternative
        int selectedAlternative = this.selectAlternative();
        try {
            return m_or[selectedAlternative].correctValue(attributeName, attributeValue);
        } catch(DoesNotExist e) {
            // do nothing
        }

        // remove ambiguities with unselected alternatives
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            if (i != selectedAlternative) {
                try {
                    if (m_or[i].correctValue(attributeName, attributeValue) != null) {
                        return null;
                    }
                } catch(DoesNotExist e) {
                    // do nothing
                }
            }
        }
        throw new DoesNotExist("Attribute not found: "+attributeName);
    }
    private int selectAlternative() throws NoSuccess {
        // returns the 1st alternative with max weight
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            if (m_weight == m_or[i].getWeight()) {
                return i;
            }
        }
        throw new NoSuccess("INTERNAL ERROR: unexpected exception");
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
