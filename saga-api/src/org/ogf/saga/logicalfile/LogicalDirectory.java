package org.ogf.saga.logicalfile;

import java.util.List;

import org.ogf.saga.URL;
import org.ogf.saga.attributes.AsyncAttributes;
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
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * This interface represents a container for logical files in a logical
 * file name space.
 */
public interface LogicalDirectory extends NSDirectory, AsyncAttributes {

    /**
     * Tests the name for being a logical file.
     * Is an alias for {@link NSDirectory#isEntry}.
     * @param name to be tested.
     * @param flags flags for the operation.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFile(URL name, int flags)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Finds entries in the current directory and below, with matching names
     * and matching meta data.
     * @param namePattern pattern for names of entries to be found.
     * @param attrPattern  pattern for meta data keys/values of entries to be
     *          found.
     * @param flags       flags defining the operation modus.
     * @return the list of matching entries.
     */
    public List<String> find(String namePattern, String[] attrPattern,
            int flags)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout,
            NoSuccess;

    // openLogicalDir and openLogicalFile: names changed with respect
    // to specs  because of Java restriction: cannot redefine methods with
    // just a different return type.

    /**
     * Creates a new <code>LogicalDirectory</code> instance.
     * @param name directory to open.
     * @param flags defining the operation modus.
     * @return the opened directory instance.
     */
    public LogicalDirectory openLogicalDir(URL name, int flags)
        throws NotImplemented, IncorrectURL, 
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>LogicalFile</code> instance.
     * @param name logical file to open.
     * @param flags defining the operation modus.
     * @return the opened logical file.
     */
    public LogicalFile openLogicalFile(URL name, int flags)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    // Task versions ...
   
    /**
     * Creates a task that tests the name for being a logical file.
     * Is an alias for {@link NSDirectory#isEntry}.
     * @param mode the task mode.
     * @param name to be tested.
     * @param flags defining operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isFile(TaskMode mode, URL name, int flags)
        throws NotImplemented ;

    /**
     * Creates a task that finds entries in the current directory and below,
     * with matching names and matching meta data.
     * @param mode        the task mode.
     * @param namePattern pattern for names of entries to be found.
     * @param attrPattern pattern for meta data keys/values of entries to be
     *          found.
     * @param flags       flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<List<String>> find(TaskMode mode, String namePattern,
            String[] attrPattern, int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a new <code>LogicalDirectory</code>
     * instance.
     * @param mode the task mode.
     * @param name directory to open.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<LogicalDirectory> openLogicalDir(TaskMode mode, URL name,
            int flags)
        throws NotImplemented;

    /**
     * Creates a task that creates a new <code>LogicalFile</code> instance.
     * @param mode the task mode.
     * @param name the gile to open.
     * @param flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<LogicalFile> openLogicalFile(TaskMode mode, URL name,
            int flags)
        throws NotImplemented;
}
