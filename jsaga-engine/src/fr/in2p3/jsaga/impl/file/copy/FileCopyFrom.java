package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.adaptor.data.*;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.impl.file.FileImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;

import java.io.IOException;
import java.lang.Exception;
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
    private FileImpl m_targetFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public FileCopyFrom(Session session, FileImpl targetFile, DataAdaptor adaptor) throws NotImplemented, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_session = session;
        m_targetFile = targetFile;
        m_adaptor = adaptor;
    }

    public void copyFrom(URL effectiveSource, FlagsBytes effectiveFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL target = m_targetFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && target.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        target,
                        overwrite, target.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+target, alreadyExists);
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
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Source file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof FileWriterPutter && !target.getScheme().equals(effectiveSource.getScheme())) {
            FileImpl sourceFile = this.createSourceFile(effectiveSource);
            try {
                ((FileWriterPutter)m_adaptor).putFromStream(
                        target.getPath(),
                        false,
                        target.getQuery(),
                        sourceFile.getFileInputStream());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExist("Target parent directory does not exist: "+target, parentDoesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+target, alreadyExists);
            } finally {
                sourceFile.close();
            }
        } else if (m_adaptor instanceof FileWriter) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(effectiveSource.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                this.getFromLogicalFile(effectiveSource, effectiveFlags);
            } else {
                this.getFromPhysicalFile(effectiveSource, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+target.getScheme());
        }
    }

    private void getFromPhysicalFile(URL source, FlagsBytes sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        IOException closingException = null;

        // open source file if it exists
        FileImpl sourceFile = this.createSourceFile(source);
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
            throw new DoesNotExist("Source file does not exist: "+source, e);
        }

        // open target file and copy
        try {
            // sourceFlags may contains OVERWRITE flag for target
            boolean overwrite = sourceFlags.contains(Flags.OVERWRITE);
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
                    }
                    readlen = in.read(data,0, data.length);
                }
            } catch (IOException e) {
                throw new Timeout(e);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    closingException = e;
                }
            }
        } catch (AlreadyExists alreadyExists) {
            throw new IncorrectState("Target file already exists: "+m_targetFile.getURL(), alreadyExists);
        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
                closingException = new IOException(e.getClass().getName()+": "+e.getMessage());
            }
        }
        if (closingException != null) {
            throw new IncorrectState(closingException);
        }
    }

    private void getFromLogicalFile(URL source, FlagsBytes sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        // get location of source physical file
        LogicalFile sourceLogicalFile = this.createSourceLogicalFile(source, sourceFlags);
        List<URL> sourceLocations = sourceLogicalFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // get source physical file
            URL sourcePhysicalUrl = sourceLocations.get(0);
            // copy
            m_targetFile.copyFrom(sourcePhysicalUrl, sourceFlags.remove(Flags.NONE));
            // close source logical file
            sourceLogicalFile.close();
        } else {
            throw new NoSuccess("No location found for logical file: "+source);
        }
    }

    private FileImpl createSourceFile(URL source) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        try {
            return (FileImpl) FileFactory.createFile(m_session, source, Flags.READ.getValue());
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception: DoesNotExist", e);
        } catch (DoesNotExist doesNotExist) {
            throw new DoesNotExist("Source file does not exist: "+source, doesNotExist.getCause());
        }
    }

    private LogicalFile createSourceLogicalFile(URL source, FlagsBytes flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        int correctedFlags = flags.remove(Flags.OVERWRITE);
        try {
            return LogicalFileFactory.createLogicalFile(m_session, source, correctedFlags);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception: AlreadyExists", e);
        }
    }
}
