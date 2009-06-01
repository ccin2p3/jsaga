package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncNSFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncNSFactoryImpl extends AbstractSyncNSFactoryImpl {
    public AbstractAsyncNSFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    ////////////////////////////////////////// interface NSFactory //////////////////////////////////////////

    protected Task<NSFactory, NSEntry> doCreateNSEntry(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSFactory,NSEntry>().create(
                mode, null, this,
                "doCreateNSEntrySync",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }

    protected Task<NSFactory, NSDirectory> doCreateNSDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSFactory,NSDirectory>().create(
                mode, null, this,
                "doCreateNSDirectorySync",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }
}
