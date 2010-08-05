package fr.in2p3.jsaga.helpers;

import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SAGAPattern
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SAGAPattern {
    /**
     * Convert wildcards to a regular expression.
     * @param sagaPattern a pattern with wildcards as specified in "SAGA Name Spaces".
     * @return a compiled regular expression.
     */
    public static Pattern toRegexp(String sagaPattern) {
        if (sagaPattern==null || sagaPattern.equals("") || sagaPattern.equals("*")) {
            // match all
            return null;
        } else {
            String regexp = sagaPattern;

            // escape some characters
            regexp = regexp.replaceAll("\\.", "\\\\.");

            // convert wildcards to regular expression
            regexp = regexp.replaceAll("\\*", ".*");
            regexp = regexp.replaceAll("\\?", ".?");
            regexp = regexp.replaceAll("\\[^", "[^");

            // compile regular expression
            return Pattern.compile(regexp);
        }
    }

    public static boolean hasWildcard(String sagaPattern) {
        return (sagaPattern==null || sagaPattern.equals("") || sagaPattern.contains("*") || sagaPattern.contains("?"));
    }
}
