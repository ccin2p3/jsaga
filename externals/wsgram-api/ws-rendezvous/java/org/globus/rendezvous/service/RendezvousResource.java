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
package org.globus.rendezvous.service;

import org.globus.wsrf.ResourceProperties;

import org.globus.rendezvous.generated.RankTakenFaultType;

/**
 * A distributed registry for rendezvous of remote registrants.
 *
 */
public interface RendezvousResource extends ResourceProperties {

    /**
     * Register input data with rank.
     * <p>
     * <b>Precondition:</b> !isFull()
     * @param data byte[] Binary data to register.
     * @param inputRank int Desired rank to associate with the registrant.
     *                      Value of -1 implies that rank should be assigned
     *                      automatically.
     * @return int Rank of this registrant
     */
    public int register(byte[] data, int inputRank) throws RankTakenFaultType;

    public boolean isFull();

}
