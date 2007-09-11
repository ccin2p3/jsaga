package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.FlagsContainer;
import fr.in2p3.jsaga.engine.data.LogicalFileImpl;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SourceLogicalFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SourceLogicalFile {
    private LogicalFileImpl m_sourceFile;

    public SourceLogicalFile(LogicalFileImpl sourceFile) {
        m_sourceFile = sourceFile;
    }

    public void putToPhysicalFile(Session session, URI target, FlagsContainer targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // get location of source physical file
        String[] sourceLocations;
        try {
            sourceLocations = m_sourceFile.listLocations();
        } catch (IncorrectURL e) {
            throw new NoSuccess("Failed to get a location for logical file: "+m_sourceFile.getURI(), e);
        }

        if (sourceLocations!=null && sourceLocations.length>0) {
            // open source physical file
            URI source;
            try {
                source = new URI(sourceLocations[0]);
            } catch (URISyntaxException e) {
                throw new BadParameter("Incorrect URI: "+sourceLocations[0]);
            }
            NamespaceEntry sourcePhysicalFile;
            try {
                sourcePhysicalFile = NamespaceFactory.createNamespaceEntry(session, source, Flags.NONE);
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            } catch (IncorrectSession e) {
                throw new NoSuccess(e);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Source physical file does not exist: "+source, doesNotExist);
            }

            // copy
            sourcePhysicalFile.copy(target, targetFlags.remove(Flags.NONE));
        } else {
            throw new NoSuccess("No location found for logical file: "+m_sourceFile.getURI());
        }
    }

    public void putToLogicalFile(Session session, URI target, FlagsContainer targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // get location of source physical file
        String[] sourceLocations;
        try {
            sourceLocations = m_sourceFile.listLocations();
        } catch (IncorrectURL e) {
            throw new NoSuccess("Failed to list locations for logical file: "+m_sourceFile.getURI(), e);
        }

        if (sourceLocations!=null && sourceLocations.length>0) {
            try {
                // open target logical file
                Flags correctedFlags =
                        (targetFlags.contains(Flags.OVERWRITE)
                            ? targetFlags.remove(Flags.OVERWRITE)               // remove overwrite
                            : targetFlags.remove(Flags.NONE).or(Flags.EXCL))    // add exclusive
                        .or(PhysicalEntryFlags.WRITE).or(Flags.CREATE);         // add write + create
                LogicalFileImpl targetLogicalFile = (LogicalFileImpl) NamespaceFactory.createNamespaceEntry(session, target, correctedFlags);

                // copy
                if (targetFlags.contains(Flags.OVERWRITE)) {
                    // remove all target locations
                    try {
                        String[] targetLocations = targetLogicalFile.listLocations();
                        for (int i=0; targetLocations !=null && i< targetLocations.length; i++) {
                            targetLogicalFile.removeLocation(targetLocations[i]);
                        }
                    } catch(IncorrectState e) {
                        // ignore if target logical file does not exist
                    }
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.length; i++) {
                    targetLogicalFile.addLocation(sourceLocations[i]);
                }
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            } catch (IncorrectSession e) {
                throw new NoSuccess(e);
            } catch (DoesNotExist e) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
    }
}
