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
    
    public ULogicalOperation() {
        super();
    }
    
    public ULogicalOperation(Collection c) {
        super(c);
    }
    
    @Deprecated
    public ULogicalOperation(Usage[] usage) {
        super(usage.length);
        for (Usage u: usage) {
            this.add(u);
        }
    }

    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>(this.size());
        for (Iterator<Usage> i = this.iterator(); i.hasNext(); ) {
            keys.addAll(i.next().getKeys());
        }
        return keys;
    }

    public String correctValue(String attributeName, String attributeValue) throws DoesNotExistException {
        for (Iterator<Usage> i = this.iterator(); i.hasNext();) {
            try {
                return i.next().correctValue(attributeName, attributeValue);
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
        for (Iterator<Usage> i = this.iterator(); i.hasNext();) {
            buf.append(i.next().toString());
            if (i.hasNext())
                buf.append(" "+ getSeparator() + " ");
        }
        buf.append(')');
        return buf.toString();
    }
    
}
