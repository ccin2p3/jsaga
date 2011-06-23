package org.ogf.saga.file;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;

/**
 * Factory for objects from the namespace package.
 */
public abstract class FileFactory {
    
    private static FileFactory getFactory(String sagaFactoryName)
            throws NoSuccessException {
        return ImplementationBootstrapLoader.getFileFactory(sagaFactoryName);
    }

    /**
     * Creates an IOVec. To be provided by the implementation.
     * 
     * @param data
     *            data to be used.
     * @param lenIn
     *            number of bytes to read/write on readV/writeV.
     * @return the IOVec.
     */
    protected abstract IOVec doCreateIOVec(byte[] data, int lenIn)
            throws BadParameterException, NoSuccessException;

    /**
     * Creates an IOVec. To be provided by the implementation.
     * 
     * @param size
     *            size of data to be used.
     * @param lenIn
     *            number of bytes to read/write on readV/writeV.
     * @return the IOVec.
     */
    protected abstract IOVec doCreateIOVec(int size, int lenIn)
            throws BadParameterException, NoSuccessException;

    /**
     * Creates a File. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the file instance.
     */
    protected abstract File doCreateFile(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a FileInputStream. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the FileInputStream instance.
     */
    protected abstract FileInputStream doCreateFileInputStream(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a FileOutputStream. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param append
     *            set when the file is opened for appending.
     * @return the FileOutputStream instance.
     */
    protected abstract FileOutputStream doCreateFileOutputStream(
            Session session, URL name, boolean append)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a Directory. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param name
     *            location of directory.
     * @param flags
     *            the open mode.
     * @return the directory instance.
     */
    protected abstract Directory doCreateDirectory(Session session, URL name,
            int flags) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Creates a task that creates a File. To be provided by the implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<FileFactory, File> doCreateFile(TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates a task that creates a FileInputStream. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<FileFactory, FileInputStream> doCreateFileInputStream(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException;

    /**
     * Creates a task that creates a FileOutputStream. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param append
     *            when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<FileFactory, FileOutputStream> doCreateFileOutputStream(
            TaskMode mode, Session session, URL name, boolean append)
            throws NotImplementedException;

    /**
     * Creates a task that creates a Directory. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<FileFactory, Directory> doCreateDirectory(
            TaskMode mode, Session session, URL name, int flags)
            throws NotImplementedException;

    /**
     * Creates an IOVec.
     * 
     * @param data
     *      data to be used.
     * @param lenIn
     *      number of bytes to read/write on readV/writeV.
     * @return
     *      the IOVec.
     * @exception BadParameterException
     *      is thrown when <code>lenIn</code> is larger than the size of the
     *      specified buffer, or < 0, or when the implementation cannot handle
     *      the specified data buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static IOVec createIOVec(byte[] data, int lenIn)
            throws BadParameterException, NoSuccessException {
	return createIOVec(null, data, lenIn);
    }
    
    /**
     * Creates an IOVec.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param data
     *      data to be used.
     * @param lenIn
     *      number of bytes to read/write on readV/writeV.
     * @return
     *      the IOVec.
     * @exception BadParameterException
     *      is thrown when <code>lenIn</code> is larger than the size of the
     *      specified buffer, or < 0, or when the implementation cannot handle
     *      the specified data buffer.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static IOVec createIOVec(String sagaFactoryClassname, byte[] data, int lenIn)
            throws BadParameterException, NoSuccessException {
	return getFactory(sagaFactoryClassname).doCreateIOVec(data, lenIn);
    }

    /**
     * Creates an IOVec.
     * 
     * @param data
     *      data to be used.
     * @return
     *      the IOVec.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle
     *      the specified data buffer.
     */
    public static IOVec createIOVec(byte[] data) throws BadParameterException,
            NoSuccessException {
        return createIOVec(data, data.length);
    }
    

    /**
     * Creates an IOVec.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param data
     *      data to be used.
     * @return
     *      the IOVec.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle
     *      the specified data buffer.
     */
    public static IOVec createIOVec(String sagaFactoryClassname, byte[] data) throws BadParameterException,
            NoSuccessException {
        return createIOVec(sagaFactoryClassname, data, data.length);
    }

    /**
     * Creates an IOVec.
     * 
     * @param size
     *      size of data to be used.
     * @param lenIn
     *      number of bytes to read/write on readV/writeV.
     * @return
     *      the IOVec.
     * @exception BadParameterException
     *      is thrown when <code>lenIn</code> is larger than the size of the
     *      specified buffer, or < 0.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static IOVec createIOVec(int size, int lenIn)
            throws BadParameterException, NoSuccessException {
	return createIOVec(null, size, lenIn);
    }
    
    /**
     * Creates an IOVec.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param size
     *      size of data to be used.
     * @param lenIn
     *      number of bytes to read/write on readV/writeV.
     * @return
     *      the IOVec.
     * @exception BadParameterException
     *      is thrown when <code>lenIn</code> is larger than the size of the
     *      specified buffer, or < 0.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static IOVec createIOVec(String sagaFactoryClassname, int size, int lenIn)
            throws BadParameterException, NoSuccessException {
	return getFactory(sagaFactoryClassname).doCreateIOVec(size, lenIn);
    }

    /**
     * Creates an IOVec.
     * 
     * @param size
     *      size of data to be used.
     * @return
     *      the IOVec.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the specified size.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static IOVec createIOVec(int size) throws BadParameterException,
            NoSuccessException, NotImplementedException {
	return createIOVec(size, size);
    }
    
    /**
     * Creates an IOVec.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param size
     *      size of data to be used.
     * @return
     *      the IOVec.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the specified size.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static IOVec createIOVec(String sagaFactoryClassname, int size) throws BadParameterException,
            NoSuccessException, NotImplementedException {
	return createIOVec(sagaFactoryClassname, size, size);
    }

    /**
     * Creates a File.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	return createFile((String) null, session, name, flags);
    }
    

    /**
     * Creates a File.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(String sagaFactoryClassname, Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateFile(session, name, flags);
    }

    /**
     * Creates a File for reading.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	return createFile(session, name, Flags.READ.getValue());
    }
    

    /**
     * Creates a File for reading.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(String sagaFactoryClassname, Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	return createFile(sagaFactoryClassname, session, name, Flags.READ.getValue());
    }


    /**
     * Creates a File using the default session.
     * 
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFile((Session) null, name, flags);
    }
    

    /**
     * Creates a File using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @param flags
     *      the open mode.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(String sagaFactoryClassname, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFile(sagaFactoryClassname, (Session) null, name, flags);
    }

    /**
     * Creates a File for reading, using the default session.
     * 
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return createFile((Session) null, name);
    }


    /**
     * Creates a File for reading, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @return
     *      the file instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static File createFile(String sagaFactoryClassname, URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return createFile(sagaFactoryClassname, SessionFactory.createSession(sagaFactoryClassname), name);
    }
    
    /**
     * Creates a FileInputStream.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the FileInputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileInputStream createFileInputStream(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileInputStream((String) null, session, name);
    }

    /**
     * Creates a FileInputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the FileInputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileInputStream createFileInputStream(String sagaFactoryClassname, Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateFileInputStream(session, name);
    }

    /**
     * Creates a FileInputStream using the default session.
     * 
     * @param name
     *      location of the file.
     * @return
     *      the FileInputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileInputStream createFileInputStream(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileInputStream((Session) null, name);
    }
    

    /**
     * Creates a FileInputStream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @return
     *      the FileInputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileInputStream createFileInputStream(String sagaFactoryClassname, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileInputStream(sagaFactoryClassname, SessionFactory.createSession(sagaFactoryClassname), name);
    }

    /**
     * Creates a FileOutputStream.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @param append
     *      when set, the file is opened for appending.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileOutputStream createFileOutputStream(Session session,
            URL name, boolean append) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return createFileOutputStream((String) null, session, name, append);
    }
    
    /**
     * Creates a FileOutputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @param append
     *      when set, the file is opened for appending.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileOutputStream createFileOutputStream(String sagaFactoryClassname, Session session,
            URL name, boolean append) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateFileOutputStream(session, name, append);
    }


    /**
     * Creates a FileOutputStream.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileOutputStream createFileOutputStream(Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileOutputStream(session, name, false);
    }


    /**
     * Creates a FileOutputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the file.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileOutputStream createFileOutputStream(String sagaFactoryClassname, Session session,
            URL name) throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileOutputStream(sagaFactoryClassname, session, name, false);
    }

    /**
     * Creates a FileOutputStream using the default session.
     * 
     * @param name
     *      location of the file.
     * @param append
     *      when set, the file is opened for appending.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.  
     */
    public static FileOutputStream createFileOutputStream(URL name,
            boolean append) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return createFileOutputStream((Session) null, name, append);
    }
    

    /**
     * Creates a FileOutputStream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @param append
     *      when set, the file is opened for appending.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.  
     */
    public static FileOutputStream createFileOutputStream(String sagaFactoryClassname, URL name,
            boolean append) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, AlreadyExistsException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        Session session = SessionFactory.createSession(sagaFactoryClassname);
        return createFileOutputStream(sagaFactoryClassname, session, name, append);
    }


    /**
     * Creates a FileOutputStream using the default session.
     * 
     * @param name
     *      location of the file.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileOutputStream createFileOutputStream(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileOutputStream(name, false);
    }
    
    /**
     * Creates a FileOutputStream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the file.
     * @return
     *      the FileOutputStream instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL points to a directory,
     *      or is an invalid entry name.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the parent directory does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static FileOutputStream createFileOutputStream(String sagaFactoryClassname, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createFileOutputStream(sagaFactoryClassname, name, false);
    }


    /**
     * Creates a Directory.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createDirectory((String) null, session, name, flags);
    }
    

    /**
     * Creates a Directory.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(String sagaFactoryClassname, Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateDirectory(session, name, flags);
    }

    /**
     * Creates a Directory for reading.
     * 
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createDirectory(session, name, Flags.READ.getValue());
    }
    

    /**
     * Creates a Directory for reading.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param session
     *      the session handle.
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(String sagaFactoryClassname, Session session, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createDirectory(sagaFactoryClassname, session, name, Flags.READ.getValue());
    }


    /**
     * Creates a Directory, using the default session.
     * 
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createDirectory((Session) null, name, flags);
    }
    

    /**
     * Creates a Directory, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the directory.
     * @param flags
     *      the open mode.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is thrown if the specified URL already exists, and the
     *      <code>CREATE</code> and <code>EXCLUSIVE</code> flags are given.
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist, and the
     *      <code>CREATE</code> flag is not given.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(String sagaFactoryClassname, URL name, int flags)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession(sagaFactoryClassname);
        return createDirectory(sagaFactoryClassname, session, name, flags);
    }


    /**
     * Creates a Directory for reading, using the default session.
     * 
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createDirectory(name, Flags.READ.getValue());
    }
    

    /**
     * Creates a Directory for reading, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      location of the directory.
     * @return
     *      the directory instance.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception PermissionDeniedException
     *      is thrown when the method failed because the identity used did
     *      not have sufficient permissions to perform the operation
     *      successfully.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown when the specified URL is an invalid file name.
     * @exception IncorrectURLException
     *      is thrown when an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible.
     * @exception AlreadyExistsException
     *      is not thrown, but a method may be invoked that may throw it (but
     *      not in this case).
     * @exception DoesNotExistException
     *      is thrown if the specified URL does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static Directory createDirectory(String sagaFactoryClassname, URL name)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        return createDirectory(sagaFactoryClassname, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a File.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
        return createFile(null, mode, session, name, flags);
    }
    

    /**
     * Creates a task that creates a File.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(String sagaFactoryClassname, TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateFile(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a File for reading.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        return createFile(mode, session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a File for reading.
     *
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(String sagaFactoryClassname, TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        return createFile(sagaFactoryClassname, mode, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a File, using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(TaskMode mode, URL name,
            int flags) throws NotImplementedException, NoSuccessException {
        return createFile(mode, null, name, flags);
    }
    

    /**
     * Creates a task that creates a File, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(String sagaFactoryClassname, TaskMode mode, URL name,
            int flags) throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession(sagaFactoryClassname);
        return createFile(sagaFactoryClassname, mode, session, name, flags);
    }

    /**
     * Creates a task that creates a File for reading, using the default
     * session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(TaskMode mode, URL name)
            throws NotImplementedException, NoSuccessException {
        return createFile(mode, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a File for reading, using the default
     * session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, File> createFile(String sagaFactoryClassname, TaskMode mode, URL name)
            throws NotImplementedException, NoSuccessException {
        return createFile(sagaFactoryClassname, mode, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a FileInputStream.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileInputStream> createFileInputStream(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createFileInputStream(null, mode, session, name);
    }
    
    /**
     * Creates a task that creates a FileInputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileInputStream> createFileInputStream(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateFileInputStream(mode, session, name);
    }

    /**
     * Creates a task that creates a FileInputStream using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileInputStream> createFileInputStream(
            TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {

        return createFileInputStream(mode, null, name);
    }
    

    /**
     * Creates a task that creates a FileInputStream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileInputStream> createFileInputStream(
            String sagaFactoryClassname, TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createFileInputStream(sagaFactoryClassname, mode, null, name);
    }

    /**
     * Creates a task that creates a FileOutputStream.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createFileOutputStream(mode, session, name, false);
    }
    
    /**
     * Creates a task that creates a FileOutputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name)
            throws NotImplementedException, NoSuccessException {
        return createFileOutputStream(sagaFactoryClassname, mode, session, name, false);
    }

    /**
     * Creates a task that creates a FileOutputStream.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @param append
     *            when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            TaskMode mode, Session session, URL name, boolean append)
            throws NotImplementedException, NoSuccessException {
        return createFileOutputStream(null, mode, session, name, append);
    }
    

    /**
     * Creates a task that creates a FileOutputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @param append
     *            when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            String sagaFactoryClassname, TaskMode mode, Session session, URL name, boolean append)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateFileOutputStream(mode, session, name, append);
    }

    /**
     * Creates a task that creates a FileOutputStream using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the default session could not be created or
     *             when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createFileOutputStream(mode, name, false);
    }
    
    /**
     * Creates a task that creates a FileOutputStream using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the default session could not be created or
     *             when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            String sagaFactoryClassname, TaskMode mode, URL name) throws NotImplementedException,
            NoSuccessException {
        return createFileOutputStream(sagaFactoryClassname, mode, name, false);
    }

    /**
     * Creates a task that creates a FileOutputStream.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @param append
     *            when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the default session could not be created or
     *             when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            TaskMode mode, URL name, boolean append)
            throws NotImplementedException, NoSuccessException {
        return createFileOutputStream(mode, null, name, append);
    }
    
    /**
     * Creates a task that creates a FileOutputStream.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the file.
     * @param append
     *            when set, the file is opened for appending.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the default session could not be created or
     *             when the Saga factory could not be created.
     */
    public static Task<FileFactory, FileOutputStream> createFileOutputStream(
            String sagaFactoryClassname, TaskMode mode, URL name, boolean append)
            throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession(sagaFactoryClassname);
        return createFileOutputStream(sagaFactoryClassname, mode, session, name, append);
    }


    /**
     * Creates a task that creates a Directory.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
        return createDirectory(null, mode, session, name, flags);
    }
    

    /**
     * Creates a task that creates a Directory.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(String sagaFactoryClassname, TaskMode mode,
            Session session, URL name, int flags)
            throws NotImplementedException, NoSuccessException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateDirectory(mode, session, name, flags);
    }

    /**
     * Creates a task that creates a Directory for reading.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        return createDirectory(mode, session, name, Flags.READ.getValue());
    }
    
    /**
     * Creates a task that creates a Directory for reading.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(String sagaFactoryClassname, TaskMode mode,
            Session session, URL name) throws NotImplementedException,
            NoSuccessException {
        return createDirectory(sagaFactoryClassname, mode, session, name, Flags.READ.getValue());
    }

    /**
     * Creates a task that creates a Directory, using the default session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(TaskMode mode,
            URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        return createDirectory(mode, null, name, flags);
    }
    

    /**
     * Creates a task that creates a Directory, using the default session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
     * @param flags
     *            the open mode.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(String sagaFactoryClassname, TaskMode mode,
            URL name, int flags) throws NotImplementedException,
            NoSuccessException {
        Session session = SessionFactory.createSession(sagaFactoryClassname);
        return createDirectory(sagaFactoryClassname, mode, session, name, flags);
    }

    /**
     * Creates a task that creates a Directory for reading, using the default
     * session.
     * 
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(TaskMode mode,
            URL name) throws NotImplementedException, NoSuccessException {
        return createDirectory(mode, name, Flags.READ.getValue());
    }
    

    /**
     * Creates a task that creates a Directory for reading, using the default
     * session.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param mode
     *            the task mode.
     * @param name
     *            location of the directory.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented
     * @exception NoSuccessException
     *                is thrown when the default session could not be created or
     *                when the Saga factory could not be created.
     */
    public static Task<FileFactory, Directory> createDirectory(String sagaFactoryClassname, TaskMode mode,
            URL name) throws NotImplementedException, NoSuccessException {
        return createDirectory(sagaFactoryClassname, mode, name, Flags.READ.getValue());
    }
}
