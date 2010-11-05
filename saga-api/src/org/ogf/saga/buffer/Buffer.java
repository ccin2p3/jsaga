package org.ogf.saga.buffer;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

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
     *      the size.
     * @exception IncorrectStateException
     *      is thrown when the buffer is closed.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the specified size.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void setSize(int size) throws BadParameterException,
            IncorrectStateException, NoSuccessException;

    /**
     * Sets the size of the buffer. This method is semantically equivalent to
     * re-creating it. This method makes the buffer implementation-managed. Spec
     * inconsistency: this method should also throw NoSuccess, as the
     * constructor can throw this, and this method is semantically equivalent to
     * destruct and then call constructor.
     * @exception IncorrectStateException
     *      is thrown when the buffer is closed.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the default size.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void setSize() throws BadParameterException,
            IncorrectStateException, NoSuccessException;

    /**
     * Retrieves the current value of the buffer size.
     * 
     * @return
     *      the size.
     * @exception IncorrectStateException
     *      is thrown when the buffer is closed.
     */
    public int getSize() throws IncorrectStateException;

    /**
     * Sets the buffer data. Makes the buffer application-managed. Deviation
     * from the SAGA specs: the size is implicit in the byte array. Calling this
     * method implies: user-allocated data. Spec inconsistency: this method
     * should also throw NoSuccess, as the constructor can throw this, and this
     * method is semantically equivalent to destruct and then call constructor.
     * 
     * @param data
     *      the data.
     * @exception IncorrectStateException
     *      is thrown when the buffer is closed.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the specified data buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void setData(byte[] data) throws BadParameterException,
            IncorrectStateException, NoSuccessException;

    /**
     * Retrieves the buffer data.
     * 
     * @return
     *      the data.
     * @exception IncorrectStateException
     *      is thrown when the buffer is closed.
     * @exception DoesNotExistException
     *      is thrown when the buffer was created with size -1, and no
     *      I/O operation has been done on it yet.
     */
    public byte[] getData() throws DoesNotExistException, IncorrectStateException;

    /**
     * Non-blocking close of the buffer object.
     */
    public void close();

    /**
     * Closes the buffer object.
     * 
     * @param timeoutInSeconds
     *      he timeout in seconds.
     */
    public void close(float timeoutInSeconds);
}
