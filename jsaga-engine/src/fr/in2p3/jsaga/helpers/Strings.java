package fr.in2p3.jsaga.helpers;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   Strings
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class Strings {
    public static String substringBeforeFirst(String str, char separator) {
        int pos = str.indexOf(separator);
        if (pos > -1) {
            return str.substring(0, pos);
        } else {
            return str;
        }
    }

    public static String substringBeforeLast(String str, char separator) {
        int pos = str.lastIndexOf(separator);
        if (pos > -1) {
            return str.substring(0, pos);
        } else {
            return str;
        }
    }

    public static String substringAfterFirst(String str, char separator) {
        int pos = str.indexOf(separator);
        if (pos > -1) {
            return str.substring(pos+1);
        } else {
            return str;
        }
    }

    public static String substringAfterLast(String str, char separator) {
        int pos = str.lastIndexOf(separator);
        if (pos > -1) {
            return str.substring(pos+1);
        } else {
            return str;
        }
    }
}
