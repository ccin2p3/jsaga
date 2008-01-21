package org.ogf.saga.rpc;

import org.ogf.saga.URL;
import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.Task;

/**
 * Factory for objects from the RPC package.
 * Note: the createParameter methods can also throw NotImplemented, because the
 * Buffer create methods can. Error in the SAGA specifications???
 */
public abstract class RPCFactory {
    
    private static RPCFactory factory;

    private static synchronized void initializeFactory() {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createRPCFactory();
        }
    }

    /**
     * Creates a Parameter object. To be provided by the implementation.
     * @param data data to be used.
     * @param mode IN, OUT, INOUT.
     * @return the parameter.
     */
    protected abstract Parameter doCreateParameter(byte[] data, IOMode mode)
        throws BadParameter, NoSuccess, NotImplemented;
    
    /**
     * Creates a Parameter object. To be provided by the implementation.
     * @param mode IN, OUT, INOUT.
     * @return the parameter.
     */
    protected abstract Parameter doCreateParameter(IOMode mode)
        throws BadParameter, NoSuccess, NotImplemented;
    
    /**
     * Creates a Parameter object. To be provided by the implementation.
     * @param sz the size of the buffer.
     * @param mode IN, OUT, INOUT.
     * @return the parameter.
     */
    protected abstract Parameter doCreateParameter(int sz, IOMode mode)
        throws BadParameter, NoSuccess, NotImplemented;
 
    /**
     * Creates a RPC handle instance. To be provided by the implementation.
     * @param session the session handle.
     * @param funcname specification of the remote procedure.
     * @return the RPC handle instance.
     */
    protected abstract RPC doCreateRPC(Session session, URL funcname)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            BadParameter, DoesNotExist, Timeout, NoSuccess;
    
    /**
     * Creates a task that creates a RPC handle instance. 
     * To be provided by the implementation.
     * @param mode the task mode.
     * @param session the session handle.
     * @param funcname specification of the remote procedure.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    protected abstract Task<RPC> doCreateRPC(TaskMode mode,
            Session session, URL funcname)
        throws NotImplemented;
    
    /**
     * Creates a Parameter object. If the mode indicates an Out parameter,
     * <code>data</code> may be <code>null</code>.
     * @param data data to be used.
     * @param mode IN, OUT, INOUT.
     * @return the parameter.
     * @throws NotImplemented 
     */
    public static Parameter createParameter(byte[] data, IOMode mode)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateParameter(data, mode);
    }
    
    /**
     * Creates a Parameter object. To be provided by the implementation.
     * @param mode IN, OUT, INOUT.
     * @return the parameter.
     * @throws NotImplemented 
     */
    public static Parameter createParameter(IOMode mode)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateParameter(mode);
    }
    
    /**
     * Creates a Parameter object. To be provided by the implementation.
     * @param sz the size of the buffer.
     * @param mode IN, OUT, INOUT.
     * @return the parameter.
     * @throws NotImplemented 
     */
    public static Parameter createParameter(int sz, IOMode mode)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateParameter(sz, mode);
    }
 
    /**
     * Creates an IN Parameter object.
     * @param data data to be used.
     * @return the parameter.
     * @throws NotImplemented 
     */
    public static Parameter createParameter(byte[] data)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateParameter(data, IOMode.IN);
    }
    
    /**
     * Creates an IN Parameter object.
     * @return the parameter.
     * @throws NotImplemented 
     */
    public static Parameter createParameter()
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateParameter(IOMode.IN);
    }
    
    /**
     * Creates an IN Parameter object.
     * @param sz the size of the buffer.
     * @return the parameter.
     * @throws NotImplemented 
     */
    public static Parameter createParameter(int sz)
        throws BadParameter, NoSuccess, NotImplemented {
        initializeFactory();
        return factory.doCreateParameter(sz, IOMode.IN);
    }
    
    
    /**
     * Creates a RPC handle instance.
     * @param session the session handle.
     * @param funcname specification of the remote procedure.
     * @return the RPC handle instance.
     */
    public static RPC createRPC(Session session, URL funcname)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
                BadParameter, DoesNotExist, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateRPC(session, funcname);
    }
    
    /**
     * Creates a task that creates a RPC handle instance.
     * @param mode the task mode.
     * @param session the session handle.
     * @param funcname specification of the remote procedure.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public static Task<RPC> createRPC(TaskMode mode, Session session,
            URL funcname) throws NotImplemented {
        initializeFactory();
        return factory.doCreateRPC(mode, session, funcname);
    }
    
    /**
     * Creates a RPC handle instance using the default session.
     * @param funcname specification of the remote procedure.
     * @return the RPC handle instance.
     */
    public static RPC createRPC(URL funcname)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
                BadParameter, DoesNotExist, Timeout, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateRPC(session, funcname);
    }
    
    /**
     * Creates a task that creates a RPC handle instance using the default session.
     * @param mode the task mode.
     * @param funcname specification of the remote procedure.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     * @exception NoSuccess is thrown when the default session could not be
     *     created.
     */
    public static Task<RPC> createRPC(TaskMode mode,URL funcname)
            throws NotImplemented, NoSuccess {
        Session session = SessionFactory.createSession();
        initializeFactory();
        return factory.doCreateRPC(mode, session, funcname);
    }
}
