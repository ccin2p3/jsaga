package org.ogf.saga.buffer;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

/**
 * Factory for creating buffers.
 */
public abstract class BufferFactory {
    
    private static BufferFactory factory;
    
    private synchronized static void initFactory() throws NotImplemented {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createBufferFactory();
        }
    }

    /**
     * Creates a buffer. To be provided by an implementation.
     * @param data the storage.
     * @return the buffer.
     */
    protected abstract Buffer doCreateBuffer(byte[] data)
        throws NotImplemented, BadParameter, NoSuccess;
    
    /**
     * Creates a buffer. To be provided by an implementation.
     * @return the buffer.
     */
    protected abstract Buffer doCreateBuffer()
        throws NotImplemented, BadParameter, NoSuccess;
    
    /**
     * Creates a buffer. To be provided by an implementation.
     * @param size the size of the buffer.
     * @return the buffer.
     */
    protected abstract Buffer doCreateBuffer(int size)
        throws NotImplemented, BadParameter, NoSuccess;
    
    /**
     * Creates a (application-allocated) buffer. The size is implicit in
     * the size of the specified array.
     * @param data the storage.
     * @return the buffer.
     * @throws NoSuccess 
     */
    public static Buffer createBuffer(byte[] data)
        throws NotImplemented, BadParameter, NoSuccess {
        initFactory();
        return factory.doCreateBuffer(data);
    }

    /**
     * Creates a (implementation-managed and implementation-allocated) buffer
     * of the specified size.
     * @param size the size.
     * @return the buffer.
     */
    public static Buffer createBuffer(int size)
        throws NotImplemented, BadParameter, NoSuccess {
        initFactory();
        return factory.doCreateBuffer(size);
    }

    /**
     * Creates a (implementation-managed) buffer.
     * @return the buffer.
     * @throws NoSuccess 
     */
    public static Buffer createBuffer()
        throws NotImplemented, BadParameter, NoSuccess {
        initFactory();
        return factory.doCreateBuffer();
    }
}
