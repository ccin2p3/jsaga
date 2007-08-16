package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
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
    protected AbstractNamespaceEntryDirImpl(Session session, URI uri, DataAdaptor adaptor) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor);
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
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.OVERWRITE).or(Flags.CREATEPARENTS).or(Flags.DEREFERENCE));
        effectiveFlags.checkRequired(Flags.RECURSIVE);
        URI effectiveTarget = this._getEffectiveTarget(target);
        if (m_adaptor instanceof DirectoryReader) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // copy source childs
            FileAttributes[] sourceChilds = ((DirectoryReader)m_adaptor).listAttributes(m_uri.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                URI remoteChild = effectiveTarget.resolve(sourceChilds[i].name);
                NamespaceEntry entry = this._openNS(sourceChilds[i]);
                if (entry instanceof NamespaceDirectory) {
                    entry.copy(remoteChild, flags);
                } else {
                    entry.copy(remoteChild, effectiveFlags.remove(Flags.RECURSIVE));
                }
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /** overload super.move() */
    public void move(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.OVERWRITE).or(Flags.CREATEPARENTS).or(Flags.DEREFERENCE));
        effectiveFlags.checkRequired(Flags.RECURSIVE);
        URI effectiveTarget = this._getEffectiveTarget(target);
        if (m_adaptor instanceof DirectoryReader) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // move source childs to target directory
            FileAttributes[] sourceChilds = ((DirectoryReader)m_adaptor).listAttributes(m_uri.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                URI remoteChild = effectiveTarget.resolve(sourceChilds[i].name);
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
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));
        URI effectiveSource = this._getEffectiveURI(effectiveFlags);
        if (m_adaptor instanceof DirectoryReader) {
            // remove source childs
            if (effectiveFlags.contains(Flags.RECURSIVE)) {
                FileAttributes[] sourceChilds = ((DirectoryReader)m_adaptor).listAttributes(effectiveSource.getPath());
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
            ((DirectoryWriter)m_adaptor).removeDir(m_uri.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    protected NamespaceEntry _openNS(FileAttributes attr) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            switch(attr.type) {
                case FileAttributes.DIRECTORY_TYPE:
                    return this._openNSDir(new URI(attr.name));
                case FileAttributes.FILE_TYPE:
                case FileAttributes.LINK_TYPE:
                    return this._openNSEntry(new URI(attr.name));
                case FileAttributes.UNKNOWN_TYPE:
                default:
                    if (this.isDir(new URI(attr.name), Flags.NONE)) {
                        return this._openNSDir(new URI(attr.name));
                    } else {
                        return this._openNSEntry(new URI(attr.name));
                    }
            }
        } catch (URISyntaxException e) {
            throw new NoSuccess("Incorrect URL: "+attr.name, e);
        } catch (IncorrectURL e) {
            throw new NoSuccess("Incorrect URL: "+attr.name, e);
        }
    }

    protected NamespaceDirectory _openNSDir(URI name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return this.openDir(name, Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (AlreadyExists e) {
            throw new NoSuccess(e);
        }
    }

    protected NamespaceEntry _openNSEntry(URI name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return this.openEntry(name, Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (AlreadyExists e) {
            throw new NoSuccess(e);
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
