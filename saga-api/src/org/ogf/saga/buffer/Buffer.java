package org.ogf.saga.buffer;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

/**
 * Encapsulates a sequence of bytes to be used for I/O operations.
 */
public interface Buffer extends SagaObject {

    /**
     * Sets the size of the buffer. This method is semantically equivalent to
     * re-creating it with the specified size. This makes the buffer
     * implementation-allocated, unless size = -1, in which case it becomes
     * implementation-managed. Spec inconsistency: this method should also throw
     * NoSuccess, as the constructor can throw this, and this method is
     * semantically equivalent to destruct and then call constructor.
     * 
     * @param size
     *            the size.
     */
    public void setSize(int size) throws NotImplementedException,
            BadParameterException, IncorrectStateException, NoSuccessException;

    /**
     * Sets the size of the buffer. This method is semantically equivalent to
     * re-creating it. This method makes the buffer implementation-managed. Spec
     * inconsistency: this method should also throw NoSuccess, as the
     * constructor can throw this, and this method is semantically equivalent to
     * destruct and then call constructor.
     */
    public void setSize() throws NotImplementedException,
            BadParameterException, IncorrectStateException, NoSuccessException;

    /**
     * Retrieves the current value of the buffer size.
     * 
     * @return the size.
     */
    public int getSize() throws NotImplementedException,
            IncorrectStateException;

    /**
     * Sets the buffer data. Makes the buffer application-managed. Deviation
     * from the SAGA specs: the size is implicit in the byte array. Calling this
     * method implies: user-allocated data. Spec inconsistency: this method
     * should also throw NoSuccess, as the constructor can throw this, and this
     * method is semantically equivalent to destruct and then call constructor.
     * 
     * @param data
     *            the data.
     */
    public void setData(byte[] data) throws NotImplementedException,
            BadParameterException, IncorrectStateException, NoSuccessException;

    /**
     * Retrieves the buffer data.
     * 
     * @return the data.
     * @exception DoesNotExistException
     *                is thrown when the buffer was created with size -1, and no
     *                I/O operation has been done on it yet.
     */
    public byte[] getData() throws NotImplementedException,
            DoesNotExistException, IncorrectStateException;

    /**
     * Non-blocking close of the buffer object.
     */
    public void close() throws NotImplementedException;

    /**
     * Closes the buffer object.
     * 
     * @param timeoutInSeconds
     *            the timeout in seconds.
     */
    public void close(float timeoutInSeconds) throws NotImplementedException;
}
