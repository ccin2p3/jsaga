package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.ExtensionFlags;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.engine.base.BufferImpl;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.copy.SourcePhysicalFile;
import fr.in2p3.jsaga.engine.data.copy.TargetPhysicalFile;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileImpl extends AbstractNamespaceEntryFileImpl implements File {
    protected InputStream m_inStream;

    /** constructor */
    public FileImpl(Session session, URI uri, PhysicalEntryFlags flags, DataConnection connection) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, connection);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.READ);
        effectiveFlags.keepPhysicalEntryFlags();
        if (effectiveFlags.contains(PhysicalEntryFlags.READ)) {
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
        if (effectiveFlags.contains(PhysicalEntryFlags.WRITE)) {
            if (m_adaptor instanceof FileWriter) {
                URI parent = super._getParentDirURI();
                String fileName = super.getName();
                boolean exclusive = effectiveFlags.contains(Flags.EXCL);
                boolean append = effectiveFlags.contains(PhysicalEntryFlags.APPEND);
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
        if (! effectiveFlags.contains(PhysicalEntryFlags.READ) &&
            ! effectiveFlags.contains(PhysicalEntryFlags.WRITE) &&
            ! new FlagsContainer(flags).contains(ExtensionFlags.LATE_EXISTENCE_CHECK)) {
            if (m_adaptor instanceof DataReaderAdaptor && !((DataReaderAdaptor)m_adaptor).exists(m_uri.getPath())) {
                throw new DoesNotExist("File does not exist: "+m_uri);
            }
        }
    }

    /** constructor for deepCopy */
    protected FileImpl(FileImpl source) {
        super(source);
        m_inStream = source.m_inStream;
        m_outStream = source.m_outStream;
    }
    public SagaBase deepCopy() {
        return new FileImpl(this);
    }

    /** overload close() in order to close opened input and output streams */
    public synchronized void close(float timeoutInSeconds) throws NotImplemented, IncorrectState, NoSuccess {
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
            super.close(timeoutInSeconds);
        }
    }

    public long getSize() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
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

    public int read(Buffer buf, int len) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        final int EOF = -1;
        if (m_inStream == null) {
            throw new IncorrectState("Reading file requires READ or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameter("Read length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileReader) {
            byte[] bytes = ((BufferImpl)buf).getDataFromSAGAImplementation();
            int readlen;
            try {
                readlen = m_inStream.read(bytes, 0, len);
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
            ((BufferImpl)buf).setSizeFromSAGAImplementation(readlen);
            return readlen;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public int write(Buffer buf, int len) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_outStream == null) {
            throw new IncorrectState("Writing file requires WRITE or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameter("Write length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileWriter) {
            byte[] bytes = ((BufferImpl)buf).getDataFromSAGAImplementation();
            try {
                m_outStream.write(bytes, 0, len);
            } catch (IOException e) {
                try {m_outStream.close();} catch (IOException e1) {/*ignore*/}
                throw new Timeout(e);
            }
            return len;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public int seek(int offset, SeekMode whence) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public void copy(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS).or(Flags.OVERWRITE));
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

    public void copyFrom(URI source, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.OVERWRITE));
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

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    public NamespaceDirectory openDir(URI absolutePath, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(m_session, super._resolveAbsoluteURI(absolutePath), PhysicalEntryFlags.cast(flags), m_connection);
    }

    public NamespaceEntry openEntry(URI absolutePath, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new FileImpl(m_session, super._resolveAbsoluteURI(absolutePath), PhysicalEntryFlags.cast(flags), m_connection);
    }
}
