package fr.in2p3.jsaga.sync.namespace;

import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.List;

/**
 * Represents a namespace entry that is a directory, and defines additional
 * methods for them. This interface extends <code>Iterable</code>, which
 * allows an application to iterate over the entries in this directory.
 * Implementations can use {@link #getNumEntriesSync()} and {@link #getEntrySync(int)}
 * to implement an iterator, but will have to encapsulate the exceptions that
 * these methods can throw in either a {@link java.lang.RuntimeException} or a
 * {@link java.lang.Error}.
 */
public interface SyncNSDirectory extends SyncNSEntry, Iterable<URL> {

    /**
     * Changes the working directory.
     *
     * @param dir
     *            the directory to change to.
     */
    public void changeDirSync(URL dir) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Lists entries in the directory that match the specified pattern. If the
     * pattern is an empty string, all entries are listed. The only allowed flag
     * is DEREFERENCE.
     *
     * @param pattern
     *            name or pattern to list.
     * @param flags
     *            defining the operation modus.
     * @return the matching entries.
     */
    public List<URL> listSync(String pattern, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException, IncorrectURLException;

    /**
     * Lists entries in the directory. The only allowed flag is DEREFERENCE.
     *
     * @param flags
     *            defining the operation modus.
     * @return the directory entries.
     */
    public List<URL> listSync(int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Lists entries in the directory that match the specified pattern. If the
     * pattern is an empty string, all entries are listed.
     *
     * @param pattern
     *            name or pattern to list.
     * @return the matching entries.
     */
    public List<URL> listSync(String pattern) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Lists entries in the directory.
     *
     * @return the directory entries.
     */
    public List<URL> listSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Finds entries in the directory and below that match the specified
     * pattern. If the pattern is an empty string, all entries are listed.
     *
     * @param pattern
     *            name or pattern to find.
     * @param flags
     *            defining the operation modus.
     * @return the matching entries.
     */
    public List<URL> findSync(String pattern, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    /**
     * Finds entries in the directory and below that match the specified
     * pattern. If the pattern is an empty string, all entries are listed.
     *
     * @param pattern
     *            name or pattern to find.
     * @return the matching entries.
     */
    public List<URL> findSync(String pattern) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Queries for the existence of an entry.
     *
     * @param name
     *            to be tested for existence.
     * @return <code>true</code> if the name exists.
     */
    public boolean existsSync(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    /**
     * Returns the time of the last modification in seconds since epoch
     * (01.01.1970) of the specified name.
     *
     * @param name
     *      the name of which the last modification time must be returned.
     * @return
     *      the last modification time.
     */
    public long getMTimeSync(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    /**
     * Tests the name for being a directory.
     *
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a directory.
     */
    public boolean isDirSync(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Tests the name for being a namespace entry.
     *
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isEntrySync(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Tests the name for being a link.
     *
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a link.
     */
    public boolean isLinkSync(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Returns the URL representing the link target.
     *
     * @param name
     *            the name of the link.
     * @return the resolved name.
     */
    public URL readLinkSync(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Obtains the number of entries in this directory.
     *
     * @return the number of entries.
     */
    public int getNumEntriesSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Gives the name of an entry in the directory based upon the enumeration
     * defined by {@link #getNumEntriesSync()}.
     *
     * @param entry
     *            index of the entry to get.
     * @return the name of the entry.
     * @exception DoesNotExistException
     *                is thrown when the index is invalid.
     */
    public URL getEntrySync(int entry) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Copies the source entry to another part of the namespace.
     *
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     * @param flags
     *            defining the operation modus.
     */
    public void copySync(URL source, URL target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Copies the source entry to another part of the namespace.
     *
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     */
    public void copySync(URL source, URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Copies the source entry to another part of the namespace. The source may
     * contain wildcards.
     *
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     * @param flags
     *            defining the operation modus.
     */
    public void copySync(String source, URL target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Copies the source entry to another part of the namespace. The source may
     * contain wildcards.
     *
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     */
    public void copySync(String source, URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a symbolic link from the specified target to the specified
     * source.
     *
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     * @param flags
     *            defining the operation modus.
     */
    public void linkSync(URL source, URL target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a symbolic link from the specified target to the specified
     * source.
     *
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     */
    public void linkSync(URL source, URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a symbolic link from the specified target to the specified
     * source. The source may contain wildcards.
     *
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     * @param flags
     *            defining the operation modus.
     */
    public void linkSync(String source, URL target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a symbolic link from the specified target to the specified
     * source. The source may contain wildcards.
     *
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     */
    public void linkSync(String source, URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     *
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     * @param flags
     *            defining the operation modus.
     */
    public void moveSync(URL source, URL target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     *
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     */
    public void moveSync(URL source, URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     * The source may contain wildcards.
     *
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     * @param flags
     *            defining the operation modus.
     */
    public void moveSync(String source, URL target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     * The source may contain wildcards.
     *
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     */
    public void moveSync(String source, URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Removes the specified entry.
     *
     * @param target
     *            name to remove.
     * @param flags
     *            defining the operation modus.
     */
    public void removeSync(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Removes the specified entry.
     *
     * @param target
     *            name to remove.
     */
    public void removeSync(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Removes the specified entry. The target string may contain wildcards.
     *
     * @param target
     *            name to remove.
     * @param flags
     *            defining the operation modus.
     */
    public void removeSync(String target, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectURLException, BadParameterException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Removes the specified entry. The target string may contain wildcards.
     *
     * @param target
     *            name to remove.
     */
    public void removeSync(String target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectURLException,
            BadParameterException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new directory.
     *
     * @param target
     *            directory to create.
     * @param flags
     *            defining the operation modus.
     */
    public void makeDirSync(URL target, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new directory.
     *
     * @param target
     *            directory to create.
     */
    public void makeDirSync(URL target) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllowSync(URL target, String id, int permissions,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     */
    public void permissionsAllowSync(URL target, String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, IncorrectURLException,
            IncorrectStateException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllowSync(String target, String id, int permissions,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     */
    public void permissionsAllowSync(String target, String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, IncorrectURLException,
            IncorrectStateException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDenySync(URL target, String id, int permissions,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     */
    public void permissionsDenySync(URL target, String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, IncorrectURLException,
            BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDenySync(String target, String id, int permissions,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     *
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     */
    public void permissionsDenySync(String target, String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, IncorrectURLException,
            BadParameterException, TimeoutException, NoSuccessException;
}
