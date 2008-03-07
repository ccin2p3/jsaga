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
package org.globus.rendezvous.client;

import java.util.ListResourceBundle;

public class Resources extends ListResourceBundle {
    public Object[][] getContents() {
        return contents;
    }

    public static final String INVALID_RANK_TAKEN_ERROR = "InvalidRankTakenError";

    public static final String NOTIFICATION_DESERIALIZATION_ERROR = "NotificationDeserializationError";

    public static final String PRECONDITION_VIOLATION = "PreconditionViolation";


    static final Object[][] contents = {
        {
            PRECONDITION_VIOLATION,
            "Precondition violation: {0}"
        },
        {
            INVALID_RANK_TAKEN_ERROR,
            "Registration was w/o desired rank, so this fault should not "+
                "have been thrown - is this an implementation error?"
        },
        {
            NOTIFICATION_DESERIALIZATION_ERROR,
            "Could not deserialize rendezvous notification"
        }
    };
}
