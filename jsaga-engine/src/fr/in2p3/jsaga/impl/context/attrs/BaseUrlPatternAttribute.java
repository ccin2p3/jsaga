package fr.in2p3.jsaga.impl.context.attrs;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;
import fr.in2p3.jsaga.engine.session.BaseUrlPattern;
import fr.in2p3.jsaga.engine.session.item.SchemeItem;
import fr.in2p3.jsaga.generated.parser.BaseUrlParser;
import fr.in2p3.jsaga.generated.parser.ParseException;
import fr.in2p3.jsaga.impl.attributes.Attribute;
import fr.in2p3.jsaga.impl.attributes.AttributeVector;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.*;

import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BaseUrlPatternAttribute
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class BaseUrlPatternAttribute implements AttributeVector {
    private String m_key;
    private String m_description;
    private BaseUrlPattern[] m_values;
    private Properties m_aliases;

    public BaseUrlPatternAttribute(String key, String description) {
        m_key = key;
        m_description = description;
        m_values = new BaseUrlPattern[]{};
        m_aliases = new Properties();
    }

    public String getKey() {
        return m_key;
    }

    public String getDescription() {
        return m_description;
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
        BaseUrlPatternAttribute clone = new BaseUrlPatternAttribute(m_key, m_description);
        clone.m_values = m_values;
        return clone;
    }

    public void setValues(String[] values) throws NotImplementedException, IncorrectStateException, PermissionDeniedException, BadParameterException {
        // set patterns
        List<BaseUrlPattern> list = new ArrayList<BaseUrlPattern>();
        for (String v : values) {
            List<String> splitted = split(v);
            for (String url : splitted) {
                try {
                    BaseUrlItem[] items = BaseUrlParser.parse(url);
                    list.add(new BaseUrlPattern(items));
                } catch (ParseException e) {
                    throw new BadParameterException(e);
                }
            }
        }
        m_values = list.toArray(new BaseUrlPattern[list.size()]);

        // set aliases
        for (BaseUrlPattern p : list) {
            SchemeItem item = (SchemeItem) p.getItems()[0];
            if (item.getSchemeOrNull() != null) {
                m_aliases.setProperty(item.getValue(), item.getSchemeOrNull());
            }
        }
    }

    public String[] getValues() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        String[] values = new String[m_values.length];
        for (int i=0; i<m_values.length; i++) {
            values[i] = m_values[i].toString();
        }
        return values;
    }

    public void throwIfConflictsWith(String currentLabel, String refLabel, BaseUrlPatternAttribute refIncludes, BaseUrlPatternAttribute refExcludes, BaseUrlPatternAttribute currentExcludes) throws NoSuccessException {
        mainloop: for (BaseUrlPattern pattern : m_values) {
            // ignore exclude patterns
            for (BaseUrlPattern currentPattern: currentExcludes.m_values) {
                if (currentPattern.conflictsWith(currentPattern)) {
                    continue mainloop;
                }
            }
            for (BaseUrlPattern refPattern: refExcludes.m_values) {
                if (pattern.conflictsWith(refPattern)) {
                    continue mainloop;
                }
            }
            // verify include patterns
            for (BaseUrlPattern refPattern : refIncludes.m_values) {
                if (pattern.conflictsWith(refPattern)) {
                    throw new NoSuccessException("Pattern '"+pattern.toString()+"' of context "+currentLabel+
                            " conflicts with pattern '"+refPattern.toString()+"' of context "+refLabel);
                }
            }
        }
    }

    public boolean matches(String url) {
        for (BaseUrlPattern pattern : m_values) {
            if (pattern.matches(url)) {
                return true;
            }
        }
        return false;
    }

    public String getSchemeFromAlias(String alias) {
        return m_aliases.getProperty(alias);
    }

    private static List<String> split(String url) {
        List<String> list = new ArrayList<String>();
        int open = url.indexOf('{');
        if (open > -1) {
            String begin = url.substring(0, open);
            String remains = url.substring(open+1);
            int close = remains.indexOf('}');
            if (close > -1) {
                String or = remains.substring(0, close);
                String end = remains.substring(close+1);
                // recurse
                for (String child : or.split(",")) {
                    String childUrl = begin.trim()+child.trim()+end.trim();
                    childUrl = childUrl.replaceAll("\\*\\*", "*");
                    List<String> childList = split(childUrl);
                    list.addAll(childList);
                }
            }
        } else {
            list.add(url);
        }
        return list;
    }
}
