package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.impl.url.URLHelper;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
    protected AbstractNSEntryDirImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSEntryDirImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSEntryDirImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    /** override super.getCWD() */
    public URL getCWD() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        return m_url.normalize();
    }

    /** override super.copy() */
    public void copy(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceDir().copy(target, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DataReaderAdaptor) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // copy source childs
            FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                NSEntry sourceChildEntry = this._openNS(sourceChilds[i]);
                if (sourceChildEntry instanceof NSDirectory) {
                    sourceChildEntry.copy(effectiveTarget, flags);
                } else {
                    // remove RECURSIVE flag (always set for NSDirectory.copy())
                    sourceChildEntry.copy(effectiveTarget, flags - Flags.RECURSIVE.getValue());
                }
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    /** override super.copyFrom() */
    public void copyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceDir().copyFrom(source, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        if (m_adaptor instanceof DataWriterAdaptor) {
            try {
                NSDirectory sourceDir = NSFactory.createNSDirectory(m_session, source);
                sourceDir.copy(m_url, flags);
            } catch (AlreadyExistsException e) {
                throw new IncorrectStateException("Unexpected exception", e);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    /** override super.move() */
    public void move(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceDir().move(target, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DataRename
                && m_url.getScheme().equals(effectiveTarget.getScheme())
                && (m_url.getUserInfo()==null || m_url.getUserInfo().equals(effectiveTarget.getUserInfo()))
                && (m_url.getHost()==null || m_url.getHost().equals(effectiveTarget.getHost()))
                && (m_url.getPort()==effectiveTarget.getPort()))
        {
            boolean overwrite = Flags.OVERWRITE.isSet(flags);
            try {
                ((DataRename)m_adaptor).rename(
                        m_url.getPath(),
                        effectiveTarget.getPath(),
                        overwrite,
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Directory does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataReaderAdaptor) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // move source childs to target directory
            FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
            for (int i=0; i<sourceChilds.length; i++) {
                URL remoteChild = URLHelper.createURL(effectiveTarget, sourceChilds[i].getName());
                NSEntry entry = this._openNS(sourceChilds[i]);
                if (entry instanceof NSDirectory) {
                    entry.move(remoteChild, flags);
                } else {
                    // remove RECURSIVE flag (always set for NSDirectory.move())
                    entry.move(remoteChild, flags - Flags.RECURSIVE.getValue());
                }
            }
            // remove source directory
            this.remove(flags);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    /**
     * override super.remove()
     * <br>Note: does not throw a BadParamater exception when RECURSIVE flag is not set, unless directory has some descendants.
     */
    public void remove(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                this._dereferenceDir().remove(flags - Flags.DEREFERENCE.getValue());
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
            return; //==========> EXIT
        }
        if (m_adaptor instanceof DataReaderAdaptor && m_adaptor instanceof DataWriterAdaptor) {
            // remove source childs
            if (Flags.RECURSIVE.isSet(flags)) {
                FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
                for (int i=0; i<sourceChilds.length; i++) {
                    NSEntry entry;
                    try {
                        entry = this._openNS(sourceChilds[i]);
                    } catch (IncorrectURLException e) {
                        throw new NoSuccessException(e);
                    }
                    if (entry instanceof NSDirectory) {
                        entry.remove(flags);
                    } else {
                        // remove RECURSIVE flag (always set here)
                        entry.remove(flags - Flags.RECURSIVE.getValue());
                    }
                }
            }
            // remove this directory
            URL parent = super._getParentDirURL();
            String directoryName = super._getEntryName();
            try {
                ((DataWriterAdaptor)m_adaptor).removeDir(
                        parent.getPath(),
                        directoryName,
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Directory does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    /** override super._getEffectiveURL() */
    protected URL _getEffectiveURL(URL target) throws NotImplementedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        if (target.getPath().endsWith("/")) {
            return URLHelper.createURL(target, super._getEntryName()+"/");
        } else {
            return target;
        }
    }

    protected FileAttributes[] _listAttributes(String absolutePath) throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                return ((DataReaderAdaptor)m_adaptor).listAttributes(absolutePath, m_url.getQuery());
            } catch (BadParameterException badParameter) {
                throw new IncorrectStateException("Entry is not a directory: "+absolutePath, badParameter);
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Directory does not exist: "+absolutePath, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    //does not throw DoesNotExistException because it would mean "parent directory does not exist"
    protected NSEntry _openNS(FileAttributes attr) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        switch(attr.getType()) {
            case FileAttributes.DIRECTORY_TYPE:
                return this._openNSDir(URLFactory.createURL(attr.getName()));
            case FileAttributes.FILE_TYPE:
            case FileAttributes.LINK_TYPE:
                return this._openNSEntry(URLFactory.createURL(attr.getName()));
            case FileAttributes.UNKNOWN_TYPE:
            default:
                NSEntry entry = this._openNSEntry(URLFactory.createURL(attr.getName()));
                if (entry.isDir()) {
                    return this._openNSDir(URLFactory.createURL(attr.getName()));
                } else {
                    return entry;
                }
        }
    }

    //does not throw DoesNotExistException because it would mean "parent directory does not exist"
    protected NSDirectory _openNSDir(URL name) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            return this.openDir(name, Flags.NONE.getValue());
        } catch (DoesNotExistException e) {
            throw new IncorrectStateException(e);
        } catch (AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        }
    }

    //does not throw DoesNotExistException because it would mean "parent directory does not exist"
    protected NSEntry _openNSEntry(URL name) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            return this.open(name, Flags.NONE.getValue());
        } catch (DoesNotExistException e) {
            throw new IncorrectStateException(e);
        } catch (AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        }
    }

    protected void _makeDir(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
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
