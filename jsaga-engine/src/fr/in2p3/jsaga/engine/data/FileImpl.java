package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.ExtensionFlags;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStream;
import fr.in2p3.jsaga.engine.base.BufferImpl;
import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

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
public class FileImpl extends AbstractNamespaceEntryImpl implements File {
    private FileReaderStream m_inStream;
    private FileWriterStream m_outStream;

    /** constructor */
    public FileImpl(Session session, URI uri, PhysicalEntryFlags flags, DataAdaptor adaptor) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, adaptor);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.READ);
        effectiveFlags.keepPhysicalEntryFlags();
        if (effectiveFlags.contains(PhysicalEntryFlags.READ)) {
            if (m_adaptor instanceof FileReader) {
                try {
                    m_inStream = ((FileReader)m_adaptor).openFileReaderStream(m_uri.getPath());
                } catch(IncorrectState e) {
                    throw new DoesNotExist("File does not exist: "+m_uri.getPath());
                }
                if (m_inStream == null) {
                    throw new NoSuccess("[ADAPTOR ERROR] Method openFileReaderStream() must return a FileReaderStream object", this);
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
            }
        }
        if (effectiveFlags.contains(PhysicalEntryFlags.WRITE)) {
            if (m_adaptor instanceof FileWriter) {
                boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
                boolean append = effectiveFlags.contains(PhysicalEntryFlags.APPEND);
                m_outStream = ((FileWriter)m_adaptor).openFileWriterStream(super._getParentDirURI().getPath(), super.getName(), overwrite, append);
                if (m_outStream == null) {
                    throw new NoSuccess("[ADAPTOR ERROR] Method openFileWriterStream() must return a FileWriterStream object", this);
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
            }
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
    public void close(float timeoutInSeconds) throws NotImplemented, IncorrectState, NoSuccess {
        if (m_inStream != null) {
            m_inStream.close();
            m_inStream = null;
        }
        if (m_outStream != null) {
            m_outStream.close();
            m_outStream = null;
        }
        super.close(timeoutInSeconds);
    }

    public int getSize() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof FileReader) {
            return ((FileReader)m_adaptor).getSize(m_uri.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public int read(Buffer buf, int len) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_inStream == null) {
            throw new IncorrectState("Reading file requires READ or READWRITE flags", this);
        }
        if (len < 0) {
            throw new BadParameter("Read length must have a positive value: "+len, this);
        }
        if (m_adaptor instanceof FileReader) {
            byte[] bytes = ((BufferImpl)buf).getDataFromSAGAImplementation();
            int readlen = m_inStream.read(bytes, len);
            ((BufferImpl)buf).setSizeFromSAGAImplementation(readlen);
            if (readlen == 0) {
                m_inStream.close();
                m_inStream = null;
            }
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
            int writelen = m_outStream.write(bytes, 0, len);
            if (writelen == 0) {
                m_outStream.close();
                m_outStream = null;
            }
            return writelen;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public int seek(int offset, SeekMode whence) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    protected NamespaceDirectory _openParentDir(Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new DirectoryImpl(m_session, super._getParentDirURI(), PhysicalEntryFlags.cast(flags), m_adaptor);
    }
}
