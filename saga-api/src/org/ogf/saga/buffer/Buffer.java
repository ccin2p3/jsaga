package org.ogf.saga.buffer;

import org.ogf.saga.SagaBase;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NotImplemented;

/**
 * Encapsulates a sequence of bytes to be used for I/O operations.
 * ??? Should we just use java.nio.ByteBuffer instead ??? Probably yes.
 * ???Java mostly uses <code>int</code> for buffer sizes. Should we use
 * <code>long</code>???
 */
public interface Buffer extends SagaBase {

    /**
     * Sets the size of the buffer. This method is semantically equivalent
     * to re-creating it with the specified size.
     * This makes the buffer implementation-allocated, unless size = -1,
     * in which case it becomes only implementation-managed.
     * @param size the size.
     */
    public void setSize(int size)
        throws NotImplemented, BadParameter;

    /**
     * Retrieves the current value of the buffer size.
     * @return the size.
     */
    public int getSize()
        throws NotImplemented, IncorrectState;

    /**
     * Sets the buffer data. Makes the buffer application-managed.
     * Deviation from the SAGA specs: the size is implicit in the byte
     * array. Calling this method implies: user-allocated data.
     * @param data the data.
     */
    public void setData(byte[] data)
        throws NotImplemented, BadParameter;

    /**
     * Retrieves the buffer data.
     * @return the data.
     * @exception DoesNotExist is thrown when the buffer was created with
     *     size -1, and no I/O operation has been done on it yet.
     */
    public byte[] getData()
        throws NotImplemented, DoesNotExist, IncorrectState;

    /**
     * Non-blocking close of the buffer object.
     */
    public void close()
        throws NotImplemented, IncorrectState;

    /**
     * Closes the buffer object.
     * @param timeoutInSeconds the timeout in seconds.
     */
    public void close(float timeoutInSeconds)
        throws NotImplemented, IncorrectState;
}
