package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.impl.attributes.AttributeImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.context.Context;

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
    AttributeImpl<String> m_type;
    AttributeImpl<Integer> m_indice;
    AttributeImpl<String> m_name;

    /** constructor */
    ContextAttributes(ContextImpl context) {
        m_type = context._addAttribute(new AttributeImpl<String>(
                Context.TYPE,
                "type of context",
                MetricMode.Final,
                MetricType.String,
                "Unknown"));
        m_indice = context._addAttribute(new AttributeImpl<String>(
                ContextImpl.INDICE,
                "indice of the context instance (relative to a type)",
                MetricMode.Final,
                MetricType.Int,
                null));
        m_name = context._addAttribute(new AttributeImpl<String>(
                ContextImpl.NAME,
                "name of the context instance",
                MetricMode.ReadWrite,
                MetricType.String,
                null));
    }

    /** clone */
    public ContextAttributes clone() throws CloneNotSupportedException {
        ContextAttributes clone = (ContextAttributes) super.clone();
        clone.m_type = m_type.clone();
        clone.m_indice = m_indice.clone();
        clone.m_name = m_name.clone();
        return clone;
    }
}
