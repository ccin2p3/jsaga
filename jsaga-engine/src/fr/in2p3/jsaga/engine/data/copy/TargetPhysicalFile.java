package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.*;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.io.*;
import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TargetPhysicalFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 ao�t 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TargetPhysicalFile {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private FileImpl m_targetFile;

    public TargetPhysicalFile(FileImpl targetFile) {
        m_targetFile = targetFile;
    }

    public void getFromPhysicalFile(Session session, URI source, FlagsContainer sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        IOException closingException = null;

        // open source file if it exists
        InputStream in = FileImplStreamFactory.createInputStream(session, source, PhysicalEntryFlags.READ);

        // start to read if it exists
        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
        int readlen;
        try {
            readlen = in.read(data, 0, data.length);
        } catch (IOException e) {
            try {
                in.close();
            } catch (IOException e1) {/*ignore*/}
            throw new DoesNotExist("Source file does not exist: "+source, e);
        }

        // open target file and copy
        try {
            // sourceFlags may contains OVERWRITE flag for target
            boolean overwrite = sourceFlags.contains(Flags.OVERWRITE);
            OutputStream out = FileImplStreamFactory.createOutputStream(m_targetFile, overwrite);
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
            throw new IncorrectState("Target file already exists: "+m_targetFile.getURI(), alreadyExists);
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

    public void getFromLogicalFile(Session session, URI source, FlagsContainer sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        // get location of source physical file
        String[] sourceLocations;
        try {
            Flags flags = sourceFlags.remove(Flags.OVERWRITE);
            LogicalFileImpl sourceLogicalFile = (LogicalFileImpl) NamespaceFactory.createNamespaceEntry(session, source, flags);
            sourceLocations = sourceLogicalFile.listLocations();
        } catch (IncorrectURL e) {
            throw new NoSuccess("Failed to get a location for logical file: "+source, e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Failed to get a location for logical file: "+source, e);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Failed to get a location for logical file: "+source, e);
        }

        if (sourceLocations!=null && sourceLocations.length>0) {
            // open source physical file
            URI sourcePhysicalUri;
            try {
                sourcePhysicalUri = new URI(sourceLocations[0]);
            } catch (URISyntaxException e) {
                throw new BadParameter("Incorrect URI: "+sourceLocations[0]);
            }

            // copy
            m_targetFile.copyFrom(sourcePhysicalUri, sourceFlags.remove(Flags.NONE));
        } else {
            throw new NoSuccess("No location found for logical file: "+source);
        }
    }
}
