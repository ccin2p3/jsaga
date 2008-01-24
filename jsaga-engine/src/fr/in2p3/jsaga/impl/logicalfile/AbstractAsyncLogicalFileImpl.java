package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncLogicalFileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncLogicalFileImpl extends AbstractNSEntryImplWithMetaData implements LogicalFile {
    /** constructor for factory */
    public AbstractAsyncLogicalFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for open() */
    public AbstractAsyncLogicalFileImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(entry, url, flags);
    }

    public Task addLocation(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalFileImpl.class.getMethod("addLocation", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task removeLocation(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalFileImpl.class.getMethod("removeLocation", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task updateLocation(TaskMode mode, URL nameOld, URL nameNew) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalFileImpl.class.getMethod("updateLocation", new Class[]{URL.class, URL.class}),
                    new Object[]{nameOld, nameNew}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<List<URL>> listLocations(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalFileImpl.class.getMethod("listLocations", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task replicate(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalFileImpl.class.getMethod("replicate", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task replicate(TaskMode mode, URL name) throws NotImplemented {
        return this.replicate(mode, name, Flags.NONE.getValue());
    }
}
