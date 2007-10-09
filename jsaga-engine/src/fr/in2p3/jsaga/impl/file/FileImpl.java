package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
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
import fr.in2p3.jsaga.impl.namespace.AbstractNamespaceEntryImpl;
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
public class FileImpl extends AbstractFileImpl implements File {
    protected InputStream m_inStream;

    /** constructor for factory */
    public FileImpl(Session session, URI uri, DataAdaptor adaptor, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor, flags);
        this.init(flags);
    }

    /** constructor for open() */
    public FileImpl(AbstractNamespaceEntryImpl entry, URI uri, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, uri, flags);
        this.init(flags);
    }

    private void init(Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytesPhysical(Flags.READ, flags);
        if (effectiveFlags.contains(Flags.READ)) {
            if (m_adaptor instanceof FileReader) {
                try {
                    m_inStream = ((FileReader)m_adaptor).getInputStream(m_uri.getPath());
                } catch(DoesNotExist e) {
                    throw new DoesNotExist("File does not exist: "+m_uri, e.getCause());
                }
                if (m_inStream == null) {
                    throw new NoSuccess("[ADAPTOR ERROR] Method getInputStream() must return an InputStream instance", this);
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
            }
        }
        if (effectiveFlags.contains(Flags.WRITE)) {
            if (m_adaptor instanceof FileWriter) {
                URI parent = super._getParentDirURI();
                String fileName = super.getName();
                boolean exclusive = effectiveFlags.contains(Flags.EXCL);
                boolean append = effectiveFlags.contains(Flags.APPEND);
                if (exclusive && append) {
                    throw new BadParameter("Incompatible flags: EXCL and APPEND");
                }
                try {
                    m_outStream = ((FileWriter)m_adaptor).getOutputStream(parent.getPath(), fileName, exclusive, append);
                } catch(DoesNotExist e) {
                    // make parent directories, then retry
                    if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                        this._makeParentDirs();
                        m_outStream = ((FileWriter)m_adaptor).getOutputStream(parent.getPath(), fileName, exclusive, append);
                    } else {
                        throw new DoesNotExist("Parent directory does not exist: "+parent, e.getCause());
                    }
                } catch(AlreadyExists e) {
                    throw new AlreadyExists("File already exists: "+m_uri, e.getCause());
                }
                if (m_outStream == null) {
                    throw new NoSuccess("[ADAPTOR ERROR] Method getOutputStream() must return an OutputStream instance", this);
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
        }
        if (!m_skipExistenceCheck && !effectiveFlags.contains(Flags.READ) && !effectiveFlags.contains(Flags.WRITE)) {
            if (m_adaptor instanceof DataReaderAdaptor && !((DataReaderAdaptor)m_adaptor).exists(m_uri.getPath())) {
                throw new DoesNotExist("File does not exist: "+m_uri);
            }
        }
    }

    /** constructor for deepCopy */
    protected FileImpl(FileImpl source) {
        super(source);
        m_inStream = source.m_inStream;
    }
    public SagaBase deepCopy() {
        return new FileImpl(this);
    }

    public ObjectType getType() {
        return ObjectType.FILE;
    }

    ///////////////////////////// override some NamespaceEntry methods /////////////////////////////

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
    public void copy(URI target, Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(Flags.NONE, flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE, Flags.CREATEPARENTS, Flags.OVERWRITE);
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveTarget = this._getEffectiveURI(target);
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (m_adaptor instanceof DataCopyDelegated && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        m_uri,
                        effectiveTarget,
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+m_uri, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copy(
                        m_uri.getPath(),
                        effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+m_uri, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
/*
        } else if (m_adaptor instanceof DataGet) {
            FileImpl targetFile = SourcePhysicalFile.createTargetFile(m_session, effectiveTarget, effectiveFlags);
            try {
                ((DataGet)m_adaptor).getToStream(m_uri.getPath(), targetFile.getOutputStream());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+m_uri, doesNotExist);
            }
            targetFile.close();
*/
        } else if (m_adaptor instanceof FileReader) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(target.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                throw new BadParameter("Maybe what you want to do is to register to logical file the following location: "+m_uri.toString());
            } else {
                SourcePhysicalFile source = new SourcePhysicalFile(this);
                source.putToPhysicalFile(m_session, effectiveTarget, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /** implements super.copyFrom() */
    public void copyFrom(URI source, Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(Flags.NONE, flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE, Flags.OVERWRITE);
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveSource = this._getEffectiveURI(source);
        if (m_inStream != null) {
            try {m_inStream.close();} catch (IOException e) {/*ignore*/}
        }
        if (m_adaptor instanceof DataCopyDelegated && m_uri.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        m_uri,
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+m_uri, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && m_uri.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), effectiveSource.getPort(), effectiveSource.getPath(),
                        m_uri.getPath(),
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+m_uri, alreadyExists);
            }
/*
        } else if (m_adaptor instanceof DataPut) {
            FileImpl sourceFile = TargetPhysicalFile.createSourceFile(m_session, effectiveSource);
            try {
                ((DataPut)m_adaptor).putFromStream(m_uri.getPath(), sourceFile.getInputStream(), false);
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+m_uri, alreadyExists);
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
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /** implements super.openDir() */
    public NamespaceDirectory openDir(URI absolutePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(this, super._resolveAbsoluteURI(absolutePath), flags);
    }

    /** implements super.open() */
    public NamespaceEntry open(URI absolutePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new FileImpl(this, super._resolveAbsoluteURI(absolutePath), flags);
    }

    /////////////////////////////////// implementation of interface ///////////////////////////////////

    public long getSize() throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof FileReader) {
            if (m_outStream != null) {
                try {m_outStream.close();} catch (IOException e) {/*ignore*/}
            }
            try {
                return ((FileReader)m_adaptor).getSize(m_uri.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("File does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public int read(int len, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
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
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public int write(int len, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IOException {
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
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
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

    public int readP(String pattern, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess, IOException {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public int writeP(String pattern, Buffer buffer) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess, IOException {
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
        String absolutePath = super.getURI().getPath();
        return ((FileReader) m_adaptor).getInputStream(absolutePath);
    }

    public OutputStream newOutputStream(boolean overwrite) throws NotImplemented, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        String parentAbsolutePath = super._getParentDirURI().getPath();
        String fileName = super.getName();
        boolean exclusive = !overwrite;
        boolean append = false;
        return ((FileWriter) m_adaptor).getOutputStream(parentAbsolutePath, fileName, exclusive, append);
    }
}
