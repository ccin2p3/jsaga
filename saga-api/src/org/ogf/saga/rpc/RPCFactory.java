package org.ogf.saga.rpc;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;

/**
 * Factory for objects from the RPC package.
 */
public abstract class RPCFactory {

    private static RPCFactory factory;

    private static synchronized void initializeFactory()
            throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createRPCFactory();
        }
    }

    /**
     * Creates a Parameter object. To be provided by the implementation.
     * 
     * @param data
     *            data to be used.
     * @param mode
     *            IN, OUT, INOUT.
     * @return the parameter.
     */
    protected abstract Parameter doCreateParameter(Object data, IOMode mode)
            throws BadParameterException, NoSuccessException,
            NotImplementedException;

    /**
     * Creates a RPC handle instance. To be provided by the implementation.
     * 
     * @param session
     *            the session handle.
     * @param funcname
     *            specification of the remote procedure.
     * @return the RPC handle instance.
     */
    protected abstract RPC doCreateRPC(Session session, URL funcname)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Creates a task that creates a RPC handle instance. To be provided by the
     * implementation.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param funcname
     *            specification of the remote procedure.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    protected abstract Task<RPCFactory, RPC> doCreateRPC(TaskMode mode,
            Session session, URL funcname) throws NotImplementedException;

    /**
     * Creates a Parameter object. If the mode indicates an Out parameter,
     * <code>data</code> may be <code>null</code>.
     * 
     * @param data
     *      data to be used.
     * @param mode
     *      IN, OUT, INOUT.
     * @return 
     *      the parameter.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the given
     *      data object.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Parameter createParameter(Object data, IOMode mode)
            throws BadParameterException, NoSuccessException,
            NotImplementedException {
        initializeFactory();
        return factory.doCreateParameter(data, mode);
    }

    /**
     * Creates a Parameter object. To be provided by the implementation.
     * 
     * @param mode
     *      IN, OUT, INOUT.
     * @return
     *      the parameter.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is not thrown, but this method may call a method that does
     *      throw it (although not in this case).
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Parameter createParameter(IOMode mode)
            throws BadParameterException, NoSuccessException,
            NotImplementedException {
        return createParameter(null, mode);
    }

    /**
     * Creates an IN Parameter object.
     * 
     * @param data
     *      data to be used.
     * @return
     *      the parameter.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is thrown when the implementation cannot handle the given
     *      data object.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Parameter createParameter(Object data)
            throws BadParameterException, NoSuccessException,
            NotImplementedException {
        return createParameter(data, IOMode.IN);
    }

    /**
     * Creates an IN Parameter object.
     * 
     * @return
     *      the parameter.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception BadParameterException
     *      is not thrown, but this method may call a method that does
     *      throw it (although not in this case).
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public static Parameter createParameter() throws BadParameterException,
            NoSuccessException, NotImplementedException {
        return createParameter(IOMode.IN);
    }

    /**
     * Creates a RPC handle instance.
     * 
     * @param session
     *      the session handle.
     * @param funcname
     *      specification of the remote procedure.
     * @return
     *      the RPC handle instance.
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
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible. Note that this exception can also
     *      be thrown when actually invoking a {@link RPC#call(Parameter...)},
     *      which may be more convenient for implementations.
     * @exception DoesNotExistException
     *      is thrown if the specified function does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static RPC createRPC(Session session, URL funcname)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        initializeFactory();
        return factory.doCreateRPC(session, funcname);
    }

    /**
     * Creates a RPC handle instance using the default session.
     * 
     * @param funcname
     *      specification of the remote procedure.
     * @return
     *      the RPC handle instance.
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
     *      is thrown when the specified URL cannot be found.
     * @exception IncorrectURLException
     *      is thrown if an implementation cannot handle the specified
     *      protocol, or that access to the specified entity via the
     *      given protocol is impossible. Note that this exception can also
     *      be thrown when actually invoking a {@link RPC#call(Parameter...)},
     *      which may be more convenient for implementations.
     * @exception DoesNotExistException
     *      is thrown if the specified function does not exist.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public static RPC createRPC(URL funcname) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        Session session = SessionFactory.createSession();
        return createRPC(session, funcname);
    }

    /**
     * Creates a task that creates a RPC handle instance.
     * 
     * @param mode
     *            the task mode.
     * @param session
     *            the session handle.
     * @param funcname
     *            specification of the remote procedure.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created or when
     *             the default session could not be created.
     */
    public static Task<RPCFactory, RPC> createRPC(TaskMode mode,
            Session session, URL funcname) throws NotImplementedException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateRPC(mode, session, funcname);
    }

    /**
     * Creates a task that creates a RPC handle instance using the default
     * session.
     * 
     * @param mode
     *            the task mode.
     * @param funcname
     *            specification of the remote procedure.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     * @exception NoSuccessException
     *                is thrown when the Saga factory could not be created or
     *                when the default session could not be created.
     */
    public static Task<RPCFactory, RPC> createRPC(TaskMode mode, URL funcname)
            throws NotImplementedException, NoSuccessException {
        Session session = SessionFactory.createSession();
        return createRPC(mode, session, funcname);
    }
}
