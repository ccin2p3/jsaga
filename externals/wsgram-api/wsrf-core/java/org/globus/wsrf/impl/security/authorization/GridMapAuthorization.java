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

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.config.ConfigException;

import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.impl.security.descriptor.SecurityPropertiesHelper;

import org.globus.gsi.jaas.UserNamePrincipal;
import org.globus.security.gridmap.GridMap;

import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.io.IOException;

import org.w3c.dom.Node;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * Performs gridmap authorization and implements {@link PDP}
 * interface. If the caller identity is in the
 * configured gridmap file (resource/service/container) then authz is
 * successful.
 */
public class GridMapAuthorization implements PDP {

    private static I18n i18n =
        I18n.getI18n(
            Authorization.RESOURCE, GridMapAuthorization.class.getClassLoader()
        );

    String servicePath = null;

    private static Log logger =
        LogFactory.getLog(GridMapAuthorization.class.getName());

    public static GridMapAuthorization getInstance() {
        return new GridMapAuthorization();
    }

    // FIXME: Last parameter is servicePath (by convention)
    public void initialize(PDPConfig config, String name, String servicePath_)
            throws InitializeException {
        this.servicePath = servicePath_;
        logger.debug("service " + this.servicePath);
    }

    // FIXME
    public String[] getPolicyNames() {
        return null;
    }

    // FIXME
    public Node getPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }

    // FIXME
    public Node setPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }

    public void close() throws CloseException {
        this.servicePath = null;
    }


    public boolean isPermitted(Subject peerSubject, MessageContext context,
                               QName op) throws AuthorizationException {

        logger.debug("Grid map authz");

        if (peerSubject == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("noPeerSubject"));
        }

        // get resource
        Resource resource = null;
        try {
            ResourceContext ctx = ResourceContext
                .getResourceContext((org.apache.axis.MessageContext)context);
            resource = ctx.getResource();
        } catch (ResourceContextException exp) {
            // FIXME: quiet catch. need to have more specific exceptions
            // from core.
            logger.debug("Error retrieving resource", exp);
            resource = null;
        } catch (ResourceException exp) {
            // FIXME: quiet catch. need to have more specific exceptions
            // from core.
            logger.debug("Error retrieving resource", exp);
            resource = null;
        }

        logger.debug("Service " + this.servicePath);
        GridMap gridMap = null;
        try {
            gridMap = SecurityPropertiesHelper.getGridMap(this.servicePath,
                                                          resource);
        } catch (ConfigException exp) {
            logger.debug(i18n.getMessage("errGridMap"), exp);
            throw new AuthorizationException(i18n.getMessage("errGridMap"));
        }

        if (gridMap == null) {
            throw new IllegalStateException(i18n.getMessage("noGridmap"));
        }

        // make sure gridmap is up-to-date
        try {
            gridMap.refresh();
        } catch (IOException e) {
            logger.error(i18n.getMessage("gridmapRefreshFail"), e);
            throw new AuthorizationException(
                i18n.getMessage("gridmapRefreshFail"), e
            );
        }

        String peerIdentity = AuthUtil.getIdentity(peerSubject);

        if (peerIdentity == null) {
            logger.debug(i18n.getMessage("anonPeer"));
            throw new AuthorizationException(i18n.getMessage("anonPeer"));
        }

        String[] username = gridMap.getUserIDs(peerIdentity);

        if ((username == null) || (username.length <=0)) {
            logger.warn(i18n.getMessage("gridmapAuthFailed", peerIdentity));
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(i18n.getMessage("gridmapAuthSuccess",
                                         new Object[] {peerIdentity,
                                                       username[0]}));
        }

        for (int i=0; i<username.length; i++) {
            peerSubject.getPrincipals()
                .add(new UserNamePrincipal(username[i]));
        }

        return true;
    }
}
