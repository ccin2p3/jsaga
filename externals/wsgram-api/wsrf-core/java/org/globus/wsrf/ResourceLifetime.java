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

/**
 * Interface that contains operations which expose the state associated with
 * resource lifetime.
 */
public interface ResourceLifetime
{
    /**
     * Set the termination time
     *
     * @param time The termination time to set
     */
    public void setTerminationTime(Calendar time);

    /**
     * Get the termination time
     *
     * @return The termination time
     */
    public Calendar getTerminationTime();

    /**
     * Get the current time
     *
     * @return The current time
     */
    public Calendar getCurrentTime();
}
