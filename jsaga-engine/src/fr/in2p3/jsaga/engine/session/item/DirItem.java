package fr.in2p3.jsaga.engine.session.item;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DirItem
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DirItem extends BaseUrlItem {
    public DirItem(String dir, Object preDir, Object postDir) {
        super(dir, preDir, postDir);
    }
    public DirItem() {
        super();
    }

    @Override
    protected boolean isRequired(boolean hasValue) {
        return true;
    }

    @Override
    protected String getSimpleSeparator() {
        return "/";
    }

    @Override
    protected String getRegExpSeparator() {
        return "/";
    }

    @Override
    protected String getRegExpSeparatorNext() {
        return "/";
    }

    @Override
    protected String getAllowedChars() {
        return "^/";
    }
}
