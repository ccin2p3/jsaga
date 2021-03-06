package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.impl.file.copy.*;
import fr.in2p3.jsaga.impl.namespace.*;
import fr.in2p3.jsaga.impl.url.URLHelper;
import fr.in2p3.jsaga.impl.url.AbstractURLImpl;
import fr.in2p3.jsaga.sync.file.SyncFile;
import org.ogf.saga.SagaObject;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.IOException;
import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncFileImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncFileImpl extends AbstractNSEntryImplWithStream implements SyncFile {
    protected FileInputStream m_inStream;

    /** constructor for factory */
    protected AbstractSyncFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, URLHelper.toFileURL(url), adaptor, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
        boolean initOK = false;
        try {
        	this.init(flags);
        	initOK = true;
        } finally {
        	if (!initOK) this.close();
        }
    }

    /** constructor for NSDirectory.open() */
    protected AbstractSyncFileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, URLHelper.toFileURL(relativeUrl), new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
        boolean initOK = false;
        try {
           this.init(flags);
           initOK = true;
        } finally {
        	if (!initOK) this.close();
        }
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractSyncFileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, URLHelper.toFilePath(absolutePath), new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
        boolean initOK = false;
        try {
           this.init(flags);
           initOK = true;
        } finally {
        	if (!initOK) this.close();
        }
    }

    private void init(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if(Flags.CREATEPARENTS.isSet(flags)) flags=Flags.CREATE.or(flags);
        if(Flags.CREATE.isSet(flags)) flags=Flags.WRITE.or(flags);
        new FlagsHelper(flags).allowed(JSAGAFlags.BYPASSEXIST, Flags.ALLFILEFLAGS);
        if (Flags.WRITE.isSet(flags)) {
            if (!Flags.CREATE.isSet(flags)) {
	            if (m_adaptor instanceof DataReaderAdaptor && !((DataReaderAdaptor)m_adaptor).exists(m_url.getPath(), m_url.getQuery())) {
	                throw new DoesNotExistException("File does not exist: "+ m_url);
	            }
            }
            try {
                this.tryToOpen(flags);
            } catch (DoesNotExistException e) {  // thrown if parent does not exist
                if (Flags.CREATEPARENTS.isSet(flags)) {
                    // make parent directories
                    this._makeParentDirs();
                    // retry
                    this.tryToOpen(flags);
                } else {
                    throw e;
                }
            } catch (AlreadyExistsException e) {
                if (Flags.EXCL.isSet(flags)) {
                    throw e;
                } else {
                    if (!Flags.APPEND.isSet(flags)) {
                        // delete file first
                        try {
                            this.remove();
                        } catch (IncorrectStateException e1) {
                            throw e;
                        }
                    } else {
                    	throw new NoSuccessException("Adaptor should not throw AlreadExistsException with flag APPEND");
                    }
                    // retry
                    this.tryToOpen(flags);
                }
            }
        }
        if (Flags.READ.isSet(flags)) {
            m_inStream = FileFactoryImpl.openFileInputStream(m_session, m_url, m_adaptor);
        }
        if (Flags.READ.isSet(flags) || Flags.WRITE.isSet(flags)) {
            // exists check already done
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !((AbstractURLImpl)m_url).hasCache() && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor)m_adaptor).exists(m_url.getPath(), m_url.getQuery());
            if (! exists) {
                throw new DoesNotExistException("File does not exist: "+ m_url);
            }
        }
    }
    private void tryToOpen(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean append = Flags.APPEND.isSet(flags);
        boolean exclusive = Flags.EXCL.isSet(flags);
        try {
            m_outStream = FileFactoryImpl.openFileOutputStream(m_session, m_url, m_adaptor, append, exclusive);
        } catch (DoesNotExistException e) {
            throw new DoesNotExistException("Failed to create parent directory", e.getCause());
        }
    }
    /*private void tryToCreate(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean append = true;
        boolean exclusive = Flags.EXCL.isSet(flags);
        try {
            FileOutputStream out = FileFactoryImpl.openFileOutputStream(m_session, m_url, m_adaptor, append, exclusive);
            out.close();
        } catch (DoesNotExistException e) {
            throw new DoesNotExistException("Failed to create parent directory", e.getCause());
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
    }*/

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        FileImpl clone = (FileImpl) super.clone();
        clone.m_inStream = m_inStream;
        return clone;
    }

    ///////////////////////////// override some NSEntry methods /////////////////////////////

    /** override super.close() in order to close opened input and output streams */
    public synchronized void close() throws NotImplementedException, NoSuccessException {
        try {
            if (m_outStream != null) {
                m_outStream.close();
                m_outStream = null;
            }
            if (m_inStream != null) {
                m_inStream.close();
                m_inStream = null;
            }
        } catch (IOException e) {
            throw new NoSuccessException(e);
        } finally {
            super.close();
        }
    }

    /** override super.removeSync() in order to close opened input and output streams */
    public void removeSync(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (m_inStream != null) {
            try {m_inStream.close();} catch (IOException e) {/*ignore*/}
        }
        super.removeSync(flags);
    }

    /** implements super.copySync() */
    public void copySync(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        this._copyAndMonitor(target, flags, null);
    }
    public void _copyAndMonitor(URL target, int flags, AbstractCopyTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        if (Flags.DEREFERENCE.isSet(flags)) {
            AbstractSyncNSEntryImpl entry = this._dereferenceEntry();
            try {
                entry.copySync(target, flags - Flags.DEREFERENCE.getValue());
            } finally {
                entry.close();
            }
            return; //==========> EXIT
        }
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (JSAGAFlags.PRESERVETIMES.isSet(flags)) {
            // throw NotImplementedException if can not preserve times
            long sourceTimes = this.getMTime();
            AbstractNSEntryImpl targetEntry = super._getTargetEntry_checkPreserveTimes(effectiveTarget);

            // copy
            new FileCopy(m_session, this, m_adaptor).copy(effectiveTarget, flags, progressMonitor);

            // preserve times
            targetEntry.setMTime(sourceTimes);
        } else {
            // copy only
            new FileCopy(m_session, this, m_adaptor).copy(effectiveTarget, flags, progressMonitor);
        }
    }

    /** implements super.copyFromSync() */
    public void copyFromSync(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        this._copyFromAndMonitor(source, flags, null);
    }
    public void _copyFromAndMonitor(URL source, int flags, AbstractCopyFromTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.OVERWRITE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            AbstractSyncNSEntryImpl entry = this._dereferenceEntry();
            try {
                entry.copyFromSync(source, flags - Flags.DEREFERENCE.getValue());
            } finally {
                entry.close();
            }
            return; //==========> EXIT
        }
        URL effectiveSource = this._getEffectiveURL(source);
        if (m_inStream != null) {
            try {m_inStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (JSAGAFlags.PRESERVETIMES.isSet(flags)) {
            // throw NotImplementedException if can not preserve times
            long sourceTimes = super._getSourceTimes_checkPreserveTimes(effectiveSource);

            // copy
            new FileCopyFrom(m_session, this, m_adaptor).copyFrom(effectiveSource, flags, progressMonitor);

            // preserve times
            this.setMTime(sourceTimes);
        } else {
            // copy only
            new FileCopyFrom(m_session, this, m_adaptor).copyFrom(effectiveSource, flags, progressMonitor);
        }
    }

    ////////////////////////////// class AbstractNSEntryImpl //////////////////////////////

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

    /////////////////////////////////// interface File ///////////////////////////////////

    public long getSizeSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof FileReader) {
            if (m_outStream != null) {
                try {m_outStream.close();} catch (IOException e) {/*ignore*/}
            }
        }
        FileAttributes attrs = this._getFileAttributes();
        long size = attrs.getSize();
        if (size > -1) {
            return size;
        }
        throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
    }

    public int readSync(Buffer buffer, int offset, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        final int EOF = -1;
        if (m_inStream == null) {
            throw new IncorrectStateException("Reading file requires READ or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameterException("Read length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileReader) {
            int readlen;
            try {
                byte[] bytes = buffer.getData();
                readlen = m_inStream.read(bytes, offset, len);
            } catch (DoesNotExistException e) {
                try {m_inStream.close();} catch (IOException e1) {/*ignore*/}
                throw new IncorrectStateException(e);
            } catch (IOException e) {
                try {m_inStream.close();} catch (IOException e1) {/*ignore*/}
                throw new SagaIOException(e);
            }
            return readlen;
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }
    public int readSync(Buffer buffer, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        return this.readSync(buffer, 0, len);
    }
    public int readSync(Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        return this.readSync(buffer, 0, buffer.getSize());
    }

    public int writeSync(Buffer buffer, int offset, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        if (m_outStream == null) {
            throw new IncorrectStateException("Writing file requires WRITE or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameterException("Write length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileWriter) {
            try {
                byte[] bytes = buffer.getData();
                m_outStream.write(bytes, offset, len);
            } catch (DoesNotExistException e) {
                try {m_outStream.close();} catch (IOException e1) {/*ignore*/}
                throw new NoSuccessException(e);
            } catch (IOException e) {
                try {m_outStream.close();} catch (IOException e1) {/*ignore*/}
                throw new SagaIOException(e);
            }
            return len;
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }
    public int writeSync(Buffer buffer, int len) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        return this.writeSync(buffer, 0, len);
    }
    public int writeSync(Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        return this.writeSync(buffer, 0, buffer.getSize());
    }

    public long seekSync(long offset, SeekMode whence) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public void readVSync(IOVec[] iovecs) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public void writeVSync(IOVec[] iovecs) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public int sizePSync(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectStateException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public int readPSync(String pattern, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public int writePSync(String pattern, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public List<String> modesESync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public int sizeESync(String emode, String spec) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectStateException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public int readESync(String emode, String spec, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public int writeESync(String emode, String spec, Buffer buffer) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, SagaIOException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    ////////////////////////////////// unofficial public methods //////////////////////////////////

    public FileInputStream getFileInputStream() {
        return m_inStream;
    }

    public FileOutputStream getFileOutputStream() {
        return m_outStream;
    }

    public FileInputStream newFileInputStream() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return FileFactoryImpl.openFileInputStream(m_session, m_url, m_adaptor);
    }

    public FileOutputStream newFileOutputStream(boolean overwrite) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean append = false;
        boolean exclusive = !overwrite;
        return FileFactoryImpl.openFileOutputStream(m_session, m_url, m_adaptor, append, exclusive);
    }
}
