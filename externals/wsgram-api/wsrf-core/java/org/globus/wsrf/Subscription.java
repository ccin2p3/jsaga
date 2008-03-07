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
package org.globus.wsrf;

import java.util.Calendar;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;

import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.QueryExpressionType;

/**
 * Interface to be implemented by subscription resources. Exposes the
 * information associated with a subscription request as well as resource
 * lifetime related state.
 */
public interface Subscription extends Resource, ResourceLifetime
{
    /**
     * Is the subscription paused?
     *
     * @return True if the subscription is paused, false if not.
     */
    boolean isPaused();

    /**
     * Pause the subscription
     *
     * @throws Exception
     */
    void pause() throws Exception;

    /**
     * Resume the subscription
     *
     * @throws Exception
     */
    void resume() throws Exception;

    /**
     * Get the consumer endpoint reference associated with this subscription
     *
     * @return The consumer endpoint reference
     */
    EndpointReferenceType getConsumerReference();

    /**
     * Get the producer endpoint reference associated with this subscription
     *
     * @return The producer endpoint reference
     */
    EndpointReferenceType getProducerReference();

    /**
     * Get the topic expression associated with this subscription
     *
     * @return The topic expression
     */
    TopicExpressionType getTopicExpression();

    /**
     * Get the precondition associated with this subscription
     *
     * @return The precondition. May be null if no precondition was specified in
     *         the subscription.
     */
    QueryExpressionType getPrecondition();

    /**
     * Get the selector expression associated with this subscription
     *
     * @return The selector. May be null if no selector was specified in the
     *         subscription.
     */
    QueryExpressionType getSelector();

    /**
     * Get the policy associated with this subscription
     *
     * @return The policy. May be null if no policy was specified in the
     *         subscription.
     */
    Object getSubscriptionPolicy();

    /**
     * Wrap notification messages in the notify element?
     *
     * @return True (default) if notify should be used, false if not.
     */
    boolean getUseNotify();

    /**
     * Get the producing resource
     *
     * @return The producing resource
     */
    Object getResource() throws Exception;

    /**
     * Get the time at which the resource was created.
     *
     * @return The creation time
     */
    public Calendar getCreationTime();

    /**
     * Get security properties that determine security for the
     * notification call. If any. Returns null for insecure subcriptions
     *
     * @return map of security properties.
     */
    public ClientSecurityDescriptor getSecurityProperties();
}
