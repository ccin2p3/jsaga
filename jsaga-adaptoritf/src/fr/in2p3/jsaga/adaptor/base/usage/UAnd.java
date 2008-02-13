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
    private int m_id;
    private Usage[] m_and;

    public UAnd(Usage[] usage) {
        this(-1, usage);
    }

    public UAnd(int id, Usage[] usage) {
        m_id = id;
        m_and = usage;
    }

    public String correctValue(String attributeName, String attributeValue) throws DoesNotExist {
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            try {
                return m_and[i].correctValue(attributeName, attributeValue);
            } catch(DoesNotExist e) {
                // next iteration
            }
        }
        throw new DoesNotExist("Attribute not found: "+attributeName);
    }

    public int getFirstMatchingUsage(Map attributes) throws DoesNotExist {
        int firstMatchingUsage = -1;
        for (int i=0; m_and!=null && i<m_and.length; i++) {
            int id = m_and[i].getFirstMatchingUsage(attributes);
            if (firstMatchingUsage==-1 && id>-1) {
                firstMatchingUsage = id;
            }
        }
        if (firstMatchingUsage > -1) {
            return firstMatchingUsage;
        } else {
            return m_id;
        }
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
            return new UAnd(m_id, (Usage[]) missing.toArray(new Usage[missing.size()]));
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
