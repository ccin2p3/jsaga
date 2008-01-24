package fr.in2p3.jsaga.helpers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SAGAPatternFinder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SAGAPatternFinder {
    private static final Pattern PATTERN = Pattern.compile("([^=]*)(=(.*))?");
    private Map<String,String> m_attributes;

    public SAGAPatternFinder(Map<String,String> attributes) {
        m_attributes = attributes;
    }

    public String[] findKey(String... valuePatterns) {
        List<String> foundKeys = new ArrayList<String>();
        for (Map.Entry<String,String> attribute : m_attributes.entrySet()) {
            if (matches(attribute, valuePatterns)) {
                foundKeys.add(attribute.getKey());
            }
        }
        return foundKeys.toArray(new String[foundKeys.size()]);
    }

    private static boolean matches(Map.Entry<String,String> attribute, String... patterns) {
        for (String pattern : patterns) {
            Matcher matcher = PATTERN.matcher(pattern);
            if (matcher.matches()) {
                String keyPattern = matcher.group(1);
                Pattern keyRegexp = SAGAPattern.toRegexp(keyPattern);
                if (keyRegexp == null) {
                    return true;    //found
                } else {
                    if (keyRegexp.matcher(attribute.getKey()).matches()) {
                        String valuePattern = matcher.group(3);
                        Pattern valueRegexp = SAGAPattern.toRegexp(valuePattern);
                        if (valueRegexp == null) {
                            return true;    //found
                        } else {
                            if (valueRegexp.matcher(attribute.getValue()).matches()) {
                                return true;    //found
                            }
                        }
                    }
                }
            }
        }
        return false;   //not found
    }
}
