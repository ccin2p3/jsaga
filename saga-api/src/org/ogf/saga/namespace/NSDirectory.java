package org.ogf.saga.namespace;

import java.util.List;

import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/**
 * Represents a namespace entry that is a directory, and defines additional
 * methods for them. This interface extends <code>Iterable</code>, which
 * allows an application to iterate over the entries in this directory.
 * Implementations can use {@link #getNumEntries()} and {@link #getEntry(int)}
 * to implement an iterator, but will have to encapsulate the exceptions that
 * these methods can throw in either a {@link java.lang.RuntimeException} or a
 * {@link java.lang.Error}.
 */
public interface NSDirectory extends NSEntry, Iterable<URL> {

    /**
     * Changes the working directory.
     * 
     * @param dir
     *            the directory to change to.
     */
    public void changeDir(URL dir) throws NotImplementedException,
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
    public List<URL> list(String pattern, int flags)
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
    public List<URL> list(int flags) throws NotImplementedException,
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
    public List<URL> list(String pattern) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException,
            IncorrectURLException;

    /**
     * Lists entries in the directory.
     * 
     * @return the directory entries.
     */
    public List<URL> list() throws NotImplementedException,
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
    public List<URL> find(String pattern, int flags)
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
    public List<URL> find(String pattern) throws NotImplementedException,
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
    public boolean exists(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
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
    public boolean isDir(URL name) throws NotImplementedException,
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
    public boolean isEntry(URL name) throws NotImplementedException,
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
    public boolean isLink(URL name) throws NotImplementedException,
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
    public URL readLink(URL name) throws NotImplementedException,
            IncorrectURLException, DoesNotExistException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Obtains the number of entries in this directory.
     * 
     * @return the number of entries.
     */
    public int getNumEntries() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Gives the name of an entry in the directory based upon the enumeration
     * defined by {@link #getNumEntries()}.
     * 
     * @param entry
     *            index of the entry to get.
     * @return the name of the entry.
     * @exception DoesNotExistException
     *                is thrown when the index is invalid.
     */
    public URL getEntry(int entry) throws NotImplementedException,
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
    public void copy(URL source, URL target, int flags)
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
    public void copy(URL source, URL target) throws NotImplementedException,
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
    public void copy(String source, URL target, int flags)
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
    public void copy(String source, URL target) throws NotImplementedException,
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
    public void link(URL source, URL target, int flags)
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
    public void link(URL source, URL target) throws NotImplementedException,
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
    public void link(String source, URL target, int flags)
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
    public void link(String source, URL target) throws NotImplementedException,
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
    public void move(URL source, URL target, int flags)
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
    public void move(URL source, URL target) throws NotImplementedException,
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
    public void move(String source, URL target, int flags)
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
    public void move(String source, URL target) throws NotImplementedException,
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
    public void remove(URL target, int flags) throws NotImplementedException,
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
    public void remove(URL target) throws NotImplementedException,
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
    public void remove(String target, int flags)
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
    public void remove(String target) throws NotImplementedException,
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
    public void makeDir(URL target, int flags) throws NotImplementedException,
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
    public void makeDir(URL target) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new <code>NamespaceDirectory</code> instance.
     * 
     * @param name
     *            directory to open.
     * @param flags
     *            defining the operation modus.
     * @return the opened directory instance.
     */
    public NSDirectory openDir(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            IncorrectStateException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a new <code>NamespaceDirectory</code> instance.
     * 
     * @param name
     *            directory to open.
     * @return the opened directory instance.
     */
    public NSDirectory openDir(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new <code>NamespaceEntry</code> instance.
     * 
     * @param name
     *            entry to open.
     * @param flags
     *            defining the operation modus.
     * @return the opened entry instance.
     */
    public NSEntry open(URL name, int flags) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a new <code>NamespaceEntry</code> instance.
     * 
     * @param name
     *            entry to open.
     * @return the opened entry instance.
     */
    public NSEntry open(URL name) throws NotImplementedException,
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
    public void permissionsAllow(URL target, String id, int permissions,
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
    public void permissionsAllow(URL target, String id, int permissions)
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
    public void permissionsAllow(String target, String id, int permissions,
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
    public void permissionsAllow(String target, String id, int permissions)
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
    public void permissionsDeny(URL target, String id, int permissions,
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
    public void permissionsDeny(URL target, String id, int permissions)
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
    public void permissionsDeny(String target, String id, int permissions,
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
    public void permissionsDeny(String target, String id, int permissions)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, IncorrectURLException,
            BadParameterException, TimeoutException, NoSuccessException;

    //
    // Task versions ...
    //

    /**
     * Creates a task that changes the working directory.
     * 
     * @param mode
     *            the task mode.
     * @param dir
     *            the directory to change to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> changeDir(TaskMode mode, URL dir)
            throws NotImplementedException;

    /**
     * Creates a task that lists entries in the directory that match the
     * specified pattern. If the pattern is an empty string, all entries are
     * listed. The only allowed flag is DEREFERENCE.
     * 
     * @param mode
     *            the task mode.
     * @param pattern
     *            name or pattern to list.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, List<URL>> list(TaskMode mode, String pattern,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that lists entries in the directory that match the
     * specified pattern. If the pattern is an empty string, all entries are
     * listed. The only allowed flag is DEREFERENCE.
     * 
     * @param mode
     *            the task mode.
     * @param pattern
     *            name or pattern to list.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, List<URL>> list(TaskMode mode, String pattern)
            throws NotImplementedException;

    /**
     * Creates a task that lists entries in the directory. The only allowed flag
     * is DEREFERENCE.
     * 
     * @param mode
     *            the task mode.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, List<URL>> list(TaskMode mode, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that lists entries in the directory.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, List<URL>> list(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that finds entries in the directory and below that match
     * the specified pattern. If the pattern is an empty string, all entries are
     * listed.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @param pattern
     *            name or pattern to find.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, List<URL>> find(TaskMode mode, String pattern,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that finds entries in the directory and below that match
     * the specified pattern. If the pattern is an empty string, all entries are
     * listed.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @param pattern
     *            name or pattern to find.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, List<URL>> find(TaskMode mode, String pattern)
            throws NotImplementedException;

    /**
     * Creates a task that queries for the existence of an entry.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            to be tested for existence.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Boolean> exists(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that tests the name for being a directory.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            to be tested.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Boolean> isDir(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that tests the name for being a namespace entry.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            to be tested.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Boolean> isEntry(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that tests the name for being a link.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            to be tested.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Boolean> isLink(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that returns the URL representing the link target.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            the name of the link.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, URL> readLink(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the number of entries in this directory.
     * 
     * @param mode
     *            the task mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Integer> getNumEntries(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that gives the name of an entry in the directory based
     * upon the enumeration defined by getNumEntries().
     * 
     * @param mode
     *            the task mode.
     * @param entry
     *            index of the entry to get.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, URL> getEntry(TaskMode mode, int entry)
            throws NotImplementedException;

    /**
     * Creates a task that copies source the entry to another part of the
     * namespace.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> copy(TaskMode mode, URL source, URL target,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that copies source the entry to another part of the
     * namespace.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> copy(TaskMode mode, URL source, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that copies the source entry to another part of the
     * namespace. The source may contain wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> copy(TaskMode mode, String source,
            URL target, int flags) throws NotImplementedException;

    /**
     * Creates a task that copies the source entry to another part of the
     * namespace. The source may contain wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to copy.
     * @param target
     *            name to copy to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> copy(TaskMode mode, String source, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that creates a symbolic link from the specified target to
     * the specified source.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> link(TaskMode mode, URL source, URL target,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that creates a symbolic link from the specified target to
     * the specified source.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> link(TaskMode mode, URL source, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that creates a symbolic link from the specified target to
     * the specified source. The source may contain wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> link(TaskMode mode, String source,
            URL target, int flags) throws NotImplementedException;

    /**
     * Creates a task that creates a symbolic link from the specified target to
     * the specified source. The source may contain wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to link to.
     * @param target
     *            name of the link.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> link(TaskMode mode, String source, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> move(TaskMode mode, URL source, URL target,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> move(TaskMode mode, URL source, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory. The source may contain wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> move(TaskMode mode, String source,
            URL target, int flags) throws NotImplementedException;

    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory. The source may contain wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param source
     *            name to move.
     * @param target
     *            name to move to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> move(TaskMode mode, String source, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that removes the specified entry.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            name to remove.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> remove(TaskMode mode, URL target, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that removes the specified entry.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            name to remove.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> remove(TaskMode mode, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that removes the specified entry. The target may contain
     * wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            name to remove.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> remove(TaskMode mode, String target,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that removes the specified entry. The target may contain
     * wildcards.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            name to remove.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> remove(TaskMode mode, String target)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new directory.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            directory to create.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> makeDir(TaskMode mode, URL target, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new directory.
     * 
     * @param mode
     *            the task mode.
     * @param target
     *            directory to create.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> makeDir(TaskMode mode, URL target)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>NamespaceDirectory</code>
     * instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            directory to open.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, NSDirectory> openDir(TaskMode mode, URL name,
            int flags) throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>NamespaceDirectory</code>
     * instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            directory to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, NSDirectory> openDir(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>NamespaceEntry</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            entry to open.
     * @param flags
     *            defining the operation modus.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, NSEntry> open(TaskMode mode, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a new <code>NamespaceEntry</code> instance.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            entry to open.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, NSEntry> open(TaskMode mode, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that enables the specified permissions for the specified
     * id. An id of "*" enables the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, URL target,
            String id, int permissions, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that enables the specified permissions for the specified
     * id. An id of "*" enables the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode, URL target,
            String id, int permissions) throws NotImplementedException;

    /**
     * Creates a task that enables the specified permissions for the specified
     * id. The target may contain wildcards. An id of "*" enables the
     * permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode,
            String target, String id, int permissions, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that enables the specified permissions for the specified
     * id. The target may contain wildcards. An id of "*" enables the
     * permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to enable.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsAllow(TaskMode mode,
            String target, String id, int permissions)
            throws NotImplementedException;

    /**
     * Creates a task that disables the specified permissions for the specified
     * id. An id of "*" disables the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, URL target,
            String id, int permissions, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that disables the specified permissions for the specified
     * id. An id of "*" disables the permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode, URL target,
            String id, int permissions) throws NotImplementedException;

    /**
     * Creates a task that disables the specified permissions for the specified
     * id. The target may contain wildcards. An id of "*" disables the
     * permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @param flags
     *            the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode,
            String target, String id, int permissions, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that disables the specified permissions for the specified
     * id. The target may contain wildcards. An id of "*" disables the
     * permissions for all.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param target
     *            the entry affected.
     * @param id
     *            the id.
     * @param permissions
     *            the permissions to disable.
     * @return the task object.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<NSDirectory, Void> permissionsDeny(TaskMode mode,
            String target, String id, int permissions)
            throws NotImplementedException;
}
