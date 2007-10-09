package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesPhysical;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.*;
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
        // get location of source entry (may be logical or physical)
        List<URI> sourceLocations = m_sourceFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open source entry
            URI source = sourceLocations.get(0);
            NamespaceEntry sourceEntry = createSourceNSEntry(session, source);

            // copy
            sourceEntry.copy(target, targetFlags.remove(Flags.NONE));

            // close source entry (but not the source logical file)
            sourceEntry.close();
        } else {
            throw new NoSuccess("No location found for logical file: "+m_sourceFile.getURI());
        }
    }

    public void putToLogicalFile(Session session, URI target, FlagsBytes targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // get location of source physical file
        List<URI> sourceLocations = m_sourceFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open target logical file
            LogicalFile targetLogicalFile = createTargetLogicalFile(session, target, targetFlags);
            try {
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
            } catch (DoesNotExist e) {
                throw new NoSuccess("Unexpected exception: DoesNotExist", e);
            } finally {
                // close target
                targetLogicalFile.close();
            }
        }
    }

    public static NamespaceEntry createSourceNSEntry(Session session, URI source) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        try {
            return NamespaceFactory.createNamespaceEntry(session, source, Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception: AlreadyExists");
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Source physical file does not exist: "+source, doesNotExist);
        }
    }

    public static LogicalFile createTargetLogicalFile(Session session, URI target, FlagsBytes flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsBytes correctedBytes = flags.or(FlagsBytesPhysical.WRITE).or(FlagsBytes.CREATE);
        Flags[] correctedFlags =
                (correctedBytes.contains(Flags.OVERWRITE)
                        ? correctedBytes.remove(Flags.OVERWRITE)
                        : correctedBytes.add(Flags.EXCL));
        try {
            return LogicalFileFactory.createLogicalFile(session, target, correctedFlags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess("Unexpected exception: DoesNotExist", e);
        } catch (AlreadyExists alreadyExists) {
            throw new AlreadyExists("Target entry already exists: "+target, alreadyExists.getCause());
        }
    }
}
