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
package org.globus.rendezvous.service.utils;

import java.util.ListResourceBundle;

public class Resources extends ListResourceBundle {
    public Object[][] getContents() {
        return contents;
    }

    public static final String RANK_TAKEN_ERROR = "RankTakenError";

    public static final String REFLECTION_METHOD_INVOCATION_ERROR = "MethodInvocationError";

    public static final String RENDEZVOUS_FULL_ERROR = "RendezvousFullError";

    public static final String EPR_CREATION_ERROR = "EPRCreationError";

    public static final String FAULT_INSTANCIATION_ERROR = "FaultInstanciationError";

    public static final String PRECONDITION_VIOLATION = "PreconditionViolation";

    static final Object[][] contents = {
        {
            PRECONDITION_VIOLATION,
            "Precondition violation: {0}"
        },
        {
            RANK_TAKEN_ERROR,
            "Registration failed: Rank {0} already taken"
        },
        {
            REFLECTION_METHOD_INVOCATION_ERROR,
            "Could not find or invoke method {0} in class {1}"
        },
        {
            RENDEZVOUS_FULL_ERROR,
            "Registration failed: the Rendezvous resource is full"
        },
        {
            EPR_CREATION_ERROR,
            "Error creating EPR. EPR will be null"
        },
        {
            FAULT_INSTANCIATION_ERROR,
            "problem instantiating fault constructor"
        }
    };
}
