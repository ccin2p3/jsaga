package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.DoesNotExistException;

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

    public String correctValue(String attributeName, String attributeValue) throws DoesNotExistException {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            try {
                return m_or[i].correctValue(attributeName, attributeValue);
            } catch(DoesNotExistException e) {
                // next iteration
            }
        }
        throw new DoesNotExistException("Attribute not found: "+attributeName);
    }

    public int getFirstMatchingUsage(Map attributes) throws DoesNotExistException {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            try {
                int id = m_or[i].getFirstMatchingUsage(attributes);
                if (id > -1) {
                    return id;
                }
            } catch(DoesNotExistException e) {
                // try next
            }
        }
        throw new DoesNotExistException("No matching usage found");
    }

    public Usage getMissingValues(Map attributes) {
        List missing = new ArrayList();
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            Usage m = m_or[i].getMissingValues(attributes);
            if (m != null) {
                missing.add(m);
            } else {
                //at least one alternative matches
                return null;
            }
        }
        if (missing.size() == 1) {
            return (Usage) missing.get(0);
        } else {
            return new UOr((Usage[]) missing.toArray(new Usage[missing.size()]));
        }
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
