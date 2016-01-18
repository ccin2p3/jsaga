package org.ogf.saga.resource;

public enum Type {
    /** accepting jobs */
    COMPUTE,

    /** connected compute(s) and storage(s) */
    NETWORK,

    /** mounted on compute, or accessible by it */
    STORAGE
}
