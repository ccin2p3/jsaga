package fr.in2p3.jsaga.engine.session.item;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DomainItem
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DomainItem extends BaseUrlItem {
    public DomainItem(String domain, Object preDomain) {
        super(domain, preDomain, null);
    }
    public DomainItem() {
        super();
    }

    @Override
    protected boolean isRequired(boolean hasValue) {
        return hasValue;
    }

    @Override
    protected String getSimpleSeparator() {
        return ".";
    }

    @Override
    protected String getRegExpSeparator() {
        return "\\.";
    }

    @Override
    protected String getRegExpSeparatorNext() {
        return "[:/]";
    }

    @Override
    protected String getAllowedChars() {
        return TOKEN+"\\.";
    }
}
