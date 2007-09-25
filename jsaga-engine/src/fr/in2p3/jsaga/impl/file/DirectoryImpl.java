package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNamespaceEntryImpl;
import org.ogf.saga.*;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DirectoryImpl extends AbstractDirectoryTaskImpl implements Directory {
    /** constructor for factory */
    public DirectoryImpl(Session session, URI uri, DataAdaptor adaptor, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor, flags);
    }

    /** constructor for open() */
    public DirectoryImpl(AbstractNamespaceEntryImpl entry, URI uri, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, uri, flags);
    }

    /** constructor for deepCopy */
    protected DirectoryImpl(AbstractNamespaceEntryImpl source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new DirectoryImpl(this);
    }

    public ObjectType getType() {
        return ObjectType.DIRECTORY;
    }

    /** implements super.openDir() */
    public NamespaceDirectory openDir(URI name, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openDirectory(name, flags);
    }

    /** implements super.open() */
    public NamespaceEntry open(URI name, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openFile(name, flags);
    }

    /** @return 0 */
    public long getSize(URI name, Flags... flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        return 0;
    }

    public boolean isFile(URI name, Flags... flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return super.isEntry(name, flags);
    }

    public Directory openDirectory(URI relativePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(this, super._resolveRelativeURI(relativePath), flags);
    }

    public File openFile(URI relativePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new FileImpl(this, super._resolveRelativeURI(relativePath), flags);
    }
}
