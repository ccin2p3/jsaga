package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DirectoryReader;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.engine.factories.NamespaceFactoryImpl;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNamespaceEntryDirImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNamespaceEntryDirImpl extends AbstractNamespaceEntryImpl implements NamespaceDirectory {
    /** constructor for AbstractNamespaceDirectoryImpl */
    protected AbstractNamespaceEntryDirImpl(Session session, URI uri, Flags flags, DataConnection connection) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, connection);
    }

    /** constructor for deepCopy */
    protected AbstractNamespaceEntryDirImpl(AbstractNamespaceEntryImpl source) {
        super(source);
    }

    /** overload super.getCWD() */
    public URI getCWD() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return m_uri;
    }

    /** overload super.copy() */
    public void copy(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE).or(Flags.OVERWRITE).or(Flags.CREATEPARENTS));
        effectiveFlags.checkRequired(Flags.RECURSIVE);
        URI effectiveTarget = this._getEffectiveURI(target);
        if (m_adaptor instanceof DirectoryReader) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // copy source childs
            FileAttributes[] sourceChilds = this._listAttributes(m_uri.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                NamespaceEntry sourceChildEntry = this._openNS(sourceChilds[i]);
                URI targetChild = effectiveTarget.resolve(sourceChilds[i].getName());
                Flags targetFlags =
                        (sourceChildEntry instanceof NamespaceDirectory
                                ? flags
                                : effectiveFlags.remove(Flags.RECURSIVE));
                sourceChildEntry.copy(targetChild, targetFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /** overload super.copyFrom() */
    public void copyFrom(URI source, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE).or(Flags.OVERWRITE));
        effectiveFlags.checkRequired(Flags.RECURSIVE);
        URI effectiveSource = this._getEffectiveURI(source);
        if (m_adaptor instanceof DirectoryWriter) {
            // copy source childs
            AbstractNamespaceDirectoryImpl sourceDir;
            try {
                sourceDir = (AbstractNamespaceDirectoryImpl) NamespaceFactory.createNamespaceDirectory(m_session, effectiveSource, Flags.NONE);
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            } catch (IncorrectSession e) {
                throw new NoSuccess(e);
            } catch (AlreadyExists e) {
                throw new NoSuccess("Unexpected exception", e);
            }
            // copy source childs
            FileAttributes[] sourceChilds = sourceDir._listAttributes(m_uri.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                NamespaceEntry sourceChildEntry = sourceDir._openNS(sourceChilds[i]);
                URI targetChild = m_uri.resolve(sourceChilds[i].getName());
                Flags targetFlags =
                        (sourceChildEntry instanceof NamespaceDirectory
                                ? flags
                                : effectiveFlags.remove(Flags.RECURSIVE));
                try {
                    sourceChildEntry.copy(targetChild, targetFlags);
                } catch (AlreadyExists e) {
                    throw new NoSuccess("Unexpected exception", e);
                }
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /** overload super.move() */
    public void move(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().move(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE).or(Flags.OVERWRITE).or(Flags.CREATEPARENTS));
        effectiveFlags.checkRequired(Flags.RECURSIVE);
        URI effectiveTarget = this._getEffectiveURI(target);
        if (m_adaptor instanceof DataRename
                && m_uri.getScheme().equals(effectiveTarget.getScheme())
                && (m_uri.getUserInfo()==null || m_uri.getUserInfo().equals(effectiveTarget.getUserInfo()))
                && (m_uri.getHost()==null || m_uri.getHost().equals(effectiveTarget.getHost()))
                && (m_uri.getPort()==effectiveTarget.getPort()))
        {
            boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
            try {
                ((DataRename)m_adaptor).rename(m_uri.getPath(), effectiveTarget.getPath(), overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Directory does not exist: "+m_uri, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DirectoryReader) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // move source childs to target directory
            FileAttributes[] sourceChilds = this._listAttributes(m_uri.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                URI remoteChild = effectiveTarget.resolve(sourceChilds[i].getName());
                NamespaceEntry entry = this._openNS(sourceChilds[i]);
                if (entry instanceof NamespaceDirectory) {
                    entry.move(remoteChild, flags);
                } else {
                    entry.move(remoteChild, effectiveFlags.remove(Flags.RECURSIVE));
                }
            }
            // remove source directory
            this.remove(flags);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /**
     * overload super.remove()
     * <br>Note: does not throw a BadParamater exception when RECURSIVE flag is not set, unless directory has some descendants.
     */
    public void remove(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().remove(effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));
        if (m_adaptor instanceof DirectoryReader) {
            // remove source childs
            if (effectiveFlags.contains(Flags.RECURSIVE)) {
                FileAttributes[] sourceChilds = this._listAttributes(m_uri.getPath());
                for (int i=0; i<sourceChilds.length; i++) {
                    NamespaceEntry entry = this._openNS(sourceChilds[i]);
                    if (entry instanceof NamespaceDirectory) {
                        entry.remove(flags);
                    } else {
                        entry.remove(effectiveFlags.remove(Flags.RECURSIVE));
                    }
                }
            }
            // remove this directory
            URI parent = super._getParentDirURI();
            String directoryName = super.getName();
            try {
                ((DirectoryWriter)m_adaptor).removeDir(parent.getPath(), directoryName);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Directory does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    /** overload super._getEffectiveURI() */
    protected URI _getEffectiveURI(URI target) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (target.getPath().endsWith("/")) {
            return target.resolve(this.getName()+"/");
        } else {
            return target;
        }
    }

    protected FileAttributes[] _listAttributes(String absolutePath) throws NotImplemented, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof DirectoryReader) {
            try {
                return ((DirectoryReader)m_adaptor).listAttributes(absolutePath);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Directory does not exist: "+absolutePath, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    protected NamespaceEntry _openNS(FileAttributes attr) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            switch(attr.getType()) {
                case FileAttributes.DIRECTORY_TYPE:
                    return this._openNSDir(new URI(attr.getName()));
                case FileAttributes.FILE_TYPE:
                case FileAttributes.LINK_TYPE:
                    return this._openNSEntry(new URI(attr.getName()));
                case FileAttributes.UNKNOWN_TYPE:
                default:
                    if (this.isDir(new URI(attr.getName()), Flags.NONE)) {
                        return this._openNSDir(new URI(attr.getName()));
                    } else {
                        return this._openNSEntry(new URI(attr.getName()));
                    }
            }
        } catch (URISyntaxException e) {
            throw new NoSuccess("Incorrect URL: "+attr.getName(), e);
        } catch (IncorrectURL e) {
            throw new NoSuccess("Incorrect URL: "+attr.getName(), e);
        }
    }

    protected NamespaceDirectory _openNSDir(URI name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return this.openDir(name, Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        }
    }

    protected NamespaceEntry _openNSEntry(URI name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return this.openEntry(name, Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        }
    }

    protected void _makeDir(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // set makeDirFlags
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        Flags makeDirFlags = Flags.CREATE;
        if (! effectiveFlags.contains(Flags.OVERWRITE)) {
            makeDirFlags = makeDirFlags.or(Flags.EXCL);
        }
        if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            makeDirFlags = makeDirFlags.or(Flags.CREATEPARENTS);
        }
        // makeDir
        try {
            NamespaceFactoryImpl.createNamespaceDirectory(m_session, target, makeDirFlags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        }
    }
}
