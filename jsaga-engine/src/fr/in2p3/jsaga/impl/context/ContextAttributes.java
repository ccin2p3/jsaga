package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.impl.attributes.ScalarAttributeImpl;
import fr.in2p3.jsaga.impl.attributes.VectorAttributeImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextAttributes implements Cloneable {
    ScalarAttributeImpl<String> m_type;

    /** constructor */
    ContextAttributes(ContextImpl context) {
        m_type = context._addAttribute(new ScalarAttributeImpl<String>(
                Context.TYPE,
                "type of context",
                MetricMode.Final,
                MetricType.String,
                "Unknown"));
    }

    /** clone */
    public ContextAttributes clone() throws CloneNotSupportedException {
        ContextAttributes clone = (ContextAttributes) super.clone();
        clone.m_type = m_type.clone();
        return clone;
    }

    public ScalarAttributeImpl<String> getScalarAttribute(String key) throws DoesNotExistException, IncorrectStateException {
        if (m_type.getKey().equals(key)) {
            return m_type;
        } else {
            throw new DoesNotExistException("[INTERNAL ERROR] This exception should have been catched");
        }
    }

    public VectorAttributeImpl<String> getVectorAttribute(String key) throws DoesNotExistException, IncorrectStateException {
        if (m_type.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on scalar attribute: "+key);
        } else {
            throw new DoesNotExistException("[INTERNAL ERROR] This exception should have been catched");
        }
    }
}
