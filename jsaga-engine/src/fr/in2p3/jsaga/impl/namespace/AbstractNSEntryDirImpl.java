package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DirectoryReader;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.helpers.URLFactory;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryDirImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 * This class override some methods of AbstractNSEntryImpl for directories
 */
public abstract class AbstractNSEntryDirImpl extends AbstractNSEntryImpl implements NSDirectory {
    /** constructor for factory */
    public AbstractNSEntryDirImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for open() */
    public AbstractNSEntryDirImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(entry, url, flags);
    }

    /** override super.getCWD() */
    public URL getCWD() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return m_url.normalize();
    }

    /** override super.copy() */
    public void copy(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE.or(Flags.OVERWRITE.or(Flags.CREATEPARENTS))));
        effectiveFlags.checkRequired(Flags.RECURSIVE.getValue());
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DirectoryReader) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // copy source childs
            FileAttributes[] sourceChilds = this._listAttributes(JSagaURL.decode(m_url.getPath()));
            for (int i=0; i<sourceChilds.length; i++) {
                NSEntry sourceChildEntry = this._openNS(sourceChilds[i]);
                URL targetChild = URLFactory.createURL(effectiveTarget, sourceChilds[i].getName());
                int targetFlags =
                        (sourceChildEntry instanceof NSDirectory
                                ? flags
                                : effectiveFlags.remove(Flags.RECURSIVE));
                sourceChildEntry.copy(targetChild, targetFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    /** override super.copyFrom() */
    public void copyFrom(URL source, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE.or(Flags.OVERWRITE)));
        effectiveFlags.checkRequired(Flags.RECURSIVE.getValue());
        URL effectiveSource = this._getEffectiveURL(source);
        if (m_adaptor instanceof DirectoryWriter) {
            // copy source childs
            AbstractNSDirectoryImpl sourceDir;
            try {
                sourceDir = (AbstractNSDirectoryImpl) NSFactory.createNSDirectory(m_session, effectiveSource, Flags.NONE.getValue());
            } catch (AlreadyExists e) {
                throw new NoSuccess("Unexpected exception: AlreadyExists", e);
            }
            // copy source childs
            FileAttributes[] sourceChilds = sourceDir._listAttributes(JSagaURL.decode(m_url.getPath()));
            for (int i=0; i<sourceChilds.length; i++) {
                NSEntry sourceChildEntry = sourceDir._openNS(sourceChilds[i]);
                URL targetChild = URLFactory.createURL(m_url, sourceChilds[i].getName());
                int targetFlags =
                        (sourceChildEntry instanceof NSDirectory
                                ? flags
                                : effectiveFlags.remove(Flags.RECURSIVE));
                try {
                    sourceChildEntry.copy(targetChild, targetFlags);
                } catch (AlreadyExists e) {
                    throw new NoSuccess("Unexpected exception: AlreadyExists", e);
                }
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    /** override super.move() */
    public void move(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceDir().move(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE.or(Flags.OVERWRITE.or(Flags.CREATEPARENTS))));
        effectiveFlags.checkRequired(Flags.RECURSIVE.getValue());
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DataRename
                && m_url.getScheme().equals(effectiveTarget.getScheme())
                && (m_url.getUserInfo()==null || m_url.getUserInfo().equals(effectiveTarget.getUserInfo()))
                && (m_url.getHost()==null || m_url.getHost().equals(effectiveTarget.getHost()))
                && (m_url.getPort()==effectiveTarget.getPort()))
        {
            boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
            try {
                ((DataRename)m_adaptor).rename(
                        JSagaURL.decode(m_url.getPath()),
                        JSagaURL.decode(effectiveTarget.getPath()),
                        overwrite,
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Directory does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DirectoryReader) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // move source childs to target directory
            FileAttributes[] sourceChilds = this._listAttributes(JSagaURL.decode(m_url.getPath()));
            for (int i=0; i<sourceChilds.length; i++) {
                URL remoteChild = URLFactory.createURL(effectiveTarget, sourceChilds[i].getName());
                NSEntry entry = this._openNS(sourceChilds[i]);
                if (entry instanceof NSDirectory) {
                    entry.move(remoteChild, flags);
                } else {
                    entry.move(remoteChild, effectiveFlags.remove(Flags.RECURSIVE));
                }
            }
            // remove source directory
            this.remove(flags);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    /**
     * override super.remove()
     * <br>Note: does not throw a BadParamater exception when RECURSIVE flag is not set, unless directory has some descendants.
     */
    public void remove(int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            try {
                this._dereferenceDir().remove(effectiveFlags.remove(Flags.DEREFERENCE));
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            }
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));
        if (m_adaptor instanceof DirectoryReader) {
            // remove source childs
            if (effectiveFlags.contains(Flags.RECURSIVE)) {
                FileAttributes[] sourceChilds = this._listAttributes(JSagaURL.decode(m_url.getPath()));
                for (int i=0; i<sourceChilds.length; i++) {
                    NSEntry entry;
                    try {
                        entry = this._openNS(sourceChilds[i]);
                    } catch (IncorrectURL e) {
                        throw new NoSuccess(e);
                    }
                    if (entry instanceof NSDirectory) {
                        entry.remove(flags);
                    } else {
                        entry.remove(effectiveFlags.remove(Flags.RECURSIVE));
                    }
                }
            }
            // remove this directory
            URL parent = super._getParentDirURL();
            String directoryName = super._getEntryName();
            try {
                ((DirectoryWriter)m_adaptor).removeDir(
                        JSagaURL.decode(parent.getPath()),
                        directoryName,
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Directory does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    /** override super._getEffectiveURL() */
    protected URL _getEffectiveURL(URL target) throws NotImplemented, IncorrectState, BadParameter, Timeout, NoSuccess {
        if (target.getPath().endsWith("/")) {
            return URLFactory.createURL(target, super._getEntryName()+"/");
        } else {
            return target;
        }
    }

    protected FileAttributes[] _listAttributes(String absolutePath) throws NotImplemented, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof DirectoryReader) {
            try {
                return ((DirectoryReader)m_adaptor).listAttributes(absolutePath, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Directory does not exist: "+absolutePath, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    //does not throw DoesNotExist because it would mean "parent directory does not exist"
    protected NSEntry _openNS(FileAttributes attr) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        switch(attr.getType()) {
            case FileAttributes.DIRECTORY_TYPE:
                return this._openNSDir(new URL(attr.getName()));
            case FileAttributes.FILE_TYPE:
            case FileAttributes.LINK_TYPE:
                return this._openNSEntry(new URL(attr.getName()));
            case FileAttributes.UNKNOWN_TYPE:
            default:
                NSEntry entry = this._openNSEntry(new URL(attr.getName()));
                if (entry.isDir()) {
                    return this._openNSDir(new URL(attr.getName()));
                } else {
                    return entry;
                }
        }
    }

    //does not throw DoesNotExist because it would mean "parent directory does not exist"
    protected NSDirectory _openNSDir(URL name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        try {
            return this.openDir(name, Flags.NONE.getValue());
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        }
    }

    //does not throw DoesNotExist because it would mean "parent directory does not exist"
    protected NSEntry _openNSEntry(URL name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        try {
            return this.open(name, Flags.NONE.getValue());
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        }
    }

    protected void _makeDir(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        // set makeDirFlags
        int makeDirFlags = Flags.CREATE.getValue();
        if (! Flags.OVERWRITE.isSet(flags)) {
            makeDirFlags = Flags.EXCL.or(makeDirFlags);
        }
        if (Flags.CREATEPARENTS.isSet(flags)) {
            makeDirFlags = Flags.CREATEPARENTS.or(makeDirFlags);
        }

        // makeDir
        NSFactory.createNSDirectory(m_session, target, makeDirFlags);
    }
}
