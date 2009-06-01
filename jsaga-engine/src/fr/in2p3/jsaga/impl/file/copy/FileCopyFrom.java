package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.adaptor.data.*;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.impl.file.AbstractSyncFileImpl;
import fr.in2p3.jsaga.impl.logicalfile.AbstractSyncLogicalFileImpl;
import fr.in2p3.jsaga.impl.namespace.FlagsHelper;
import fr.in2p3.jsaga.impl.namespace.JSAGAFlags;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.IOException;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileCopyFrom
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileCopyFrom {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private Session m_session;
    private AbstractSyncFileImpl m_targetFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public FileCopyFrom(Session session, AbstractSyncFileImpl targetFile, DataAdaptor adaptor) throws NotImplementedException {
        m_session = session;
        m_targetFile = targetFile;
        m_adaptor = adaptor;
    }

    public void copyFrom(URL effectiveSource, int flags, FileCopyFromTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        boolean overwrite = Flags.OVERWRITE.isSet(flags);
        URL target = m_targetFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && target.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        target,
                        overwrite, target.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new DoesNotExistException("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExistsException alreadyExists) {
                throw new IncorrectStateException("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && target.getScheme().equals(effectiveSource.getScheme())) {
            BaseURL base = m_adaptor.getBaseURL();
            if (base == null) {
                base = new BaseURL();
            }
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), base.getPort(effectiveSource), effectiveSource.getPath(),
                        target.getPath(),
                        overwrite, target.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new DoesNotExistException("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExistsException alreadyExists) {
                throw new IncorrectStateException("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof FileWriterPutter && !target.getScheme().equals(effectiveSource.getScheme())) {
            AbstractSyncFileImpl sourceFile = this.createSourceFile(effectiveSource);
            try {
                ((FileWriterPutter)m_adaptor).putFromStream(
                        target.getPath(),
                        false,
                        target.getQuery(),
                        sourceFile.getFileInputStream());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExistException("Target parent directory does not exist: "+target, parentDoesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new IncorrectStateException("Target entry already exists: "+target, alreadyExists);
            } finally {
                sourceFile.close();
            }
        } else if (m_adaptor instanceof FileWriter) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(effectiveSource.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                this.getFromLogicalFile(effectiveSource, flags);
            } else {
                this.getFromPhysicalFile(effectiveSource, flags, progressMonitor);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+target.getScheme());
        }
    }

    private void getFromPhysicalFile(URL source, int flags, FileCopyFromTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        IOException closingException = null;

        // open source file if it exists
        AbstractSyncFileImpl sourceFile = this.createSourceFile(source);
        FileInputStream in = sourceFile.getFileInputStream();

        // start to read if it exists
        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
        int readlen;
        try {
            readlen = in.read(data, 0, data.length);
        } catch (IOException e) {
            try {
                sourceFile.close();
            } catch (Exception e1) {/*ignore*/}
            throw new DoesNotExistException("Source file does not exist: "+source, e);
        }

        // open target file and copy
        try {
            // sourceFlags may contains OVERWRITE flag for target
            boolean overwrite = Flags.OVERWRITE.isSet(flags);
            FileOutputStream out = m_targetFile.newFileOutputStream(overwrite);
            try {
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
            } catch (IOException e) {
                throw new TimeoutException(e);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    closingException = e;
                }
            }
        } catch (AlreadyExistsException alreadyExists) {
            throw new IncorrectStateException("Target file already exists: "+m_targetFile.getURL(), alreadyExists);
        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
                closingException = new IOException(e.getClass().getName()+": "+e.getMessage());
            }
        }
        if (closingException != null) {
            throw new IncorrectStateException(closingException);
        }
    }

    private void getFromLogicalFile(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        // get location of source physical file
        AbstractSyncLogicalFileImpl sourceLogicalFile = this.createSourceLogicalFile(source, flags);
        List<URL> sourceLocations = sourceLogicalFile.listLocationsSync();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // get source physical file
            URL sourcePhysicalUrl = sourceLocations.get(0);
            // copy
            m_targetFile.copyFromSync(sourcePhysicalUrl, flags);
            // close source logical file
            sourceLogicalFile.close();
        } else {
            throw new NoSuccessException("No location found for logical file: "+source);
        }
    }

    private AbstractSyncFileImpl createSourceFile(URL source) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            return (AbstractSyncFileImpl) FileFactory.createFile(m_session, source, Flags.READ.getValue());
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException("Unexpected exception", e);
        } catch (DoesNotExistException doesNotExist) {
            throw new DoesNotExistException("Source file does not exist: "+source, doesNotExist.getCause());
        }
    }

    private AbstractSyncLogicalFileImpl createSourceLogicalFile(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        int correctedFlags = flags;
        correctedFlags = new FlagsHelper(correctedFlags).remove(JSAGAFlags.PRESERVETIMES, Flags.OVERWRITE);
        try {
            return (AbstractSyncLogicalFileImpl) LogicalFileFactory.createLogicalFile(m_session, source, correctedFlags);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException("Unexpected exception", e);
        }
    }
}
