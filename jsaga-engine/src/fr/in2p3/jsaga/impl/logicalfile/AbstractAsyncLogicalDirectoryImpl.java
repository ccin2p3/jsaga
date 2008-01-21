package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
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
* File:   AbstractAsyncLogicalDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncLogicalDirectoryImpl extends AbstractNSDirectoryImplWithAsyncAttributes implements LogicalDirectory {
    /** constructor for factory */
    public AbstractAsyncLogicalDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for open() */
    public AbstractAsyncLogicalDirectoryImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, url, flags);
    }

    public Task<Boolean> isFile(TaskMode mode, URL name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalDirectoryImpl.class.getMethod("isFile", new Class[]{URL.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<List<URL>> find(TaskMode mode, String namePattern, String[] attrPattern, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalDirectoryImpl.class.getMethod("find", new Class[]{String.class, String[].class, int.class}),
                    new Object[]{namePattern, attrPattern, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<List<URL>> find(TaskMode mode, String namePattern, String[] attrPattern) throws NotImplemented {
        return this.find(mode, namePattern, attrPattern, Flags.RECURSIVE.getValue());
    }

    public Task<LogicalDirectory> openLogicalDir(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalDirectoryImpl.class.getMethod("openLogicalDir", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<LogicalDirectory> openLogicalDir(TaskMode mode, URL name) throws NotImplemented {
        return this.openLogicalDir(mode, name, Flags.READ.getValue());
    }

    public Task<LogicalFile> openLogicalFile(TaskMode mode, URL name, int flags) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    LogicalDirectoryImpl.class.getMethod("openLogicalFile", new Class[]{URL.class, int.class}),
                    new Object[]{name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
    public Task<LogicalFile> openLogicalFile(TaskMode mode, URL name) throws NotImplemented {
        return this.openLogicalFile(mode, name, Flags.READ.getValue());
    }
}
