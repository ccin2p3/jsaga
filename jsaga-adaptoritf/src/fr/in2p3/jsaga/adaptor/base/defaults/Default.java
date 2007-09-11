package fr.in2p3.jsaga.adaptor.base.defaults;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Default
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Default {
    private String m_name;
    private String m_value;

    public Default(String name, String value) {
        m_name = name;
        m_value = value;
    }
    public Default(String name, String[] values) {
        this(name, getFirstNotNull(values));
    }
    public Default(String name, File[] values) {
        this(name, getFirstExisting(values));
    }

    public String getName() {
        return m_name;
    }

    public String getValue() {
        return m_value;
    }

    private static String getFirstNotNull(String[] values) {
        for (int i=0; values!=null && i<values.length; i++) {
            if (values[i] != null) {
                return values[i];
            }
        }
        return null;
    }

    private static String getFirstExisting(File[] values) {
        for (int i=0; values!=null && i<values.length; i++) {
            if (values[i]!=null && values[i].exists()) {
                return values[i].getAbsolutePath();
            }
        }
        return null;
    }
}
