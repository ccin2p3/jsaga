package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

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
public class UAnd extends ULogicalOperation {
    private int m_id;

    public UAnd(Collection<Usage> c) {
        this(-1, c);
    }
    
    public UAnd(int id, Collection<Usage> c) {
        super(c);
        m_id = id;
    }
    
    @Deprecated
    public UAnd(Usage[] usage) {
        this(-1, usage);
    }
    
    @Deprecated
    public UAnd(int id, Usage[] usage) {
        super(usage);
        m_id = id;
    }

    public int getFirstMatchingUsage(Map attributes) throws DoesNotExistException, BadParameterException {
        int firstMatchingUsage = -1;
        for (int i=0; m_and_deprecated!=null && i<m_and_deprecated.length; i++) {
            int id = m_and_deprecated[i].getFirstMatchingUsage(attributes);
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
        for (int i=0; m_and_deprecated!=null && i<m_and_deprecated.length; i++) {
            Usage m = m_and_deprecated[i].getMissingValues(attributes);
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

    @Override
    public String getSeparator() {
        return "";
    }
    
    public static class Builder {
        private Vector<Usage> m_coll = new Vector<Usage>();
        private int m_id = -1;
        public Builder id(int i) {
            m_id = i;
            return this;
        }
        public Builder and(Usage newAnd) {
            this.m_coll.add(newAnd);
            return this;
        }
        public UAnd build() {
            return new UAnd(m_id, m_coll);
        }
    }
    
}
