package org.ogf.saga.buffer;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

/**
 * Factory for creating buffers.
 */
public abstract class BufferFactory {

    private static BufferFactory getFactory(String sagaFactoryName)
            throws NoSuccessException {
        return ImplementationBootstrapLoader.getBufferFactory(sagaFactoryName);
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
	return createBuffer(null, data);
    }
    
    /**
     * Creates a (application-allocated) buffer. The size is implicit in the
     * size of the specified array.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
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
    public static Buffer createBuffer(String sagaFactoryClassname, byte[] data)
            throws BadParameterException, NoSuccessException {
	BufferFactory factory = getFactory(sagaFactoryClassname);
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
	return createBuffer(null, size);
    }
    

    /**
     * Creates a (implementation-managed) buffer of the specified size.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
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
    public static Buffer createBuffer(String sagaFactoryClassname, int size) throws BadParameterException, 
            NoSuccessException {
	BufferFactory factory = getFactory(sagaFactoryClassname);
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
	return createBuffer((String)null);
    }
    
    /**
     * Creates a (implementation-managed) buffer.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @return
     *      the buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the defaults are not suitable.
     */
    public static Buffer createBuffer(String sagaFactoryClassname) throws BadParameterException,
            NoSuccessException {
        BufferFactory factory = getFactory(sagaFactoryClassname);
        return factory.doCreateBuffer(-1);
    }
}
