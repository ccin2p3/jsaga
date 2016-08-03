package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.*;
import fr.in2p3.jsaga.impl.url.AbstractURLImpl;
import fr.in2p3.jsaga.impl.url.URLHelper;
import fr.in2p3.jsaga.sync.file.SyncDirectory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncDirectoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class AbstractSyncDirectoryImpl extends AbstractNSDirectoryImpl implements SyncDirectory {
    /** constructor for factory */
    public AbstractSyncDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
    }

    /** constructor for NSDirectory.open() */
    public AbstractSyncDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
    }

    /** constructor for NSEntry.openAbsolute() */
    public AbstractSyncDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /////////////////////////////// class AbstractNSEntryImpl ///////////////////////////////

    /** timeout not supported */
    public NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new DirectoryImpl(this, absolutePath, flags);
    }

    /** timeout not supported */
    public NSEntry openAbsolute(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(absolutePath)) {
            return new DirectoryImpl(this, absolutePath, flags);
        } else {
            return new FileImpl(this, absolutePath, flags);
        }
    }

    /////////////////////////////////// interface NSEntry ///////////////////////////////////

    /** timeout not supported */
    public NSDirectory openDir(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.openDirectory(name, flags);
    }
    /** timeout not supported */
    public NSDirectory openDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.openDirectory(name);
    }

    /** timeout not supported */
    public NSEntry open(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(name)) {
            return this.openDirectory(name, flags);
        } else {
            return this.openFile(name, flags);
        }
    }
    /** timeout not supported */
    public NSEntry open(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(name)) {
            return this.openDirectory(name);
        } else {
            return this.openFile(name);
        }
    }

    /////////////////////////////////// interface Directory ///////////////////////////////////

    // <extra specs>
    public long getSizeSync(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
		int total_size = 0;
		try {
			List list = this.list(flags);
	    	for (int i=0; i<list.size(); i++) {
				AbstractURLImpl url = (AbstractURLImpl)list.get(i);
				if (url.getPath().endsWith("/")) {
					total_size += ((DirectoryImpl)this.openDir(url, flags)).getSize();
				} else {
					if (url.hasCache()) {
						total_size += url.getCache().getSize();
					} else {
						total_size += ((FileImpl)this.openFile(url, flags)).getSize();
					}
				}
	    	}
			return total_size;
		} catch (IncorrectURLException | DoesNotExistException | AlreadyExistsException e) {
			throw new NoSuccessException(e);
		}
    }
    
    public long getSizeSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        return this.getSizeSync(Flags.NONE.getValue());
    }
    // </extra specs>
    
    public long getSizeSync(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.NONE, Flags.DEREFERENCE);
        try {
            File entry = this.openFile(name, flags);
            try {
                return entry.getSize();
            } finally {
                entry.close();
            }
        } catch (BadParameterException bpe) {
            // This is extra-spec: catch BadParameterException thrown by URLHelper in case URL is a directory (eg ".")
        	if (bpe.getStackTrace()[0].getClassName().equals(fr.in2p3.jsaga.impl.url.URLHelper.class.getName())) {
        		if (name.normalize().getPath().equals(m_url.getPath())) {
        			return this.getSizeSync(flags);
        		} else {
        			try {
						return ((DirectoryImpl)this.openDir(name, flags)).getSize();
					} catch (AlreadyExistsException aee) {
			        	// This should never happen since only DEREFERENCE flag is allowed
			            throw new NoSuccessException("Wrong exception thrown: "+ name, aee);
					}
        		}
        	} else {
        		throw bpe;
        	}
        } catch (AlreadyExistsException aee) {
        	// This should never happen since only DEREFERENCE flag is allowed
            throw new NoSuccessException("Wrong exception thrown: "+ name, aee);
        }
    }
    
    public long getSizeSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.getSizeSync(name, Flags.NONE.getValue());
    }

    public boolean isFileSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return super.isEntrySync(name);
    }

    /** timeout not supported */
    public Directory openDirectory(URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new DirectoryImpl(this, relativeUrl, flags);
    }
    /** timeout not supported */
    public Directory openDirectory(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new DirectoryImpl(this, relativeUrl, Flags.READ.getValue());
    }

    /** timeout not supported */
    public File openFile(URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new FileImpl(this, relativeUrl, flags);
    }
    /** timeout not supported */
    public File openFile(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new FileImpl(this, relativeUrl, Flags.READ.getValue());
    }

    public FileInputStream openFileInputStreamSync(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL url = _resolveRelativeUrl(m_url, relativeUrl);
        return FileFactoryImpl.openFileInputStream(m_session, url, m_adaptor);
    }

    public FileOutputStream openFileOutputStreamSync(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL url = _resolveRelativeUrl(m_url, relativeUrl);
        boolean exclusive = false;
        boolean append = false;
        return FileFactoryImpl.openFileOutputStream(m_session, url, m_adaptor, append, exclusive);
    }
    public FileOutputStream openFileOutputStreamSync(URL relativeUrl, boolean append) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL url = _resolveRelativeUrl(m_url, relativeUrl);
        boolean exclusive = false;
        return FileFactoryImpl.openFileOutputStream(m_session, url, m_adaptor, append, exclusive);
    }
}
