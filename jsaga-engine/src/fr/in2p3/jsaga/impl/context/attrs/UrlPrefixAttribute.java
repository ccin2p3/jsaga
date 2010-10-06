package fr.in2p3.jsaga.impl.context.attrs;

import fr.in2p3.jsaga.impl.attributes.Attribute;
import fr.in2p3.jsaga.impl.attributes.AttributeScalar;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.*;

import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UrlPrefixAttribute
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class UrlPrefixAttribute implements AttributeScalar {
    private static final Pattern PATTERN = Pattern.compile("[\\p{Alpha}][\\p{Alpha}\\p{Digit}-]*");
    private String m_value;

    public UrlPrefixAttribute() {
        m_value = null;
    }

    public String getKey() {
        return ContextImpl.URL_PREFIX;
    }

    public String getDescription() {
        return "by adding this prefix at the beginning of URL, you can use any protocol without configuring it";
    }

    public boolean isReadOnly() {
        return false;
    }

    public MetricMode getMetricMode() {
        return MetricMode.Final;
    }

    public MetricType getMetricType() {
        return MetricType.String;
    }

    public Attribute clone() throws CloneNotSupportedException {
        UrlPrefixAttribute clone = new UrlPrefixAttribute();
        clone.m_value = m_value;
        return clone;
    }

    public void setValue(String value) throws NotImplementedException, IncorrectStateException, PermissionDeniedException, BadParameterException {
        if (PATTERN.matcher(value).matches()) {
            m_value = value;
        } else {
            throw new BadParameterException("UrlPrefix '"+value+"' does not match regular expression: "+PATTERN.pattern());
        }
    }

    public String getValue() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        return m_value;
    }
}
