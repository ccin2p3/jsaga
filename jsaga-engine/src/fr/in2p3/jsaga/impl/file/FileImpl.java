package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesPhysical;
import fr.in2p3.jsaga.helpers.URLFactory;
import fr.in2p3.jsaga.impl.file.copy.FileCopy;
import fr.in2p3.jsaga.impl.file.copy.FileCopyFrom;
import fr.in2p3.jsaga.impl.namespace.*;
import org.ogf.saga.*;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileImpl extends AbstractAsyncFileImpl implements File {
    protected FileInputStream m_inStream;

    /** constructor for factory */
    public FileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, URLFactory.toFileURL(url), adaptor, flags);
        this.init(flags);
    }

    /** constructor for NSDirectory.open() */
    public FileImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(dir, URLFactory.toFileURL(relativeUrl), flags);
        this.init(flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public FileImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, URLFactory.toFilePath(absolutePath), flags);
        this.init(flags);
    }

    private void init(int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytesPhysical(flags);
        if (effectiveFlags.contains(Flags.READ)) {
            m_inStream = FileFactoryImpl.openFileInputStream(m_session, m_url, m_adaptor);
        }
        if (effectiveFlags.contains(Flags.WRITE)) {
            boolean append = effectiveFlags.contains(Flags.APPEND);
            boolean exclusive = effectiveFlags.contains(Flags.EXCL);
            try {
                m_outStream = FileFactoryImpl.openFileOutputStream(m_session, m_url, m_adaptor, append, exclusive);
            } catch(DoesNotExist e) {
                // make parent directories, then retry
                if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                    this._makeParentDirs();
                    try {
                        m_outStream = FileFactoryImpl.openFileOutputStream(m_session, m_url, m_adaptor, append, exclusive);
                    } catch(DoesNotExist e2) {
                        throw new DoesNotExist("Failed to create parent directory", e2.getCause());
                    }
                } else {
                    throw e;
                }
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            // make parent directories
            this._makeParentDirs();
        }
        if (effectiveFlags.contains(Flags.READ) || effectiveFlags.contains(Flags.WRITE)) {
            // exists check already done
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !(m_url instanceof JSagaURL) && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor)m_adaptor).exists(m_url.getPath(), m_url.getQuery());
            if (! exists) {
                throw new DoesNotExist("File does not exist: "+ m_url);
            }
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        FileImpl clone = (FileImpl) super.clone();
        clone.m_inStream = m_inStream;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.FILE;
    }

    ///////////////////////////// override some NSEntry methods /////////////////////////////

    /** override super.close() in order to close opened input and output streams */
    public synchronized void close() throws NotImplemented, IncorrectState, NoSuccess {
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
            throw new IncorrectState(e);
        } finally {
            super.close();
        }
    }

    /** override super.close() in order to close opened input and output streams */
    public synchronized void close(float timeoutInSeconds) throws NotImplemented, IncorrectState, NoSuccess {
        this.close();
    }

    /** implements super.copy() */
    public void copy(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS.or(Flags.OVERWRITE)));
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (JSAGAFlags.PRESERVETIMES.isSet(flags)) {
            // throw NotImplemented if can not preserve times
            Date sourceTimes = this.getLastModified();
            AbstractNSEntryImpl targetEntry = super._getTargetEntry_checkPreserveTimes(effectiveTarget);

            // copy
            new FileCopy(m_session, this, m_adaptor).copy(effectiveTarget, effectiveFlags);

            // preserve times
            targetEntry.setLastModified(sourceTimes);
        } else {
            // copy only
            new FileCopy(m_session, this, m_adaptor).copy(effectiveTarget, effectiveFlags);
        }
    }

    /** implements super.copyFrom() */
    public void copyFrom(URL source, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.OVERWRITE));
        URL effectiveSource = this._getEffectiveURL(source);
        if (m_inStream != null) {
            try {m_inStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (JSAGAFlags.PRESERVETIMES.isSet(flags)) {
            // throw NotImplemented if can not preserve times
            Date sourceTimes = super._getSourceTimes_checkPreserveTimes(effectiveSource);

            // copy
            new FileCopyFrom(m_session, this, m_adaptor).copyFrom(effectiveSource, effectiveFlags);

            // preserve times
            this.setLastModified(sourceTimes);
        } else {
            // copy only
            new FileCopyFrom(m_session, this, m_adaptor).copyFrom(effectiveSource, effectiveFlags);
        }
    }

    ////////////////////////////// class AbstractNSEntryImpl //////////////////////////////

    public NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(this, absolutePath, flags);
    }

    public NSEntry openAbsolute(String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (URLFactory.isDirectory(absolutePath)) {
            return new DirectoryImpl(this, absolutePath, flags);
        } else {
            return new FileImpl(this, absolutePath, flags);
        }
    }

    /////////////////////////////////// interface File ///////////////////////////////////

    public long getSize() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof FileReader) {
            if (m_outStream != null) {
                try {m_outStream.close();} catch (IOException e) {/*ignore*/}
            }
        }
        try {
            FileAttributes attrs = this._getFileAttributes();
            long size = attrs.getSize();
            if (size > -1) {
                return size;
            }
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        }
        throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
    }

    public int read(Buffer buffer, int len) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        final int EOF = -1;
        if (m_inStream == null) {
            throw new IncorrectState("Reading file requires READ or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameter("Read length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileReader) {
            int readlen;
            try {
                byte[] bytes = buffer.getData();
                readlen = m_inStream.read(bytes, 0, len);
            } catch (DoesNotExist e) {
                try {m_inStream.close();} catch (IOException e1) {/*ignore*/}
                throw new IncorrectState(e);
            } catch (IOException e) {
                try {m_inStream.close();} catch (IOException e1) {/*ignore*/}
                throw new Timeout(e);
            }
            if (readlen == EOF) {
                try {
                    m_inStream.close();
                } catch (IOException e) {
                    throw new IncorrectState(e);
                }
            }
            return readlen;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }
    public int read(Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        return this.read(buffer, buffer.getSize());
    }

    public int write(Buffer buffer, int len) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        if (m_outStream == null) {
            throw new IncorrectState("Writing file requires WRITE or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameter("Write length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileWriter) {
            try {
                byte[] bytes = buffer.getData();
                m_outStream.write(bytes, 0, len);
            } catch (DoesNotExist e) {
                try {m_outStream.close();} catch (IOException e1) {/*ignore*/}
                throw new Timeout(e);
            } catch (IOException e) {
                try {m_outStream.close();} catch (IOException e1) {/*ignore*/}
                throw new Timeout(e);
            }
            return len;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }
    public int write(Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        return this.write(buffer, buffer.getSize());
    }

    public long seek(long offset, SeekMode whence) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public void readV(IOVec[] iovecs) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public void writeV(IOVec[] iovecs) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int sizeP(String pattern) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int readP(String pattern, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int writeP(String pattern, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public List<String> modesE() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int sizeE(String emode, String spec) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int readE(String emode, String spec, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int writeE(String emode, String spec, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    ////////////////////////////////// unofficial public methods //////////////////////////////////

    public FileInputStream getFileInputStream() {
        return m_inStream;
    }

    public FileOutputStream getFileOutputStream() {
        return m_outStream;
    }

    public FileInputStream newFileInputStream() throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        return FileFactoryImpl.openFileInputStream(m_session, m_url, m_adaptor);
    }

    public FileOutputStream newFileOutputStream(boolean overwrite) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        boolean append = false;
        boolean exclusive = !overwrite;
        return FileFactoryImpl.openFileOutputStream(m_session, m_url, m_adaptor, append, exclusive);
    }
}
