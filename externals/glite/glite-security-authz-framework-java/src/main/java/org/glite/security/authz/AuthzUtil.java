package org.glite.security.authz;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Set;

/**
 * Utility class with miscellaneous authorizaion utilities.
 */
public final class AuthzUtil {
   private AuthzUtil() {
   }
   /**
    * gets the identity from the specified subject.
    * @param subject subject to get the subject dn from
    * @return subject dn of first contained principal or null if none
    *         was found
    */
   public static String getIdentity(Subject subject) {
        Set set = subject.getPrincipals();
        if ((set == null) || (set.size() < 1)) {
            return null;
        }
        return ((Principal) set.iterator().next()).getName();
    }
}

