package org.ogf.saga.namespace;

import java.util.List;

import org.ogf.saga.URL;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * Represents a namespace entry that is a directory, and defines additional
 * methods for them.
 */
public interface NSDirectory extends NSEntry {

    /**
     * Changes the working directory.
     * @param dir the directory to change to.
     */
    public void changeDir(URL dir)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
                          AuthorizationFailed, PermissionDenied, BadParameter,
                          IncorrectState, DoesNotExist, Timeout, NoSuccess;

    /**
     * Lists entries in the directory that match the specified pattern.
     * If the pattern is an empty string, all entries are listed.
     * The only allowed flag is DEREFERENCE.
     * @param pattern name or pattern to list.
     * @param flags defining the operation modus.
     * @return the matching entries.
     */
    public List<URL> list(String pattern, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess,
            IncorrectURL;

    /**
     * Lists entries in the directory.
     * The only allowed flag is DEREFERENCE.
     * @param flags defining the operation modus.
     * @return the directory entries.
     */
    public List<URL> list(int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess,
            IncorrectURL;
    
    /**
     * Lists entries in the directory that match the specified pattern.
     * If the pattern is an empty string, all entries are listed.
     * @param pattern name or pattern to list.
     * @return the matching entries.
     */
    public List<URL> list(String pattern)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess,
            IncorrectURL;
 
    /**
     * Lists entries in the directory.
     * @return the directory entries.
     */
    public List<URL> list()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess,
            IncorrectURL;
    
    /**
     * Finds entries in the directory and below that match the specified
     * pattern.
     * If the pattern is an empty string, all entries are listed.
     * @param pattern name or pattern to find.
     * @param flags defining the operation modus.
     * @return the matching entries.
     */
    public List<URL> find(String pattern, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;

    /**
     * Finds entries in the directory and below that match the specified
     * pattern.
     * If the pattern is an empty string, all entries are listed.
     * @param pattern name or pattern to find.
     * @return the matching entries.
     */
    public List<URL> find(String pattern)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;
    
    /**
     * Queries for the existence of an entry.
     * @param name to be tested for existence.
     * @return <code>true</code> if the name exists.
     */
    public boolean exists(URL name)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Tests the name for being a directory.
     * @param name to be tested.
     * @return <code>true</code> if the name represents a directory.
     */
    public boolean isDir(URL name)
        throws NotImplemented, IncorrectURL, DoesNotExist, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Tests the name for being a namespace entry. 
     * @param name to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isEntry(URL name)
        throws NotImplemented, IncorrectURL, DoesNotExist, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Tests the name for being a link.
     * @param name to be tested.
     * @return <code>true</code> if the name represents a link.
     */
    public boolean isLink(URL name)
        throws NotImplemented, IncorrectURL, DoesNotExist, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Returns the URL representing the link target.
     * @param name the name of the link.
     * @return the resolved name.
     */
    public URL readLink(URL name)
        throws NotImplemented, IncorrectURL, DoesNotExist, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    // TODO: replace the next two methods by making NamespaceDirectory extend
    // Iterable<URL>??? What to do then about the async versions?

    /**
     * Obtains the number of entries in this directory.
     * @return the number of entries.
     */
    public int getNumEntries()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Gives the name of an entry in the directory based upon the
     * enumeration defined by getNumEntries().
     * @param entry index of the entry to get.
     * @return the name of the entry.
     * @exception DoesNotExistException is thrown when the index is invalid.
     */
    public URL getEntry(int entry)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Copies the source entry to another part of the namespace.
     * @param source name to copy.
     * @param target name to copy to.
     * @param flags defining the operation modus.
     */
    public void copy(URL source, URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Copies the source entry to another part of the namespace.
     * @param source name to copy.
     * @param target name to copy to.
     */
    public void copy(URL source, URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;  
 
    /**
     * Copies the source entry to another part of the namespace. The source
     * may contain wildcards.
     * @param source name to copy.
     * @param target name to copy to.
     * @param flags defining the operation modus.
     */
    public void copy(String source, URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Copies the source entry to another part of the namespace. The source
     * may contain wildcards. 
     * @param source name to copy.
     * @param target name to copy to.
     */
    public void copy(String source, URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;  
    
    /**
     * Creates a symbolic link from the specified target to the
     * specified source.
     * @param source name to link to.
     * @param target name of the link.
     * @param flags defining the operation modus.
     */
    public void link(URL source, URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Creates a symbolic link from the specified target to the
     * specified source.
     * @param source name to link to.
     * @param target name of the link.
     */
    public void link(URL source, URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Creates a symbolic link from the specified target to the
     * specified source. The source may contain wildcards.
     * @param source name to link to.
     * @param target name of the link.
     * @param flags defining the operation modus.
     */
    public void link(String source, URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Creates a symbolic link from the specified target to the
     * specified source. The source may contain wildcards.
     * @param source name to link to.
     * @param target name of the link.
     */
    public void link(String source, URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     * @param source name to move.
     * @param target name to move to.
     * @param flags defining the operation modus.
     */
    public void move(URL source, URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     * @param source name to move.
     * @param target name to move to.
     */
    public void move(URL source, URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     * The source may contain wildcards.
     * @param source name to move.
     * @param target name to move to.
     * @param flags defining the operation modus.
     */
    public void move(String source, URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Renames the specified source to the specified target, or move the
     * specified source to the specified target if the target is a directory.
     * The source may contain wildcards.
     * @param source name to move.
     * @param target name to move to.
     */
    public void move(String source, URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Removes the specified entry.
     * @param target name to remove.
     * @param flags defining the operation modus.
     */
    public void remove(URL target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            DoesNotExist, Timeout, NoSuccess;

    /**
     * Removes the specified entry.
     * @param target name to remove.
     */
    public void remove(URL target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Removes the specified entry.
     * The target string may contain wildcards.
     * @param target name to remove.
     * @param flags defining the operation modus.
     */
    public void remove(String target, int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            DoesNotExist, Timeout, NoSuccess;

    /**
     * Removes the specified entry.
     * The target string may contain wildcards.
     * @param target name to remove.
     */
    public void remove(String target)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectURL, BadParameter, IncorrectState,
            DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Creates a new directory.
     * @param target directory to create.
     * @param flags defining the operation modus.
     */
    public void makeDir(URL target, int flags)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Creates a new directory.
     * @param target directory to create.
     */
    public void makeDir(URL target)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    /**
     * Creates a new <code>NamespaceDirectory</code> instance.
     * @param name directory to open.
     * @param flags defining the operation modus.
     * @return the opened directory instance.
     */
    public NSDirectory openDir(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>NamespaceDirectory</code> instance.
     * @param name directory to open.
     * @return the opened directory instance.
     */
    public NSDirectory openDir(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
    
    /**
     * Creates a new <code>NamespaceEntry</code> instance.
     * @param name entry to open.
     * @param flags defining the operation modus.
     * @return the opened entry instance.
     */
    public NSEntry open(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>NamespaceEntry</code> instance.
     * @param name entry to open.
     * @return the opened entry instance.
     */
    public NSEntry open(URL name)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;
    
    /**
     * Allows the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllow(URL target, String id, int permissions,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess;

    /**
     * Allows the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     */
    public void permissionsAllow(URL target, String id, int permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess;
    
    /**
     * Allows the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsAllow(String target, String id, int permissions,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess;

    /**
     * Allows the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     */
    public void permissionsAllow(String target, String id, int permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess;
    
    /**
     * Denies the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDeny(URL target, String id, int permissions,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;
    
    /**
     * Denies the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     */
    public void permissionsDeny(URL target, String id, int permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;
    
    /**
     * Denies the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     */
    public void permissionsDeny(String target, String id, int permissions,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;
    
    /**
     * Denies the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     */
    public void permissionsDeny(String target, String id, int permissions)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, Timeout, NoSuccess;
    
    //
    // Task versions ...
    //

    /**
     * Creates a task that changes the working directory.
     * @param mode the task mode.
     * @param dir the directory to change to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task changeDir(TaskMode mode, URL dir) throws NotImplemented;

    /**
     * Creates a task that lists entries in the directory that match
     * the specified pattern.
     * If the pattern is an empty string, all entries are listed.
     * The only allowed flag is DEREFERENCE.
     * @param mode the task mode.
     * @param pattern name or pattern to list.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<URL>> list(TaskMode mode, String pattern, int flags)
        throws NotImplemented;

    /**
     * Creates a task that lists entries in the directory that match
     * the specified pattern.
     * If the pattern is an empty string, all entries are listed.
     * The only allowed flag is DEREFERENCE.
     * @param mode the task mode.
     * @param pattern name or pattern to list.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<URL>> list(TaskMode mode, String pattern)
        throws NotImplemented;
 
    /**
     * Creates a task that lists entries in the directory.
     * The only allowed flag is DEREFERENCE.
     * @param mode the task mode.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<URL>> list(TaskMode mode, int flags)
        throws NotImplemented;

    /**
     * Creates a task that lists entries in the directory.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<URL>> list(TaskMode mode) throws NotImplemented;
    
    /**
     * Creates a task that finds entries in the directory and below that
     * match the specified pattern.
     * If the pattern is an empty string, all entries are listed.
     * @param mode the task mode.
     * @return the task.
     * @param pattern name or pattern to find.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<URL>> find(TaskMode mode, String pattern, int flags)
        throws NotImplemented;

    /**
     * Creates a task that finds entries in the directory and below that
     * match the specified pattern.
     * If the pattern is an empty string, all entries are listed.
     * @param mode the task mode.
     * @return the task.
     * @param pattern name or pattern to find.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<URL>> find(TaskMode mode, String pattern)
        throws NotImplemented;
    
    /**
     * Creates a task that queries for the existence of an entry.
     * @param mode the task mode.
     * @param name to be tested for existence.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> exists(TaskMode mode, URL name)
        throws NotImplemented;

    /**
     * Creates a task that tests the name for being a directory.
     * @param mode the task mode.
     * @param name to be tested.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isDir(TaskMode mode, URL name)
        throws NotImplemented;

    /**
     * Creates a task that tests the name for being a namespace entry. 
     * @param mode the task mode.
     * @param name to be tested.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isEntry(TaskMode mode, URL name)
        throws NotImplemented;

    /**
     * Creates a task that tests the name for being a link.
     * @param mode the task mode.
     * @param name to be tested.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isLink(TaskMode mode, URL name)
        throws NotImplemented;

    /**
     * Creates a task that returns the URL representing the link target.
     * @param mode the task mode.
     * @param name the name of the link.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<URL> readLink(TaskMode mode, URL name)
        throws NotImplemented;

    /**
     * Creates a task that obtains the number of entries in this directory.
     * @param mode the task mode.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Integer> getNumEntries(TaskMode mode)
        throws NotImplemented;

    /**
     * Creates a task that gives the name of an entry in the directory based
     * upon the enumeration defined by getNumEntries().
     * @param mode the task mode.
     * @param entry index of the entry to get.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<URL> getEntry(TaskMode mode, int entry)
        throws NotImplemented;

    /**
     * Creates a task that copies source the entry to another part of
     * the namespace.
     * @param mode the task mode.
     * @param source name to copy.
     * @param target name to copy to.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task copy(TaskMode mode, URL source, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that copies source the entry to another part of
     * the namespace.
     * @param mode the task mode.
     * @param source name to copy.
     * @param target name to copy to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task copy(TaskMode mode, URL source, URL target)
        throws NotImplemented;

    /**
     * Creates a task that copies the source entry to another part of
     * the namespace. The source may contain wildcards.
     * @param mode the task mode.
     * @param source name to copy.
     * @param target name to copy to.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task copy(TaskMode mode, String source, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that copies the source entry to another part of
     * the namespace. The source may contain wildcards.
     * @param mode the task mode.
     * @param source name to copy.
     * @param target name to copy to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task copy(TaskMode mode, String source, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a symbolic link from the specified
     * target to the specified source.
     * @param mode the task mode.
     * @param source name to link to.
     * @param target name of the link.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task link(TaskMode mode, URL source, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a symbolic link from the specified
     * target to the specified source.
     * @param mode the task mode.
     * @param source name to link to.
     * @param target name of the link.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task link(TaskMode mode, URL source, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a symbolic link from the specified
     * target to the specified source. The source may contain wildcards.
     * @param mode the task mode.
     * @param source name to link to.
     * @param target name of the link.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task link(TaskMode mode, String source, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a symbolic link from the specified
     * target to the specified source. The source may contain wildcards.
     * @param mode the task mode.
     * @param source name to link to.
     * @param target name of the link.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task link(TaskMode mode, String source, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory.
     * @param mode the task mode.
     * @param source name to move.
     * @param target name to move to.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task move(TaskMode mode, URL source, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory.
     * @param mode the task mode.
     * @param source name to move.
     * @param target name to move to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task move(TaskMode mode, URL source, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory. The source may contain wildcards.
     * @param mode the task mode.
     * @param source name to move.
     * @param target name to move to.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task move(TaskMode mode, String source, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that renames the specified source to the specified target,
     * or move the specified source to the specified target if the target is a
     * directory. The source may contain wildcards.
     * @param mode the task mode.
     * @param source name to move.
     * @param target name to move to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task move(TaskMode mode, String source, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that removes the specified entry.
     * @param mode the task mode.
     * @param target name to remove.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task remove(TaskMode mode, URL target, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that removes the specified entry.
     * @param mode the task mode.
     * @param target name to remove.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task remove(TaskMode mode, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that removes the specified entry.
     * The target may contain wildcards.
     * @param mode the task mode.
     * @param target name to remove.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task remove(TaskMode mode, String target, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that removes the specified entry.
     * The target may contain wildcards.
     * @param mode the task mode.
     * @param target name to remove.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task remove(TaskMode mode, String target)
        throws NotImplemented;

    /**
     * Creates a task that creates a new directory.
     * @param mode the task mode.
     * @param target directory to create.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task makeDir(TaskMode mode, URL target, int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a new directory.
     * @param mode the task mode.
     * @param target directory to create.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task makeDir(TaskMode mode, URL target)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a new <code>NamespaceDirectory</code>
     * instance.
     * @param mode the task mode.
     * @param name directory to open.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<NSDirectory> openDir(TaskMode mode, URL name,
            int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a new <code>NamespaceDirectory</code>
     * instance.
     * @param mode the task mode.
     * @param name directory to open.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<NSDirectory> openDir(TaskMode mode, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that creates a new <code>NamespaceEntry</code> instance.
     * @param mode the task mode.
     * @param name entry to open.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<NSEntry> open(TaskMode mode, URL name, int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a new <code>NamespaceEntry</code> instance.
     * @param mode the task mode.
     * @param name entry to open.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<NSEntry> open(TaskMode mode, URL name)
        throws NotImplemented;
    
    /**
     * Creates a task that enables the specified permissions for the
     * specified id.
     * An id of "*" enables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsAllow(TaskMode mode, URL target, String id,
            int permissions, int flags)
        throws NotImplemented;

    /**
     * Creates a task that enables the specified permissions for the
     * specified id.
     * An id of "*" enables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsAllow(TaskMode mode, URL target, String id,
            int permissions)
        throws NotImplemented;
    
    /**
     * Creates a task that enables the specified permissions for the
     * specified id. The target may contain wildcards.
     * An id of "*" enables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsAllow(TaskMode mode, String target, String id,
            int permissions, int flags)
        throws NotImplemented;

    /**
     * Creates a task that enables the specified permissions for the
     * specified id. The target may contain wildcards.
     * An id of "*" enables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to enable.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsAllow(TaskMode mode, String target, String id,
            int permissions)
        throws NotImplemented;
    
    /**
     * Creates a task that disables the specified permissions for the
     * specified id.
     * An id of "*" disables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsDeny(TaskMode mode, URL target, String id,
            int permissions, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that disables the specified permissions for the
     * specified id.
     * An id of "*" disables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsDeny(TaskMode mode, URL target, String id,
            int permissions)
        throws NotImplemented;
    
    /**
     * Creates a task that disables the specified permissions for the
     * specified id. The target may contain wildcards.
     * An id of "*" disables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @param flags the only allowed flags are RECURSIVE and DEREFERENCE.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsDeny(TaskMode mode, String target, String id,
            int permissions, int flags)
        throws NotImplemented;
    
    /**
     * Creates a task that disables the specified permissions for the
     * specified id. The target may contain wildcards.
     * An id of "*" disables the permissions for all.
     * @param mode determines the initial state of the task.
     * @param target the entry affected.
     * @param id the id.
     * @param permissions the permissions to disable.
     * @return the task object.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task permissionsDeny(TaskMode mode, String target, String id,
            int permissions)
        throws NotImplemented;
}
