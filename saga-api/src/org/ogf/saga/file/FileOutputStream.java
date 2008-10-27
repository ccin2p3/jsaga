package org.ogf.saga.file;

import java.io.OutputStream;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * Since Java programmers are used to streams, the Java language bindings of
 * SAGA provide them. In contrast to everything else in the language bindings,
 * this is an abstract class, not an interface, because it is supposed to be a
 * java.io.OutputStream (which is a class, not an interface). Implementations
 * should redefine methods of java.io.OutputStream.
 */
public abstract class FileOutputStream extends OutputStream implements
        SagaObject, Async {

    /**
     * Clone is mentioned here because the inherited
     * {@link java.lang.Object#clone()} cannot hide the public version in
     * {@link SagaObject#clone()}.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // Task versions of java.io.OutputStream methods.

    /**
     * Creates a task that writes a byte to this stream.
     * 
     * @param mode
     *            the task mode. See {@link java.io.OutputStream#write(int)}.
     */
    public abstract Task<FileOutputStream, Void> write(TaskMode mode, int b)
            throws NotImplementedException;

    /**
     * Creates a task that writes (part of) a buffer to this stream.
     * 
     * @param mode
     *            the task mode. See
     *            {@link java.io.OutputStream#write(byte[], int, int)}.
     */
    public abstract Task<FileOutputStream, Void> write(TaskMode mode,
            byte[] buf, int off, int len) throws NotImplementedException;

    /**
     * Creates a task that writes a buffer to this stream.
     * 
     * @param mode
     *            the task mode. See {@link java.io.OutputStream#write(byte[])}.
     */
    public Task<FileOutputStream, Void> write(TaskMode mode, byte[] buf)
            throws NotImplementedException {
        return write(mode, buf, 0, buf.length);
    }

    /**
     * Creates a task that flushes this stream.
     * 
     * @param mode
     *            the task mode. See {@link java.io.OutputStream#flush()}.
     */
    public abstract Task<FileOutputStream, Void> flush(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that closes this stream.
     * 
     * @param mode
     *            the task mode. See {@link java.io.OutputStream#close()}.
     */
    public abstract Task<FileOutputStream, Void> close(TaskMode mode)
            throws NotImplementedException;
}
