package fr.in2p3.jsaga;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Base
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Base {
    public static final File JSAGA_HOME =
            System.getProperty("JSAGA_HOME")!=null
                    ? new File(System.getProperty("JSAGA_HOME"))
                    : new File("jsaga-engine/config").exists()
                        ? new File("jsaga-engine/config")
                        : (new File("config").exists()
                            ? new File("config")
                            : new File("."));

    public static final File JSAGA_VAR = new File(JSAGA_HOME, "var");
    static {
        if(!JSAGA_VAR.exists()) {
            JSAGA_VAR.mkdir();
        }
    }

    public static final boolean DEBUG =
            System.getProperty("debug")!=null && !System.getProperty("debug").equalsIgnoreCase("false");
}
