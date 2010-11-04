package fr.in2p3.jsaga.sync.namespace;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.url.URL;

/**
 * Defines methods that allow inspection and management of the entry.
 */
public interface SyncNSEntry extends SagaObject, Permissions<NSEntry> {

    /**
     * Obtains the complete URL refering to the entry.
     *
     * @return the URL.
     */
    public URL getURLSync() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Obtains the current working directory for the entry.
     *
     * @return the current working directory.
     */
    public URL getCWDSync() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Obtains the name part of the URL of this entry.
     *
     * @return the name part.
     */
    public URL getNameSync() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Tests this entry for being a directory.
     *
     * @return true if the entry is a directory.
     */
    public boolean isDirSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Tests this entry for being a namespace entry. If this entry represents a
     * link or a directory, this method returns <code>false</code>, although
     * strictly speaking, directories and links are namespace entries as well.
     *
     * @return true if the entry is a namespace entry.
     */
    public boolean isEntrySync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Tests this entry for being a link.
     *
     * @return true if the entry is a link.
     */
    public boolean isLinkSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Returns the URL representing the link target. Resolves one link level
     * only.
     *
     * @return the link target.
     */
    public URL readLinkSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Copies this entry to another part of the namespace.
     *
     * @param target
     *            the name to copy to.
     * @param flags
     *            defining the operation modus.
     */
    public void copySync(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Copies this entry to another part of the namespace.
     *
     * @param target
     *            the name to copy to.
     */
    public void copySync(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Creates a symbolic link from the target to this entry.
     *
     * @param target
     *            the name that will have the symbolic link to this entry.
     * @param flags
     *            defining the operation modus.
     */
    public void linkSync(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException, TimeoutException,
            NoSuccessException, IncorrectURLException;

    /**
     * Creates a symbolic link from the target to this entry.
     *
     * @param target
     *            the name that will have the symbolic link to this entry.
     */
    public void linkSync(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException, TimeoutException,
            NoSuccessException, IncorrectURLException;

    /**
     * Renames this entry to the target, or moves this entry to the target if it
     * is a directory.
     *
     * @param target
     *            the name to move to.
     * @param flags
     *            defining the operation modus.
     */
    public void moveSync(URL target, int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Renames this entry to the target, or moves this entry to the target if it
     * is a directory.
     *
     * @param target
     *            the name to move to.
     */
    public void moveSync(URL target) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Removes this entry and closes it.
     *
     * @param flags
     *            defining the operation modus.
     */
    public void removeSync(int flags) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Removes this entry and closes it.
     */
    public void removeSync() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Allows the specified permissions for the specified id. An id of "*"
     * enables the permissions for all.
     *
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllowSync(String id, int permissions, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, BadParameterException, TimeoutException,
            NoSuccessException;

    /**
     * Denies the specified permissions for the specified id. An id of "*"
     * disables the permissions for all.
     *
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDenySync(String id, int permissions, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, IncorrectStateException,
            PermissionDeniedException, BadParameterException, TimeoutException,
            NoSuccessException;
}
