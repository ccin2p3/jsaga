package fr.in2p3.jsaga.engine.session.item;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SchemeItem
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class SchemeItem extends BaseUrlItem {
    private String m_type;

    public SchemeItem(String scheme, String alias) {
        super(alias!=null?alias:scheme, null, null);
        m_type = alias!=null?scheme:null;
    }

    public String getSchemeOrNull() {
        return m_type;
    }

    @Override
    public String toString() {
        if (m_type!= null) {
            return super.toString()+"->"+m_type;
        } else {
            return super.toString();
        }
    }

    @Override
    protected boolean isRequired(boolean hasValue) {
        return true;
    }

    @Override
    protected String getSimpleSeparator() {
        return "";
    }

    @Override
    protected String getRegExpSeparator() {
        return "";
    }

    @Override
    protected String getRegExpSeparatorNext() {
        return "://";
    }

    @Override
    protected String getAllowedChars() {
        return TOKEN;
    }
}
