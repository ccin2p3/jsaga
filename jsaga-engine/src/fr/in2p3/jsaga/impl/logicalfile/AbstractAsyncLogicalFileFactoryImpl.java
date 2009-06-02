package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.logicalfile.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncLogicalFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncLogicalFileFactoryImpl extends AbstractSyncLogicalFileFactoryImpl {
    public AbstractAsyncLogicalFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    protected Task<LogicalFileFactory, LogicalFile> doCreateLogicalFile(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFileFactory,LogicalFile>().create(
                mode, null, this,
                "doCreateLogicalFileSync",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }

    protected Task<LogicalFileFactory, LogicalDirectory> doCreateLogicalDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFileFactory,LogicalDirectory>().create(
                mode, null, this,
                "doCreateLogicalDirectorySync",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }
}
