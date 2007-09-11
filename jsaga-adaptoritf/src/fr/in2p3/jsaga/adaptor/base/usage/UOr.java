package fr.in2p3.jsaga.adaptor.base.usage;

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

    public void updateAttributes(Map attributes) throws Exception {
        for (int i=0; m_or!=null && i<m_or.length; i++) {
            m_or[i].updateAttributes(attributes);
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
