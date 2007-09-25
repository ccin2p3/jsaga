package org.ogf.saga.logicalfile;

import java.util.List;

import org.ogf.saga.URI;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectSession;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceDirectory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * This interface represents a container for logical files in a logical
 * file name space.
 */
public interface LogicalDirectory extends NamespaceDirectory, Attributes {

    /**
     * Tests the name for being a logical file.
     * Is an alias for {@link NamespaceDirectory#isEntry}.
     * @param name to be tested.
     * @param flags flags for the operation.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFile(URI name, Flags... flags)
        throws NotImplemented, IncorrectURL, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, BadParameter,
            IncorrectState, Timeout, NoSuccess;

    /**
     * Finds entries in the current directory and below, with matching names
     * and matching meta data.
     * @param namePattern pattern for names of entries to be found.
     * @param keyPattern  pattern for meta data keys of entries to be found.
     * @param valPattern  pattern for metat data values of entries to be found.
     * @param flags       flags defining the operation modus.
     * @return the list of matching entries.
     */
    public List<String> find(String namePattern, String[] keyPattern,
            String[] valPattern, Flags... flags)
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
    public LogicalDirectory openLogicalDir(URI name, Flags... flags)
        throws NotImplemented, IncorrectURL, IncorrectSession,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    /**
     * Creates a new <code>LogicalFile</code> instance.
     * @param name logical file to open.
     * @param flags defining the operation modus.
     * @return the opened logical file.
     */
    public LogicalFile openLogicalFile(URI name, Flags... flags)
        throws NotImplemented, IncorrectURL, IncorrectSession,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, IncorrectState, AlreadyExists, DoesNotExist,
            Timeout, NoSuccess;

    // Task versions ...
   
    /**
     * Creates a task that tests the name for being a logical file.
     * Is an alias for {@link NamespaceDirectory#isEntry}.
     * @param mode the task mode.
     * @param name to be tested.
     * @param flags defining operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Boolean> isFile(TaskMode mode, URI name, Flags... flags)
        throws NotImplemented ;

    /**
     * Creates a task that finds entries in the current directory and below,
     * with matching names and matching meta data.
     * @param mode        the task mode.
     * @param namePattern pattern for names of entries to be found.
     * @param keyPattern  pattern for meta data keys of entries to be found.
     * @param valPattern  pattern for metat data values of entries to be found.
     * @param flags       flags defining the operation modus.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<List<String>> find(TaskMode mode, String namePattern,
            String[] keyPattern, String[] valPattern, Flags... flags)
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
    public RVTask<LogicalDirectory> openLogicalDir(TaskMode mode, URI name,
            Flags... flags)
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
    public RVTask<LogicalFile> openLogicalFile(TaskMode mode, URI name,
            Flags... flags)
        throws NotImplemented;
}
