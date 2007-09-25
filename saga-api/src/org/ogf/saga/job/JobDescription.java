package org.ogf.saga.job;

import org.ogf.saga.SagaBase;
import org.ogf.saga.attributes.Attributes;

/**
 * Contents of a job description is defined by its attributes.
 * Should we have separate methods for each of the attributes???
 */
public interface JobDescription extends SagaBase, Attributes {
    // Empty interface: it is all attributes.
}
