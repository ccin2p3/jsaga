package org.glite.security.authz.pdp;

import org.glite.security.authz.PIPAttribute;
/**
 * This class represents an attribute that is used to pupulate a subject with
 * the local users found in the gridmap file.
 * @see PIPAttribute
 * @see GridMapServicePDP
 */
public class LocalUserPIPAttribute extends PIPAttribute {
    /**
     * Property defining the name of this attribute.
     */
    public static final String LOCAL_USER =
        "http://www.glite.org/security/localUser";
    /**
     * Constructor.
     * @param users local users to populate this attribute with
     */
    public LocalUserPIPAttribute(String[] users) {
        super(LOCAL_USER, users);
    }
    /**
     * gets the users in this attribute.
     * @return array of local users
     */
    public String[] getUsers() {
        return (String[]) getValue();
    }
}

