package fr.in2p3.jsaga.engine.session;

import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BaseUrlPattern
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class BaseUrlPattern {
    private BaseUrlItem[] m_items;

    public BaseUrlPattern(BaseUrlItem... items) {
        m_items = items;
    }

    public BaseUrlItem[] getItems() {
        return m_items;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (BaseUrlItem item : m_items) {
            buffer.append(item.toString());
        }
        return buffer.toString();
    }

    // accessible to friends for testing purpose
    Pattern toRegExp() {
        StringBuffer buffer = new StringBuffer();
        String nextRegExp = "";
        for (BaseUrlItem item : m_items) {
            buffer.append(item.toRegExp());
            nextRegExp = item.getNextRegExp();
        }
        buffer.append(nextRegExp);
        return Pattern.compile(buffer.toString());
    }

    public boolean matches(String url) {
        Pattern regexp = this.toRegExp();
        return regexp.matcher(url).matches();
    }

    public boolean conflictsWith(BaseUrlPattern ref) {
        for (int i=0; i<m_items.length && i<ref.getItems().length; i++) {
            if (! m_items[i].conflictsWith(ref.getItems()[i])) {
                return false;
            }
        }
        return true;
    }
}
