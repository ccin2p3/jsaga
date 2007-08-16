package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DirectoryImpl extends AbstractNamespaceDirectoryImpl implements Directory {
    /** constructor */
    public DirectoryImpl(Session session, URI uri, PhysicalEntryFlags flags, DataAdaptor adaptor) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, adaptor);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.READ);
        effectiveFlags.keepPhysicalEntryFlags();
    }

    /** constructor for deepCopy */
    protected DirectoryImpl(AbstractNamespaceDirectoryImpl source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new DirectoryImpl(this);
    }

    public NamespaceDirectory openDir(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(m_session, resolveURI(name), PhysicalEntryFlags.cast(flags), m_adaptor);
    }

    public NamespaceEntry openEntry(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new FileImpl(m_session, resolveURI(name), PhysicalEntryFlags.cast(flags), m_adaptor);
    }

    protected NamespaceDirectory _openParentDir(Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(m_session, super._getParentDirURI(), PhysicalEntryFlags.cast(flags), m_adaptor);
    }
}
