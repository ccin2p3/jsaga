package fr.in2p3.jsaga.sync.file;

import fr.in2p3.jsaga.sync.namespace.SyncNSDirectory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileInputStream;
import org.ogf.saga.file.FileOutputStream;
import org.ogf.saga.url.URL;

/**
 * A Directory instance represents an open directory.
 */
public interface SyncDirectory extends SyncNSDirectory {

    // Inspection methods

    /**
     * Returns the number of bytes in the specified file.
     *
     * @param name
     *            name of file to inspect.
     * @param flags
     *            mode for operation.
     * @return the size.
     */
    public long getSizeSync(URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Returns the number of bytes in the specified file.
     *
     * @param name
     *            name of file to inspect.
     * @return the size.
     */
    public long getSizeSync(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Tests the name for being a directory entry. Is an alias for
     * {@link SyncNSDirectory#isEntrySync}.
     *
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFileSync(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    // openDirectory and openFile: names changed with respect
    // to specs because of Java restriction: cannot redefine methods with
    // just a different return type.
    // Thus, they don't hide the methods in NamespaceDirectory, but then,
    // the ones in the SAGA spec don't either, because they have different
    // out parameters.

    /**
     * Creates a new <code>FileInputStream</code> instance.
     *
     * @param name
     *            file to open.
     * @return the input stream.
     */
    public FileInputStream openFileInputStreamSync(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>FileOutputStream</code> instance.
     *
     * @param name
     *            file to open.
     * @return the output stream.
     */
    public FileOutputStream openFileOutputStreamSync(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>FileOutputStream</code> instance.
     *
     * @param name
     *            file to open.
     * @param append
     *            when set, the stream appends to the file.
     * @return the output stream.
     */
    public FileOutputStream openFileOutputStreamSync(URL name, boolean append)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;
}
