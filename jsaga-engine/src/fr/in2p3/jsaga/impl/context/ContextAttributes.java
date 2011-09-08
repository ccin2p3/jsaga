package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.impl.attributes.*;
import fr.in2p3.jsaga.impl.context.attrs.*;
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
    AttributeScalar m_type;
    AttributeScalar m_urlPrefix;
    BaseUrlPatternAttribute m_baseUrlIncludes;
    BaseUrlPatternAttribute m_baseUrlExcludes;
    JobServiceConfigAttribute m_jobServiceAttributes;
    DataServiceConfigAttribute m_dataServiceAttributes;

    /** constructor */
    ContextAttributes(ContextImpl context) {
        m_type = context._addAttribute(new ScalarAttributeImpl<String>(
                Context.TYPE,
                "type of context",
                MetricMode.Final,
                MetricType.String,
                "Unknown"));
        m_urlPrefix = context._addAttribute(new UrlPrefixAttribute());
        m_baseUrlIncludes = (BaseUrlPatternAttribute) context._addVectorAttribute(new BaseUrlPatternAttribute(
                ContextImpl.BASE_URL_INCLUDES,
                "array of URL patterns accepted for this context"));
        m_baseUrlExcludes = (BaseUrlPatternAttribute) context._addVectorAttribute(new BaseUrlPatternAttribute(
                ContextImpl.BASE_URL_EXCLUDES,
                "array of URL patterns rejected for this context"));
        m_jobServiceAttributes = (JobServiceConfigAttribute) context._addVectorAttribute(new JobServiceConfigAttribute());
        m_dataServiceAttributes = (DataServiceConfigAttribute) context._addVectorAttribute(new DataServiceConfigAttribute());
    }

    /** clone */
    public ContextAttributes clone() throws CloneNotSupportedException {
        ContextAttributes clone = (ContextAttributes) super.clone();
        clone.m_type = m_type;
        clone.m_urlPrefix = m_urlPrefix;
        clone.m_baseUrlIncludes = m_baseUrlIncludes;
        clone.m_baseUrlExcludes = m_baseUrlExcludes;
        clone.m_jobServiceAttributes = m_jobServiceAttributes;
        clone.m_dataServiceAttributes = m_dataServiceAttributes;
        return clone;
    }

    public AttributeScalar getScalarAttribute(String key) throws DoesNotExistException, IncorrectStateException {
        if (m_type.getKey().equals(key)) {
            return m_type;
        } else if (m_urlPrefix.getKey().equals(key)) {
            return m_urlPrefix;
        } else if (m_baseUrlIncludes.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on vector attribute: "+key);
        } else if (m_baseUrlExcludes.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on vector attribute: "+key);
        } else if (m_jobServiceAttributes.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on vector attribute: "+key);
        } else if (m_dataServiceAttributes.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on vector attribute: "+key);
        } else {
            throw new DoesNotExistException("[INTERNAL ERROR] This exception should have been catched");
        }
    }

    public AttributeVector getVectorAttribute(String key) throws DoesNotExistException, IncorrectStateException {
        if (m_type.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on scalar attribute: "+key);
        } else if (m_urlPrefix.getKey().equals(key)) {
            throw new IncorrectStateException("Operation not allowed on scalar attribute: "+key);
        } else if (m_baseUrlIncludes.getKey().equals(key)) {
            return m_baseUrlIncludes;
        } else if (m_baseUrlExcludes.getKey().equals(key)) {
            return m_baseUrlExcludes;
        } else if (m_jobServiceAttributes.getKey().equals(key)) {
            return m_jobServiceAttributes;
        } else if (m_dataServiceAttributes.getKey().equals(key)) {
            return m_dataServiceAttributes;
        } else {
            throw new DoesNotExistException("[INTERNAL ERROR] This exception should have been catched");
        }
    }
}
