package org.ogf.saga.file;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.BadParameter;

/**
 * Extends the <code>Buffer</code> interface with  lenIn and lenOut
 * attributes.
 */
public interface IOVec extends Buffer {

    // TODO offset ???

    /**
     * Sets the lenIn attribute.
     * @param len the value for the attribute.
     */
    void setLenIn(int len)
        throws BadParameter;

    /**
     * Retrieves the current value of the lenIn attribute.
     * @return the lenIn value.
     */
    int getLenIn();

    /**
     * Retrieves the current value of the lenOut attribute.
     * @return the lenOut value.
     */
    int getLenOut();
}
