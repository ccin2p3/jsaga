package fr.in2p3.jsaga.sync.file;


import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.IOVec;
import org.ogf.saga.file.SeekMode;

import java.util.List;

/**
 * The File interface represents an open file descriptor for reads/writes on a
 * physical file. Errors result in an SagaIOException, not a POSIX error code.
 */
public interface SyncFile extends SyncNSEntry {

    // Inspection

    /**
     * Returns the number of bytes in the file.
     *
     * @return the size.
     */
    public long getSizeSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    // POSIX-like I/O

    /**
     * Reads up to <code>len</code> bytes from the file into the buffer.
     * Returns the number of bytes read, or 0 at end-of-file. Note: this call is
     * blocking. The async version can be used to implement non-blocking reads.
     *
     * @param buffer
     *            the buffer to read data into.
     * @param len
     *            the number of bytes to be read.
     * @return the number of bytes read.
     */
    public int readSync(Buffer buffer, int len) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Reads up to <code>len</code> bytes from the file into the buffer, at
     * the specified <code>offset</code>. Returns the number of bytes read,
     * or 0 at end-of-file. Note: this call is blocking. The async version can
     * be used to implement non-blocking reads.
     *
     * @param buffer
     *            the buffer to read data into.
     * @param offset
     *            ths offset in the buffer.
     * @param len
     *            the number of bytes to be read.
     * @return the number of bytes read.
     */
    public int readSync(Buffer buffer, int offset, int len)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, SagaIOException;

    /**
     * Reads up to the buffer's size from the file into the buffer. Returns the
     * number of bytes read, or 0 at end-of-file. Note: this call is blocking.
     * The async version can be used to implement non-blocking reads.
     *
     * @param buffer
     *            the buffer to read data into.
     * @return the number of bytes read.
     */
    public int readSync(Buffer buffer) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Writes up to <code>len</code> bytes from the buffer at the specified
     * buffer <code>offset</code> to the file at the current file position.
     * Returns the number of bytes written.
     *
     * @param buffer
     *            the buffer to write data from.
     * @param offset
     *            the buffer offset.
     * @param len
     *            the number of bytes to be written.
     * @return the number of bytes written.
     */
    public int writeSync(Buffer buffer, int offset, int len)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, SagaIOException;

    /**
     * Writes up to <code>len</code> bytes from the buffer to the file at the
     * current file position. Returns the number of bytes written.
     *
     * @param buffer
     *            the buffer to write data from.
     * @param len
     *            the number of bytes to be written.
     * @return the number of bytes written.
     */
    public int writeSync(Buffer buffer, int len) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Writes up to the buffer's size bytes from the buffer to the file at the
     * current file position. Returns the number of bytes written.
     *
     * @param buffer
     *            the buffer to write data from.
     * @return the number of bytes written.
     */
    public int writeSync(Buffer buffer) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Repositions the current file position as requested.
     *
     * @param offset
     *            offset in bytes to move pointer.
     * @param whence
     *            determines from where the offset is relative.
     * @return the position after the seek.
     */
    public long seekSync(long offset, SeekMode whence)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    // Scattered I/O

    /**
     * Gather/scatter read.
     *
     * @param iovecs
     *            array of IOVecs determining how much to read and where to
     *            store it.
     */
    public void readVSync(IOVec[] iovecs) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    /**
     * Gather/scatter write.
     *
     * @param iovecs
     *            array of IOVecs determining how much to write and where to
     *            obtain the data from.
     */
    public void writeVSync(IOVec[] iovecs) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            SagaIOException;

    // Pattern-based I/O

    /**
     * Determines the storage size required for a pattern I/O operation.
     *
     * @param pattern
     *            to determine size for.
     * @return the size.
     */
    public int sizePSync(String pattern) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            IncorrectStateException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Pattern-based read.
     *
     * @param pattern
     *            specification for the read operation.
     * @param buffer
     *            to store data into.
     * @return number of succesfully read bytes.
     */
    public int readPSync(String pattern, Buffer buffer)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, SagaIOException;

    /**
     * Pattern-based write.
     *
     * @param pattern
     *            specification for the write operation.
     * @param buffer
     *            to be written.
     * @return number of succesfully written bytes.
     */
    public int writePSync(String pattern, Buffer buffer)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, SagaIOException;

    // extended I/O

    /**
     * Lists the extended modes available in this implementation and/or on the
     * server side.
     *
     * @return list of available modes.
     */
    public List<String> modesESync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Determines the storage size required for an extended I/O operation.
     *
     * @param emode
     *            extended mode to use.
     * @param spec
     *            to determine size for.
     * @return the size.
     */
    public int sizeESync(String emode, String spec) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            IncorrectStateException, PermissionDeniedException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Extended read.
     *
     * @param emode
     *            extended mode to use.
     * @param spec
     *            specification of read operation.
     * @param buffer
     *            to store the data read.
     * @return the number of successfully read bytes.
     */
    public int readESync(String emode, String spec, Buffer buffer)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, SagaIOException;

    /**
     * Extended write.
     *
     * @param emode
     *            extended mode to use.
     * @param spec
     *            specification of write operation.
     * @param buffer
     *            data to write.
     * @return the number of successfully written bytes.
     */
    public int writeESync(String emode, String spec, Buffer buffer)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, SagaIOException;
}
