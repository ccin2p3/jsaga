package fr.in2p3.jsaga.adaptor.base.usage;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

import java.io.FileNotFoundException;
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
public class UOr extends ULogicalOperation {

    protected UOr(Collection<Usage> c) {
        super(c);
    }
    

    @Deprecated
    public UOr(Usage[] array) {
        super(array);
    }


    public int getFirstMatchingUsage(Map attributes) throws DoesNotExistException, BadParameterException {
        for (Iterator<Usage> i = this.iterator(); i.hasNext();) {
            try {
                int id = i.next().getFirstMatchingUsage(attributes);
                if (id > -1) {
                    return id;
                }
            } catch(DoesNotExistException e) {
                // try next
            } catch (FileNotFoundException e) {
                // try next
            }
        }
        throw new DoesNotExistException("No matching usage found");
    }

    public Usage getMissingValues(Map attributes) {
        List missing = new ArrayList();
        for (Iterator<Usage> i = this.iterator(); i.hasNext();) {
            Usage m = i.next().getMissingValues(attributes);
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
            return new UOr(missing);
        }
    }

    @Override
    public String getSeparator() {
        return "|";
    }
    
    public static class Builder {
        private Vector<Usage> m_coll = new Vector<Usage>();
        public Builder or(Usage newOr) {
            this.m_coll.add(newOr);
            return this;
        }
        public UOr build() {
            return new UOr(m_coll);
        }
    }

}
