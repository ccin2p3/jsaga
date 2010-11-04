package org.ogf.saga.buffer;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

/**
 * Factory for creating buffers.
 */
public abstract class BufferFactory {

    private static BufferFactory factory;

    private synchronized static void initFactory()
            throws NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createBufferFactory();
        }
    }

    /**
     * Creates a buffer. To be provided by an implementation.
     * 
     * @param data
     *            the storage.
     * @return the buffer.
     */
    protected abstract Buffer doCreateBuffer(byte[] data)
            throws BadParameterException, NoSuccessException;


    /**
     * Creates a buffer. To be provided by an implementation.
     * 
     * @param size
     *            the size of the buffer.
     * @return the buffer.
     */
    protected abstract Buffer doCreateBuffer(int size)
            throws BadParameterException, NoSuccessException;

    /**
     * Creates a (application-allocated) buffer. The size is implicit in the
     * size of the specified array.
     * 
     * @param data
     *      the storage.
     * @return
     *      the buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the specified data buffer.
     */
    public static Buffer createBuffer(byte[] data)
            throws BadParameterException, NoSuccessException {
        initFactory();
        return factory.doCreateBuffer(data);
    }

    /**
     * Creates a (implementation-managed) buffer of the specified size.
     * 
     * @param size
     *      the size.
     * @return
     *      the buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the specified size.
     */
    public static Buffer createBuffer(int size) throws BadParameterException, 
            NoSuccessException {
        initFactory();
        return factory.doCreateBuffer(size);
    }

    /**
     * Creates a (implementation-managed) buffer.
     * 
     * @return
     *      the buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the defaults are not suitable.
     */
    public static Buffer createBuffer() throws BadParameterException,
            NoSuccessException {
        initFactory();
        return factory.doCreateBuffer(-1);
    }
}
