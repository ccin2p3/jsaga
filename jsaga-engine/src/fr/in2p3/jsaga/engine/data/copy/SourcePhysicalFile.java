package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.*;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.PhysicalEntryFlags;
import org.ogf.saga.session.Session;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SourcePhysicalFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SourcePhysicalFile {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private FileImpl m_sourceFile;

    public SourcePhysicalFile(FileImpl sourceFile) {
        m_sourceFile = sourceFile;
    }

    public void putToPhysicalFile(Session session, URI target, FlagsContainer targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        IOException closingException = null;

        // open source file if it exists
        InputStream in;
        try {
            in = FileImplStreamFactory.createInputStream(m_sourceFile);
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Source file does not exist: "+m_sourceFile.getURI(), doesNotExist);
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
            throw new IncorrectState("Source file does not exist: "+m_sourceFile.getURI(), e);
        }

        // open target file and copy
        try {
            Flags correctedFlags =
                    (targetFlags.contains(Flags.OVERWRITE)
                        ? targetFlags.remove(Flags.OVERWRITE)               // remove overwrite
                        : targetFlags.remove(Flags.NONE).or(Flags.EXCL))    // add exclusive
                    .or(PhysicalEntryFlags.WRITE).or(Flags.CREATE);         // add write + create
            OutputStream out = FileImplStreamFactory.createOutputStream(session, target, correctedFlags);
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
}
