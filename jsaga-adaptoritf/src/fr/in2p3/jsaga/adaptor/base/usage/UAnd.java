package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

import java.io.FileNotFoundException;
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

    /**
     * If 1 of Usages does not exist or is BadParameter, rethrow
     * Otherwise, if 1 of Usages is FileNotFound return -1
     * Otherwise, returns firstMatchingUsage if != -1
     * Otherwise, returns m_id
     */
    @Override
    public int getFirstMatchingUsage(Map attributes) throws DoesNotExistException, BadParameterException {
        int firstMatchingUsage = -1;
        for (Iterator<Usage> i = this.iterator(); i.hasNext();) {
            Usage nextU = i.next();
            // validate usage: if FileNotFound
            int id;
            try {
                id = nextU.getFirstMatchingUsage(attributes);
            } catch (FileNotFoundException e) {
                return -1;
            }
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
        for (Iterator<Usage> i = this.iterator(); i.hasNext();) {
            Usage m = i.next().getMissingValues(attributes);
            if (m != null) {
                missing.add(m);
            }
        }
        if (missing.isEmpty()) {
            return null;
        } else if (missing.size() == 1) {
            return (Usage) missing.get(0);
        } else {
            return new UAnd(m_id, missing);
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
