package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.adaptor.DataAdaptorFactory;
import fr.in2p3.jsaga.engine.data.*;
import fr.in2p3.jsaga.engine.security.ContextImpl;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceFactoryImpl extends NamespaceFactory {
    /**
     * Notes:
     * <br> - do not check existance of entry, a method exists() has been added to NamespaceEntry instead.
     * <br> - do not throw BadParameter exception if entry is a directory, create a NamespaceDirectory object instead.
     * <br> - if entry is not a directory, then flags are ignored.
     */
    protected NamespaceEntry doCreateNamespaceEntry(Session session, URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess, IncorrectState, AlreadyExists {
        if (name==null || name.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name");
        }
        if (name.getPath().endsWith("/")) {
            return this.doCreateNamespaceDirectory(session, name, flags);
        } else {
            return this.doCreateNamespaceFile(session, name, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry, a method exists() has been added to NamespaceEntry instead.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    protected NamespaceDirectory doCreateNamespaceDirectory(Session session, URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess, IncorrectState, AlreadyExists {
        if (name==null || name.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name");
        }
        DataAdaptor dataAdaptor = createDataAdaptor(session, name);
        DataConnection connection = new DataConnection(dataAdaptor);
        if (dataAdaptor instanceof LogicalReader || dataAdaptor instanceof LogicalWriter) {
            return new LogicalDirectoryImpl(session, name, PhysicalEntryFlags.cast(flags), connection);
        } else {
            return new DirectoryImpl(session, name, PhysicalEntryFlags.cast(flags), connection);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry, a method exists() has been added to NamespaceEntry instead.
     * <br> - support the CREATEPARENTS flag (for consistency with NamespaceDirectory constructor).
     * <br> - support the CREATE flag, but use create the entry on later operations (e.g. write, addLocation).
     */
    protected NamespaceEntry doCreateNamespaceFile(Session session, URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess, IncorrectState {
        if (name==null || name.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name");
        }
        DataAdaptor dataAdaptor = createDataAdaptor(session, name);
        DataConnection connection = new DataConnection(dataAdaptor);
        if (dataAdaptor instanceof LogicalReader || dataAdaptor instanceof LogicalWriter) {
            return new LogicalFileImpl(session, name, PhysicalEntryFlags.cast(flags), connection);
        } else {
            return new FileImpl(session, name, PhysicalEntryFlags.cast(flags), connection);
        }
    }

    private DataAdaptor createDataAdaptor(Session session, URI name) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess, IncorrectState {
        ContextImpl context = new DataContextSelector(session).selectContextByURI(name);
        if (context != null) {
            String contextType;
            try {
                contextType = context.getAttribute("Type");
            } catch (ReadOnly e) {
                throw new NoSuccess(e);
            }
            return DataAdaptorFactory.getInstance().getDataAdaptor(
                    name,
                    context.createSecurityAdaptor(),
                    contextType);
        } else {
            return DataAdaptorFactory.getInstance().getDataAdaptor(name, null, null);
        }
    }
}
