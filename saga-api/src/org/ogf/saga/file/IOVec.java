package org.ogf.saga.file;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.BadParameterException;

/**
 * Extends the <code>Buffer</code> interface with lenIn, lenOut, and offset
 * attributes.
 */
public interface IOVec extends Buffer {

    /**
     * Sets the lenIn attribute.
     * 
     * @param len
     *            the value for the attribute.
     * @exception BadParameterException
     *            is thrown when the lenIn is set to an illegal
     *            value (< 0 or larger than size if size != -1).
     */
    void setLenIn(int len) throws BadParameterException;

    /**
     * Retrieves the current value of the lenIn attribute.
     * 
     * @return the lenIn value.
     */
    int getLenIn();

    /**
     * Retrieves the current value of the lenOut attribute.
     * 
     * @return the lenOut value.
     */
    int getLenOut();

    /**
     * Sets the offset attribute.
     * 
     * @param offset
     *            the value for the attribute.
     * @exception BadParameterException
     *            is thrown when the offset is set to an illegal
     *            value (< 0).
     */
    void setOffset(int offset) throws BadParameterException;

    /**
     * Retrieves the current value of the offset attribute.
     * 
     * @return the offset value.
     */
    int getOffset();
}
