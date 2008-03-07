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
package org.globus.wsrf.security.authorization;

import org.w3c.dom.Node;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * Interface that must be implemented by all PDPs in an interceptor chain
 * A PDP is responsible for making decisions whether a subject is
 * allowed to invoke a certain operation. The subject may contain public
 * or private credentials holding attributes collected and verified by PIPs.
 * A PDP is also responsible for managing a policy associated with a service.
 * The service is associated with the PDP in the initialize call in
 * {@link Interceptor} through the id parameter.
 *
 * @see org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain
 * @see PIP
 */
public interface PDP extends Interceptor {
    /**
     * gets the names (typically uris) of all the policies that
     * the PDP supports
     * @return array of policy names
     */
    public String[] getPolicyNames();

    /**
     * gets the current policy of the PDP
     * @param query may be used to query for a subset of a policy
     * @return the policy
     */
    public Node getPolicy(Node query) throws InvalidPolicyException;

    /**
     * sets the current policy of the PDP
     * @param policy new policy
     * @return optional set policy result
     */
    public Node setPolicy(Node policy) throws InvalidPolicyException;

    /**
     * this operation is called by the PDP Framework whenever the application
     * needs to call secured operations. The PDP should return true if the
     * local policy allows the subject to invoke the operation. If the PDP
     * has no local knowledge about whether the operation is allowed or not
     * it should return false to allow other PDPs and PIPs in the chain to
     * continue the evaluation. Obligations to be read by other PIPs or PDPs
     * may be set as attributes in the Subject credentials.
     * @param peerSubject authenticated client subject with credentials
     *                    and attributes
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     */
    public boolean isPermitted(Subject peerSubject,
                               MessageContext context,
                               QName operation) throws AuthorizationException;
}
