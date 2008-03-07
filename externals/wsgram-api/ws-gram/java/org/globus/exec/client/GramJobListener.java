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
package org.globus.exec.client;

/**
 * This interface is used to allow client-side objects to
 * listen for status changes of GramJob objects.
 *
 */
public interface GramJobListener {

  /**
   * This method is used to notify the implementer when the status of a
   * GramJob has changed.
   *
   * @param job The GramJob whose status has changed.
   */
  public void stateChanged(GramJob job);

}
