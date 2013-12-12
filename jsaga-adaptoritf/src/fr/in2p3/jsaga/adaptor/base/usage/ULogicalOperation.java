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
public abstract class ULogicalOperation extends Vector<Usage> implements Usage {
    @Deprecated
    protected Usage[] m_and_deprecated;
    
    public ULogicalOperation() {
        super();
    }
    
    public ULogicalOperation(Collection c) {
        super(c);
        m_and_deprecated = (Usage[])(c.toArray(new Usage[c.size()]));
    }
    
    @Deprecated
    public ULogicalOperation(Usage[] usage) {
        super(usage.length);
        m_and_deprecated = usage;
        for (Usage u: usage) {
            this.add(u);
        }
    }

    // TODO: remove this when m_and_deprecated is removed
    @Override
    public boolean add(Usage newUsage) {
        boolean res = super.add(newUsage);
        if (res) {
            // If newUsage was added, build a new array
            m_and_deprecated = new Usage[this.size()];
            this.toArray(m_and_deprecated);
        }
        return res;
    }
    
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>(m_and_deprecated.length);
        for (Usage u : m_and_deprecated) {
            keys.addAll(u.getKeys());
        }
        return keys;
    }

    public String correctValue(String attributeName, String attributeValue) throws DoesNotExistException {
        for (int i=0; m_and_deprecated!=null && i<m_and_deprecated.length; i++) {
            try {
                return m_and_deprecated[i].correctValue(attributeName, attributeValue);
            } catch(DoesNotExistException e) {
                // next iteration
            }
        }
        throw new DoesNotExistException("Attribute not found: "+attributeName);
    }

    public abstract int getFirstMatchingUsage(Map attributes) throws DoesNotExistException, BadParameterException;

    public abstract Usage getMissingValues(Map attributes);

    public abstract String getSeparator();
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i=0; m_and_deprecated!=null && i<m_and_deprecated.length; i++) {
            if(i>0) buf.append(" "+ getSeparator() + " ");
            buf.append(m_and_deprecated[i].toString());
        }
        buf.append(')');
        return buf.toString();
    }
    
}
