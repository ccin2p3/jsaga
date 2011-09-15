package fr.in2p3.jsaga.helpers;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StringArray
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   5 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class StringArray {
    public static boolean arrayContains(String[] array, String value) {
        for (int i=0; array!=null && i<array.length; i++) {
            if (array[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String arrayToString(String[] array, String separator) {
        if (array!=null && array.length>0) {
            StringBuffer buffer = new StringBuffer(array[0]);
            for (int i=1; i<array.length; i++) {
                buffer.append(separator);
                buffer.append(array[i]);
            }
            return buffer.toString();
        } else {
            return null;
        }
    }
}
