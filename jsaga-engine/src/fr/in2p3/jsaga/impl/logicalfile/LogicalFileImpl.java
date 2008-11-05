package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.impl.logicalfile.copy.LogicalFileCopy;
import fr.in2p3.jsaga.impl.logicalfile.copy.LogicalFileCopyFrom;
import fr.in2p3.jsaga.impl.namespace.*;
import fr.in2p3.jsaga.impl.url.URLHelper;
import fr.in2p3.jsaga.impl.url.URLImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileImpl extends AbstractAsyncLogicalFileImpl implements LogicalFile {
    /** constructor for factory */
    public LogicalFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, URLHelper.toFileURL(url), adaptor, new FlagsHelper(flags).remove(Flags.ALLLOGICALFILEFLAGS));
        this.init(flags);
    }

    /** constructor for NSDirectory.open() */
    public LogicalFileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, URLHelper.toFileURL(relativeUrl), new FlagsHelper(flags).remove(Flags.ALLLOGICALFILEFLAGS));
        this.init(flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public LogicalFileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, URLHelper.toFilePath(absolutePath), new FlagsHelper(flags).remove(Flags.ALLLOGICALFILEFLAGS));
        this.init(flags);
    }

    private void init(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS, Flags.ALLLOGICALFILEFLAGS);
        if (Flags.CREATE.isSet(flags)) {
            if (m_adaptor instanceof LogicalWriter) {
                if (this.exists()) {
                    if (Flags.EXCL.isSet(flags)) {
                        throw new AlreadyExistsException("Entry already exists: "+ m_url);
                    }
                } else if (Flags.CREATEPARENTS.isSet(flags)) {
                    this._makeParentDirs();
                }
            } else {
                throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme());
            }
        } else if (Flags.CREATEPARENTS.isSet(flags)) {
            this._makeParentDirs();
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !((URLImpl)m_url).hasCache() && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor)m_adaptor).exists(
                    m_url.getPath(),
                    m_url.getQuery());
            if (! exists) {
                throw new DoesNotExistException("Logical file does not exist: "+ m_url);
            }
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /** implements super.copy() */
    public void copy(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.CREATEPARENTS, Flags.OVERWRITE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceEntry().copy(target, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        URL effectiveTarget = this._getEffectiveURL(target);
        if (JSAGAFlags.PRESERVETIMES.isSet(flags)) {
            // throw NotImplementedException if can not preserve times
            Date sourceTimes = this.getLastModified();
            AbstractNSEntryImpl targetEntry = super._getTargetEntry_checkPreserveTimes(effectiveTarget);

            // copy
            new LogicalFileCopy(m_session, this, m_adaptor).copy(effectiveTarget, flags);

            // preserve times
            targetEntry.setLastModified(sourceTimes);
        } else {
            // copy only
            new LogicalFileCopy(m_session, this, m_adaptor).copy(effectiveTarget, flags);
        }
    }

    public void copyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.OVERWRITE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceEntry().copyFrom(source, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        URL effectiveSource = this._getEffectiveURL(source);
        if (JSAGAFlags.PRESERVETIMES.isSet(flags)) {
            // throw NotImplementedException if can not preserve times
            Date sourceTimes = super._getSourceTimes_checkPreserveTimes(effectiveSource);

            // copy
            new LogicalFileCopyFrom(m_session, this, m_adaptor).copyFrom(effectiveSource, flags);

            // preserve times
            this.setLastModified(sourceTimes);
        } else {
            // copy only
            new LogicalFileCopyFrom(m_session, this, m_adaptor).copyFrom(effectiveSource, flags);
        }
    }

    /////////////////////////////// class AbstractNSEntryImpl ///////////////////////////////

    public NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, absolutePath, flags);
    }

    public NSEntry openAbsolute(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(absolutePath)) {
            return new LogicalDirectoryImpl(this, absolutePath, flags);
        } else {
            return new LogicalFileImpl(this, absolutePath, flags);
        }
    }

    ///////////////////////////////// interface LogicalFile /////////////////////////////////

    public void addLocation(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).addLocation(
                    m_url.getPath(),
                    name,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void removeLocation(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).removeLocation(
                    m_url.getPath(),
                    name,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void updateLocation(URL nameOld, URL nameNew) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).addLocation(
                    m_url.getPath(),
                    nameNew,
                    m_url.getQuery());
            ((LogicalWriter)m_adaptor).removeLocation(
                    m_url.getPath(),
                    nameOld,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public List<URL> listLocations() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReader) {
            try {
                String[] array = ((LogicalReader)m_adaptor).listLocations(
                        m_url.getPath(),
                        m_url.getQuery());
                List<URL> list = new ArrayList<URL>();
                try {
                    for (String location : array) {
                        list.add(URLFactory.createURL(location));
                    }
                } catch (BadParameterException e) {
                    throw new NoSuccessException(e);
                }
                return list;
            } catch (BadParameterException badParameter) {
                throw new IncorrectStateException("Logical entry is not a file: "+m_url, badParameter);
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Logical file does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void replicate(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReader && m_adaptor instanceof LogicalWriter) {
            this._replicate_step1(name, flags);
            this._replicate_step2(name);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }
    public void replicate(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.replicate(name, Flags.NONE.getValue());
    }

    private void _replicate_step1(URL physicalTarget, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        final String MESSAGE = "Failed to copy replica (state is still consistent)";
        try {
            List<URL> locations = this.listLocations();
            if (locations==null || locations.size()==0) {
                throw new IncorrectStateException("Can not replicate a logical entry with empty location", this);
            }
            URL physicalSource = locations.get(0);
            NSEntry physicalSourceEntry = NSFactory.createNSEntry(m_session, physicalSource, Flags.NONE.getValue());
            physicalSourceEntry.copy(physicalTarget, flags);
        } catch (NotImplementedException e) {
            throw new NotImplementedException(MESSAGE, e);
        } catch (IncorrectURLException e) {
            throw new IncorrectURLException(MESSAGE, e);
        } catch (AuthenticationFailedException e) {
            throw new AuthenticationFailedException(MESSAGE, e);
        } catch (AuthorizationFailedException e) {
            throw new AuthorizationFailedException(MESSAGE, e);
        } catch (PermissionDeniedException e) {
            throw new PermissionDeniedException(MESSAGE, e);
        } catch (BadParameterException e) {
            throw new BadParameterException(MESSAGE, e);
        } catch (IncorrectStateException e) {
            throw new IncorrectStateException(MESSAGE, e);
        } catch (AlreadyExistsException e) {
            throw new AlreadyExistsException(MESSAGE, e);
        } catch (TimeoutException e) {
            throw new TimeoutException(MESSAGE, e);
        } catch (NoSuccessException e) {
            throw new NoSuccessException(MESSAGE, e);
        }
    }
    private void _replicate_step2(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException {
        final String MESSAGE = "INCONSISTENT STATE: Replica has been copied but failed to register the new location";
        try {
            this.addLocation(name);
        } catch (NotImplementedException e) {
            throw new NotImplementedException(MESSAGE, e);
        } catch (IncorrectURLException e) {
            throw new IncorrectURLException(MESSAGE, e);
        } catch (AuthenticationFailedException e) {
            throw new AuthenticationFailedException(MESSAGE, e);
        } catch (AuthorizationFailedException e) {
            throw new AuthorizationFailedException(MESSAGE, e);
        } catch (PermissionDeniedException e) {
            throw new PermissionDeniedException(MESSAGE, e);
        } catch (BadParameterException e) {
            throw new BadParameterException(MESSAGE, e);
        } catch (IncorrectStateException e) {
            throw new IncorrectStateException(MESSAGE, e);
        } catch (TimeoutException e) {
            throw new TimeoutException(MESSAGE, e);
        } catch (NoSuccessException e) {
            throw new NoSuccessException(MESSAGE, e);
        }
    }
}
