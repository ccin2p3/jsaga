package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.sync.logicalfile.SyncLogicalFileFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncLogicalFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncLogicalFileFactoryImpl extends LogicalFileFactory implements SyncLogicalFileFactory {
    protected static final String JSAGA_FACTORY = Base.getSagaFactory();

    private final static boolean PLUGIN_TYPE = DataAdaptorFactory.LOGICAL;
    private DataAdaptorFactory m_adaptorFactory;

    public AbstractSyncLogicalFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    public LogicalFile doCreateLogicalFileSync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptorAndConnect(name, session, PLUGIN_TYPE);
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        if (isLogical || !isPhysical) {
            return new LogicalFileImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameterException("Not a logical file URL: "+name);
        }
    }

    public LogicalDirectory doCreateLogicalDirectorySync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptorAndConnect(name, session, PLUGIN_TYPE);
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        if (isLogical || !isPhysical) {
            return new LogicalDirectoryImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameterException("Not a logical directory URL: "+name);
        }
    }
}
