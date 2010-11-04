package fr.in2p3.jsaga.engine.session.item;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   HostItem
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class HostItem extends BaseUrlItem {
    public HostItem(String host, Object preHost, Object postHost) {
        super(host, preHost, postHost);
    }
    public HostItem() {
        super();
    }

    @Override
    protected boolean isRequired(boolean hasValue) {
        return true;
    }

    @Override
    protected String getSimpleSeparator() {
        return "://";
    }

    @Override
    protected String getRegExpSeparator() {
        return "://(["+TOKEN+"\\.]+@)?";
    }

    @Override
    protected String getRegExpSeparatorNext() {
        return "[\\.:/]";
    }

    @Override
    protected String getAllowedChars() {
        return TOKEN;
    }
}
