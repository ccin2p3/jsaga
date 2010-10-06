package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.impl.file.AbstractSyncFileImpl;
import fr.in2p3.jsaga.impl.namespace.FlagsHelper;
import fr.in2p3.jsaga.impl.namespace.JSAGAFlags;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileCopy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileCopy {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private Session m_session;
    private AbstractSyncFileImpl m_sourceFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public FileCopy(Session session, AbstractSyncFileImpl sourceFile, DataAdaptor adaptor) throws NotImplementedException {
        m_session = session;
        m_sourceFile = sourceFile;
        m_adaptor = adaptor;
    }

    public void copy(URL effectiveTarget, int flags, AbstractCopyTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        boolean overwrite = Flags.OVERWRITE.isSet(flags);
        URL source = m_sourceFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        source,
                        effectiveTarget,
                        overwrite, source.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Source file does not exist: "+source, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                String targetHost = effectiveTarget.getHost();
                int targetPort = (effectiveTarget.getPort()>-1 ? effectiveTarget.getPort() : m_adaptor.getDefaultPort());
                String targetPath = effectiveTarget.getPath();
                ((DataCopy)m_adaptor).copy(
                        source.getPath(),
                        targetHost, targetPort, targetPath,
                        overwrite, source.getQuery());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExistException("Target parent directory does not exist: "+effectiveTarget.resolve(URLFactory.createURL(".")), parentDoesNotExist);
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Source file does not exist: "+source, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof FileReaderGetter && !source.getScheme().equals(effectiveTarget.getScheme())) {
            AbstractSyncFileImpl targetFile = this.createTargetFile(effectiveTarget, flags);
            try {
                ((FileReaderGetter)m_adaptor).getToStream(
                        source.getPath(),
                        source.getQuery(),
                        targetFile.getFileOutputStream());
            } catch (DoesNotExistException doesNotExist) {
                targetFile.removeSync();
                throw new IncorrectStateException("Source file does not exist: "+source, doesNotExist);
            } finally {
                targetFile.close();
            }
        } else if (m_adaptor instanceof FileReader) {
            // todo: check that target is not a logical entry
            this.putToPhysicalFile(effectiveTarget, flags, progressMonitor);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+source.getScheme());
        }
    }

    private void putToPhysicalFile(URL target, int flags, AbstractCopyTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        IOException closingException = null;

        // open source file if it exists
        FileInputStream in;
        try {
            in = m_sourceFile.newFileInputStream();
        } catch (DoesNotExistException doesNotExist) {
            throw new IncorrectStateException("Source file does not exist: "+m_sourceFile.getURL(), doesNotExist);
        }

        // start to read if it exists
        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
        int readlen;
        try {
            readlen = in.read(data, 0, data.length);
        } catch (IOException e) {
            try {
                in.close();
            } catch (IOException e1) {/*ignore*/}
            throw new IncorrectStateException("Source file does not exist: "+m_sourceFile.getURL(), e);
        }

        // open target file and copy
        try {
            AbstractSyncFileImpl targetFile = this.createTargetFile(target, flags);
            try {
                FileOutputStream out = targetFile.getFileOutputStream();
                while (readlen > 0) {
                    int writelen;
                    for (int total=0; total<readlen; total+=writelen) {
                        writelen = readlen - total;
                        if (total > 0) {
                            byte[] dataBis = new byte[writelen];
                            System.arraycopy(data, total, dataBis, 0, writelen);
                            out.write(dataBis, 0, writelen);
                        } else {
                            out.write(data, 0, readlen);
                        }
                        // update progress monitor
                        if (progressMonitor != null) {
                            progressMonitor.increment(writelen);
                        }
                    }
                    readlen = in.read(data,0, data.length);
                }
                // close stream
                out.close();
            } catch (IOException e) {
                throw new TimeoutException(e);
            } finally {
                try {
                    // close connection
                    targetFile.close();
                } catch (Exception e) {
                    closingException = new IOException(e.getClass().getName()+": "+e.getMessage());
                }
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                closingException = e;
            }
        }
        if (closingException != null) {
            throw new IncorrectStateException(closingException);
        }
    }

    private AbstractSyncFileImpl createTargetFile(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        // set corrected flags
        int correctedFlags = flags;
        correctedFlags = new FlagsHelper(correctedFlags).add(Flags.WRITE, Flags.CREATE);
        correctedFlags = new FlagsHelper(correctedFlags).remove(JSAGAFlags.PRESERVETIMES);
        if (Flags.OVERWRITE.isSet(correctedFlags)) {
            correctedFlags = correctedFlags - Flags.OVERWRITE.getValue();
        } else {
            correctedFlags = correctedFlags + Flags.EXCL.getValue();
        }
        try {
            return (AbstractSyncFileImpl) FileFactory.createFile(m_session, target, correctedFlags);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException("Unexpected exception", e);
        } catch (AlreadyExistsException alreadyExists) {
            throw new AlreadyExistsException("Target entry already exists: "+target, alreadyExists.getCause());
        }
    }
}
