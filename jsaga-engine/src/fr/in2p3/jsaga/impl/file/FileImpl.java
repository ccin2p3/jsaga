package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.copy.SourcePhysicalFile;
import fr.in2p3.jsaga.engine.data.copy.TargetPhysicalFile;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesPhysical;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.helpers.URLFactory;
import fr.in2p3.jsaga.impl.namespace.*;
import org.ogf.saga.*;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.io.*;
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
    protected InputStream m_inStream;

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
            if (m_adaptor instanceof FileReader) {
                try {
                    m_inStream = ((FileReader)m_adaptor).getInputStream(
                            m_url.getPath(),
                            m_url.getQuery());
                } catch(DoesNotExist e) {
                    throw new DoesNotExist("File does not exist: "+ m_url, e.getCause());
                }
                if (m_inStream == null) {
                    throw new NoSuccess("[ADAPTOR ERROR] Method getInputStream() must return an InputStream instance", this);
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
        }
        if (effectiveFlags.contains(Flags.WRITE)) {
            if (m_adaptor instanceof FileWriter) {
                URL parent = super._getParentDirURL();
                String fileName = super._getEntryName();
                boolean exclusive = effectiveFlags.contains(Flags.EXCL);
                boolean append = effectiveFlags.contains(Flags.APPEND);
                if (exclusive && append) {
                    throw new BadParameter("Incompatible flags: EXCL and APPEND");
                }
                try {
                    m_outStream = ((FileWriter)m_adaptor).getOutputStream(parent.getPath(), fileName, exclusive, append, m_url.getQuery());
                } catch(ParentDoesNotExist e) {
                    // make parent directories, then retry
                    if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                        this._makeParentDirs();
                        try {
                            m_outStream = ((FileWriter)m_adaptor).getOutputStream(parent.getPath(), fileName, exclusive, append, m_url.getQuery());
                        } catch (ParentDoesNotExist e2) {
                            throw new DoesNotExist("Failed to create parent directory: "+parent, e2.getCause());
                        }
                    } else {
                        throw new DoesNotExist("Parent directory does not exist: "+parent, e.getCause());
                    }
                } catch(AlreadyExists e) {
                    throw new AlreadyExists("File already exists: "+ m_url, e.getCause());
                }
                if (m_outStream == null) {
                    throw new NoSuccess("[ADAPTOR ERROR] Method getOutputStream() must return an OutputStream instance", this);
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
        }
        if (effectiveFlags.contains(Flags.READ) || effectiveFlags.contains(Flags.WRITE)) {
            // exists check already done
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !(m_url instanceof JSagaURL) && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor)m_adaptor).exists(m_url.getPath());
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
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (m_adaptor instanceof DataCopyDelegated && m_url.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        m_url,
                        effectiveTarget,
                        overwrite, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && m_url.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                int targetPort = (effectiveTarget.getPort()>0 ? effectiveTarget.getPort() : m_adaptor.getDefaultPort());
                ((DataCopy)m_adaptor).copy(
                        m_url.getPath(),
                        effectiveTarget.getHost(), targetPort, effectiveTarget.getPath(),
                        overwrite, m_url.getQuery());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExist("Target parent directory does not exist: "+effectiveTarget.resolve(new URL(".")), parentDoesNotExist);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
/*
        } else if (m_adaptor instanceof DataGet) {
            FileImpl targetFile = SourcePhysicalFile.createTargetFile(m_session, effectiveTarget, effectiveFlags);
            try {
                ((DataGet)m_adaptor).getToStream(m_url.getPath(), targetFile.getOutputStream());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+m_url, doesNotExist);
            }
            targetFile.close();
*/
        } else if (m_adaptor instanceof FileReader) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(target.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                throw new BadParameter("Maybe what you want to do is to register to logical file the following location: "+ m_url.toString());
            } else {
                SourcePhysicalFile source = new SourcePhysicalFile(this);
                source.putToPhysicalFile(m_session, effectiveTarget, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
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
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL effectiveSource = this._getEffectiveURL(source);
        if (m_inStream != null) {
            try {m_inStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (m_adaptor instanceof DataCopyDelegated && m_url.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        m_url,
                        overwrite, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+ m_url, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && m_url.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), effectiveSource.getPort(), effectiveSource.getPath(),
                        m_url.getPath(),
                        overwrite, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+ m_url, alreadyExists);
            }
/*
        } else if (m_adaptor instanceof DataPut) {
            FileImpl sourceFile = TargetPhysicalFile.createSourceFile(m_session, effectiveSource);
            try {
                ((DataPut)m_adaptor).putFromStream(m_url.getPath(), sourceFile.getInputStream(), false);
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+m_url, alreadyExists);
            }
            sourceFile.close();
*/
        } else if (m_adaptor instanceof FileWriter) {
            TargetPhysicalFile target = new TargetPhysicalFile(this);
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(source.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                target.getFromLogicalFile(m_session, effectiveSource, effectiveFlags);
            } else {
                target.getFromPhysicalFile(m_session, effectiveSource, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
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
        if (m_url instanceof JSagaURL) {
            long size = ((JSagaURL)m_url).getAttributes().getSize();
            if (size > -1) {
                return size;
            }
        }
        if (m_adaptor instanceof FileReader) {
            if (m_outStream != null) {
                try {m_outStream.close();} catch (IOException e) {/*ignore*/}
            }
            try {
                return ((FileReader)m_adaptor).getSize(
                        m_url.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("File does not exist: "+ m_url, doesNotExist);
            } catch (BadParameter badParameter) {
                throw new IncorrectState("Entry is not a file: "+ m_url, badParameter);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
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

    public InputStream getInputStream() {
        return m_inStream;
    }

    public OutputStream getOutputStream() {
        return m_outStream;
    }

    public InputStream newInputStream() throws NotImplemented, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        String absolutePath = super.getURL().getPath();
        return ((FileReader) m_adaptor).getInputStream(absolutePath, m_url.getQuery());
    }

    public OutputStream newOutputStream(boolean overwrite) throws NotImplemented, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        String parentAbsolutePath = super._getParentDirURL().getPath();
        String fileName = super._getEntryName();
        boolean exclusive = !overwrite;
        boolean append = false;
        try {
            return ((FileWriter) m_adaptor).getOutputStream(parentAbsolutePath, fileName, exclusive, append, m_url.getQuery());
        } catch (ParentDoesNotExist e) {
            throw new DoesNotExist(e);
        }
    }
}
