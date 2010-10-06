package fr.in2p3.jsaga.engine.session.item;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   PortItem
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class PortItem extends BaseUrlItem {
    public static final boolean REQUIRED = true;
    public static final boolean OPTIONAL = false;

    private boolean m_isRequired;

    public PortItem(String port, boolean isRequired) {
        super(port, null, null);
        m_isRequired = isRequired;
    }
    public PortItem() {
        super();
        m_isRequired = false;
    }

    @Override
    protected boolean isRequired(boolean hasValue) {
        return m_isRequired;
    }

    @Override
    protected String getSimpleSeparator() {
        return ":";
    }

    @Override
    protected String getRegExpSeparator() {
        return ":";
    }

    @Override
    protected String getRegExpSeparatorNext() {
        return "/";
    }

    @Override
    protected String getAllowedChars() {
        return "\\p{Digit}";
    }
}
