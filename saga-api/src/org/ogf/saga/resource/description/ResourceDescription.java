package org.ogf.saga.resource.description;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;

public interface ResourceDescription extends SagaObject, Attributes {

    // Required attributes:

    /**
     * Attribute name: value is "COMPUTE", "NETWORK" or "STORAGE"
     */
    public static final String TYPE = "Type";

    // Optional attributes:

    /**
     * Attribute name: a list of strings
     */
    public static final String TEMPLATE = "Template";

    /**
     * Attribute name: a boolean (default = false)
     */
    public static final String DYNAMIC = "Dynamic";

    /**
     * Attribute name: a string
     */
    public static final String PLACEMENT = "Placement";

    /**
     * Attribute name: a date (default = now)
     */
    public static final String START = "Start";

    /**
     * Attribute name: a date
     */
    public static final String END = "End";

    /**
     * Attribute name: a duration
     */
    public static final String DURATION = "Duration";
}
