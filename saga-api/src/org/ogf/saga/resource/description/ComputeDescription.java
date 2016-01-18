package org.ogf.saga.resource.description;

import org.ogf.saga.resource.description.ResourceDescription;

public interface ComputeDescription extends ResourceDescription {
    /**
     * Attribute name: the machine operating system (default = Any)
     */
    public static final String MACHINE_OS = "MachineOS";

    /**
     * Attribute name: the machine architecture (default = Any)
     */
    public static final String MACHINE_ARCH = "MachineArch";

    /**
     * Attribute name: the number of slots (default = 1)
     */
    public static final String SIZE = "Size";

    /**
     * Attribute name: a list of strings
     */
    public static final String HOST_NAMES = "HostNames";

    /**
     * Attribute name: a long integer
     */
    public static final String MEMORY = "Memory";

    /**
     * Attribute name: FDHN or IP
     */
    public static final String ACCESS = "Access";
}
