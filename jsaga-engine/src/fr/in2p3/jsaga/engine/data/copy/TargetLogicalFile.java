package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.FlagsContainer;
import fr.in2p3.jsaga.engine.data.LogicalFileImpl;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceFactory;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TargetLogicalFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TargetLogicalFile {
    private LogicalFileImpl m_targetFile;

    public TargetLogicalFile(LogicalFileImpl targetFile) {
        m_targetFile = targetFile;
    }

    public void getFromLogicalFile(Session session, URI source, FlagsContainer sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        // get location of source physical file
        String[] sourceLocations;
        try {
            Flags flags = sourceFlags.remove(Flags.OVERWRITE);
            LogicalFileImpl sourceLogicalFile = (LogicalFileImpl) NamespaceFactory.createNamespaceEntry(session, source, flags);
            sourceLocations = sourceLogicalFile.listLocations();
        } catch (IncorrectURL e) {
            throw new NoSuccess("Failed to list locations for logical file: "+source, e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Failed to list locations for logical file: "+source, e);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Failed to list locations for logical file: "+source, e);
        }

        if (sourceLocations!=null && sourceLocations.length>0) {
            try {
                // copy
                // remove all target locations
                try {
                    String[] targetLocations = m_targetFile.listLocations();
                    for (int i=0; targetLocations !=null && i< targetLocations.length; i++) {
                        m_targetFile.removeLocation(targetLocations[i]);
                    }
                } catch(IncorrectState e) {
                    // ignore if target logical file does not exist
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.length; i++) {
                    m_targetFile.addLocation(sourceLocations[i]);
                }
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            } catch (AlreadyExists e) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
    }
}
