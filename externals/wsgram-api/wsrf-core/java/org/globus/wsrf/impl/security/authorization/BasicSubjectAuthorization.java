/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.impl.security.authorization;

import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.rpc.handler.MessageContext;

import java.security.Principal;

import java.util.Iterator;
import java.util.Set;

import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * Base class used to do subject based authorization.
 */
public abstract class BasicSubjectAuthorization {

    private static Log logger =
        LogFactory.getLog(BasicSubjectAuthorization.class.getName());

    static I18n i18n =
        I18n.getI18n(Authorization.RESOURCE,
                     IdentityAuthorization.class.getClassLoader()
        );

    /**
     * Matches to see if localSubject is equals to peerSubject
     */
    protected boolean authorize(Subject localSubject,
                                Subject peerSubject,
                                MessageContext context
                                ) throws AuthorizationException {
        if (peerSubject == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("noPeerSubject")
            );
        }

        if (localSubject == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("noLocalSubject")
            );
        }

        Set localPrincipals = localSubject.getPrincipals();

        if ((localPrincipals == null) || localPrincipals.isEmpty()) {
            logger.debug( i18n.getMessage("noLocalPrincipals"));
            throw new AuthorizationException(i18n
                                             .getMessage("noLocalPrincipals"));
        }

        Set peerPrincipals = peerSubject.getPrincipals();

        if ((peerPrincipals == null) || peerPrincipals.isEmpty()) {
            logger.debug(i18n.getMessage("anonPeer"));
            throw new AuthorizationException(i18n.getMessage("anonPeer"));
        }

        Iterator iter = localPrincipals.iterator();

        while (iter.hasNext()) {
            Principal principal = (Principal) iter.next();

            if (peerPrincipals.contains(principal)) {
                logger.debug(i18n.getMessage("identityAuthSuccess",
                                             principal));
                return true;
            }
        }

        logger.warn(
             i18n.getMessage(
               "identityAuthFail",
                new Object[] { localPrincipals, peerPrincipals }
             )
        );

        return false;
    }
}
