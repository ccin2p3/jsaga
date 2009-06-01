package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.file.DirectoryImpl;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalDirectoryImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileImpl;
import fr.in2p3.jsaga.sync.namespace.SyncNSFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncNSFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncNSFactoryImpl extends NSFactory implements SyncNSFactory {
    private DataAdaptorFactory m_adaptorFactory;

    public AbstractSyncNSFactoryImpl(DataAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - do not throw BadParameterException exception if entry is a directory, create a NSDirectory object instead.
     */
    public NSEntry doCreateNSEntrySync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (name.getPath().endsWith("/")) {
            return this.doCreateNSDirectorySync(session, name, flags);
        } else {
            return this.doCreateNSFileSync(session, name, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    public NSDirectory doCreateNSDirectorySync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new DirectoryImpl(session, name, adaptor, flags);
        } else {
            return new LogicalDirectoryImpl(session, name, adaptor, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    private NSEntry doCreateNSFileSync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new FileImpl(session, name, adaptor, flags);
        } else {
            return new LogicalFileImpl(session, name, adaptor, flags);
        }
    }
}
