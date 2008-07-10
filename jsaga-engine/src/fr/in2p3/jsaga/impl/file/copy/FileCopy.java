package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.adaptor.data.*;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesPhysical;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.impl.file.FileImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;

import java.io.IOException;
import java.lang.Exception;

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
    private FileImpl m_sourceFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public FileCopy(Session session, FileImpl sourceFile, DataAdaptor adaptor) throws NotImplemented, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_session = session;
        m_sourceFile = sourceFile;
        m_adaptor = adaptor;
    }

    public void copy(URL effectiveTarget, FlagsBytes effectiveFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL source = m_sourceFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        source,
                        effectiveTarget,
                        overwrite, source.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+source, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                BaseURL base = m_adaptor.getBaseURL();
                if (base == null) {
                    base = new BaseURL();
                }
                ((DataCopy)m_adaptor).copy(
                        source.getPath(),
                        effectiveTarget.getHost(), base.getPort(effectiveTarget), effectiveTarget.getPath(),
                        overwrite, source.getQuery());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExist("Target parent directory does not exist: "+effectiveTarget.resolve(new URL(".")), parentDoesNotExist);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source file does not exist: "+source, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof FileReaderGetter && !source.getScheme().equals(effectiveTarget.getScheme())) {
            FileImpl targetFile = this.createTargetFile(effectiveTarget, effectiveFlags);
            try {
                ((FileReaderGetter)m_adaptor).getToStream(
                        source.getPath(),
                        source.getQuery(),
                        targetFile.getFileOutputStream());
            } catch (DoesNotExist doesNotExist) {
                targetFile.remove();
                throw new IncorrectState("Source file does not exist: "+source, doesNotExist);
            } finally {
                targetFile.close();
            }
        } else if (m_adaptor instanceof FileReader) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(effectiveTarget.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                throw new BadParameter("Maybe what you want to do is to register to logical file the following location: "+source.toString());
            } else {
                this.putToPhysicalFile(effectiveTarget, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+source.getScheme());
        }
    }

    private void putToPhysicalFile(URL target, FlagsBytes targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        IOException closingException = null;

        // open source file if it exists
        FileInputStream in;
        try {
            in = m_sourceFile.newFileInputStream();
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Source file does not exist: "+m_sourceFile.getURL(), doesNotExist);
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
            throw new IncorrectState("Source file does not exist: "+m_sourceFile.getURL(), e);
        }

        // open target file and copy
        try {
            FileImpl targetFile = this.createTargetFile(target, targetFlags);
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
                    }
                    readlen = in.read(data,0, data.length);
                }
            } catch (IOException e) {
                throw new Timeout(e);
            } finally {
                try {
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
            throw new IncorrectState(closingException);
        }
    }

    private FileImpl createTargetFile(URL target, FlagsBytes flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes correctedBytes = flags.or(FlagsBytesPhysical.WRITE).or(FlagsBytes.CREATE);
        int correctedFlags =
                (correctedBytes.contains(Flags.OVERWRITE)
                        ? correctedBytes.remove(Flags.OVERWRITE)
                        : correctedBytes.add(Flags.EXCL));
        try {
            return (FileImpl) FileFactory.createFile(m_session, target, correctedFlags);
        } catch (DoesNotExist e) {
            throw new NoSuccess("Unexpected exception: DoesNotExist", e);
        } catch (AlreadyExists alreadyExists) {
            throw new AlreadyExists("Target entry already exists: "+target, alreadyExists.getCause());
        }
    }
}
