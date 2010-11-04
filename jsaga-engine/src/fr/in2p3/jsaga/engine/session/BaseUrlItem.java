package fr.in2p3.jsaga.engine.session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BaseUrlItem
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public abstract class BaseUrlItem {
    protected static final String TOKEN = "\\p{Alnum}-_";

    private String m_value;
    private Mode m_mode;

    public BaseUrlItem(String value, Object pre, Object post) {
        if (value != null) {
            m_value = value;
            if (pre==null && post==null) {
                m_mode = Mode.EQUALS;
            } else if (pre==null && post!=null) {
                m_mode = Mode.STARTS_WITH;
            } else if (pre!=null && post==null) {
                m_mode = Mode.ENDS_WITH;
            } else {
                m_mode = Mode.CONTAINS;
            }
        } else {
            m_value = null;
            m_mode = Mode.ANY;
        }
    }
    public BaseUrlItem() {
        m_value = null;
        m_mode = Mode.ANY;
    }

    public String getValue() {
        return m_value;
    }

    public Mode getMode() {
        return m_mode;
    }

    public String toString() {
        String separator = this.getSimpleSeparator();
        final String any = "*";
        if (m_value != null) {
            String expr = separator+this.toExpression(m_value, any);
            if (this.isRequired(true)) {
                return expr;
            } else {
                return "["+expr+"]";
            }
        } else {
            String expr = separator+any;
            if (this.isRequired(false)) {
                return expr;
            } else {
                return "";
            }
        }
    }
    public String toRegExp() {
        String separator = this.getRegExpSeparator();
        String any = "["+this.getAllowedChars()+"]*";
        if (m_value != null) {
            String expr = separator+this.toExpression(m_value.replaceAll("\\.", "\\\\."), any);
            if (this.isRequired(true)) {
                return expr;
            } else {
                return "("+expr+")?";
            }
        } else {
            String expr = separator+any;
            if (this.isRequired(false)) {
                return expr;
            } else {
                return "("+expr+")?";
            }
        }
    }
    private String toExpression(String value, String any) {
        switch (m_mode) {
            case EQUALS:
                return value;
            case STARTS_WITH:
                return value+any;
            case ENDS_WITH:
                return any+value;
            case CONTAINS:
                return any+value+any;
            default:
                throw new RuntimeException("[INTERNAL ERROR] unexpected mode: "+m_mode);
        }
    }

    public String getNextRegExp() {
        return "("+this.getRegExpSeparatorNext()+".*)?";
    }

    public boolean conflictsWith(BaseUrlItem ref) {
        if (Mode.ANY.equals(m_mode) || Mode.ANY.equals(ref.getMode())) {
            return true;
        } else {
            String refValue = ref.getValue();
            switch (m_mode) {
                case EQUALS:
                    switch (ref.getMode()) {
                        case EQUALS: return m_value.equals(refValue);
                        case STARTS_WITH: return m_value.startsWith(refValue);
                        case ENDS_WITH: return m_value.endsWith(refValue);
                        case CONTAINS: return m_value.contains(refValue);
                    }
                case STARTS_WITH:
                    switch (ref.getMode()) {
                        case EQUALS: return refValue.startsWith(m_value);
                        case STARTS_WITH: return refValue.startsWith(m_value) || m_value.startsWith(refValue);
                        case ENDS_WITH: return true;
                        case CONTAINS: return true;
                    }
                case ENDS_WITH:
                    switch (ref.getMode()) {
                        case EQUALS: return refValue.endsWith(m_value);
                        case STARTS_WITH: return true;
                        case ENDS_WITH: return refValue.endsWith(m_value) || m_value.endsWith(refValue);
                        case CONTAINS: return true;
                    }
                case CONTAINS:
                    switch (ref.getMode()) {
                        case EQUALS: return refValue.contains(m_value);
                        case STARTS_WITH: return true;
                        case ENDS_WITH: return true;
                        case CONTAINS: return true;
                    }
            }
            throw new RuntimeException("[INTERNAL ERROR] unexpected mode: "+m_mode+" or "+ref.getMode());
        }
    }

    protected abstract boolean isRequired(boolean hasValue);
    protected abstract String getSimpleSeparator();
    protected abstract String getRegExpSeparator();
    protected abstract String getRegExpSeparatorNext();
    protected abstract String getAllowedChars();
}
