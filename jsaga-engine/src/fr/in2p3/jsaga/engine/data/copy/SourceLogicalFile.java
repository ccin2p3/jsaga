package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesPhysical;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceEntry;
import org.ogf.saga.session.Session;

import java.util.List;

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
    private LogicalFile m_sourceFile;

    public SourceLogicalFile(LogicalFile sourceFile) {
        m_sourceFile = sourceFile;
    }

    public void putToPhysicalFile(Session session, URI target, FlagsBytes targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // get location of source physical file
        List<URI> sourceLocations = m_sourceFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open source physical file
            URI source = sourceLocations.get(0);
            NamespaceEntry sourcePhysicalFile;
            try {
                sourcePhysicalFile = FileFactory.createFile(session, source, Flags.NONE);
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

    public void putToLogicalFile(Session session, URI target, FlagsBytes targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // get location of source physical file
        List<URI> sourceLocations = m_sourceFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            try {
                // open target logical file
                FlagsBytes correctedBytes = targetFlags.or(FlagsBytesPhysical.WRITE).or(FlagsBytes.CREATE);
                Flags[] correctedFlags =
                        (correctedBytes.contains(Flags.OVERWRITE)
                                ? correctedBytes.remove(Flags.OVERWRITE)
                                : correctedBytes.add(Flags.EXCL));
                LogicalFile targetLogicalFile = LogicalFileFactory.createLogicalFile(session, target, correctedFlags);

                // copy
                if (targetFlags.contains(Flags.OVERWRITE)) {
                    // remove all target locations
                    try {
                        List<URI> targetLocations = targetLogicalFile.listLocations();
                        for (int i=0; targetLocations !=null && i< targetLocations.size(); i++) {
                            targetLogicalFile.removeLocation(targetLocations.get(i));
                        }
                    } catch(IncorrectState e) {
                        // ignore if target logical file does not exist
                    }
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.size(); i++) {
                    targetLogicalFile.addLocation(sourceLocations.get(i));
                }
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            } catch (IncorrectSession e) {
                throw new NoSuccess(e);
            } catch (DoesNotExist e) {
                throw new NoSuccess("Unexpected exception: DoesNotExist", e);
            }
        }
    }
}
