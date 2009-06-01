package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncFileFactoryImpl extends AbstractSyncFileFactoryImpl {
    public AbstractAsyncFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    protected Task<FileFactory, File> doCreateFile(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,File>().create(
                mode, null, this,
                "doCreateFileSync",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }

    protected Task<FileFactory, FileInputStream> doCreateFileInputStream(TaskMode mode, Session session, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,FileInputStream>().create(
                mode, null, this,
                "doCreateFileInputStreamSync",
                new Class[]{Session.class, URL.class},
                new Object[]{session, name});
    }

    protected Task<FileFactory, FileOutputStream> doCreateFileOutputStream(TaskMode mode, Session session, URL name, boolean append) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,FileOutputStream>().create(
                mode, null, this,
                "doCreateFileOutputStreamSync",
                new Class[]{Session.class, URL.class, boolean.class},
                new Object[]{session, name, append});
    }

    protected Task<FileFactory, Directory> doCreateDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,Directory>().create(
                mode, null, this,
                "doCreateDirectorySync",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }
}
