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
package org.globus.wsrf.container;

import java.io.OutputStream;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisEngine;

import org.gridforum.jgss.ExtendedGSSContext;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;

import org.globus.axis.gsi.GSIConstants;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.jaas.GlobusPrincipal;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;


class GSIServiceThread extends ServiceThread {
    static Log logger = LogFactory.getLog(GSIServiceThread.class.getName());
    private GSSCredential credentials;
    private static I18n i18n = I18n.getI18n(Resources.class.getName());


    public GSIServiceThread(
        ServiceRequestQueue queue,
        ServiceThreadPool pool,
        AxisEngine engine,
        GSSCredential credentials
    ) {
        super(queue, pool, engine);
        setCredentials(credentials);
        logger.debug(getName() + ": Thread created");
    }

    public void setCredentials(GSSCredential credentials) {
        this.credentials = credentials;
    }

    protected String getProtocol() {
        return "https";
    }

    protected void process(ServiceRequest request) {
        logger.debug(getName() + ": processing requests");

        GSSManager manager = ExtendedGSSManager.getInstance();

        GssSocket gsiSocket = null;
        OutputStream out = null;

        try {
            ExtendedGSSContext context =
                (ExtendedGSSContext) manager.createContext(this.credentials);

            context.setOption(GSSConstants.GSS_MODE, GSIConstants.MODE_SSL);

            context.setOption(GSSConstants.ACCEPT_NO_CLIENT_CERTS,
                              Boolean.TRUE);

            GssSocketFactory factory = GssSocketFactory.getDefault();

            gsiSocket =
                (GssSocket) factory.createSocket(
                    request.getSocket(), null, 0, context
                );

            // server socket
            gsiSocket.setUseClientMode(false);
            gsiSocket.setAuthorization(null);

            // forces handshake
            out = gsiSocket.getOutputStream();

            String globusID = context.getSrcName().toString();

            logger.debug(getName() + ": Authenticated globus user: " + globusID);

            Subject subject = getSubject();
            subject.getPrincipals().add(new GlobusPrincipal(globusID));

            // Don't set the context since it interferes with secure conversation
            //this.msgContext.setProperty(Constants.CONTEXT, context);

            if (context.getConfState()) {
                this.msgContext.setProperty(Constants.GSI_TRANSPORT,
                                            Constants.ENCRYPTION);
            } else if (context.getIntegState()) {
                this.msgContext.setProperty(Constants.GSI_TRANSPORT,
                                            Constants.SIGNATURE);
            } else {
                this.msgContext.setProperty(Constants.GSI_TRANSPORT,
                                            Constants.NONE);
            }
        } catch (Exception e) {
            if (gsiSocket != null) {
                try {
                    gsiSocket.close();
                } catch (Exception ee) {
                }
            }
            logger.error(i18n.getMessage("requestFailed"), e);

            return;
        }

        ServiceRequest req =
            new ServiceRequest(gsiSocket, request.getServerSocket());
        super.process(req);
    }

    protected Subject getSubject() {
        Subject subject =
            (Subject) msgContext.getProperty(Constants.PEER_SUBJECT);

        if (subject == null) {
            subject = new Subject();
            msgContext.setProperty(Constants.PEER_SUBJECT, subject);
        }

        return subject;
    }
}
