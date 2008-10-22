package fr.in2p3.jsaga.engine.data.copy;

import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import org.ogf.saga.url.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;

import java.util.List;

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
 * TODO: remove this dead code
 */
public class TargetLogicalFile {
    private LogicalFile m_targetFile;

    public TargetLogicalFile(LogicalFile targetFile) {
        m_targetFile = targetFile;
    }

    public void getFromLogicalFile(Session session, URL source, FlagsBytes sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        // get location of source physical file
        LogicalFile sourceLogicalFile = createSourceLogicalFile(session, source, sourceFlags);
        try {
            List<URL> sourceLocations = sourceLogicalFile.listLocations();
            if (sourceLocations!=null && sourceLocations.size()>0) {
                // remove all target locations
                try {
                    List<URL> targetLocations = m_targetFile.listLocations();
                    for (int i=0; targetLocations!=null && i<targetLocations.size(); i++) {
                        m_targetFile.removeLocation(targetLocations.get(i));
                    }
                } catch(IncorrectState e) {
                    // ignore if target logical file does not exist
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.size(); i++) {
                    m_targetFile.addLocation(sourceLocations.get(i));
                }
            }
        } finally {
            sourceLogicalFile.close();
        }
    }

    public static LogicalFile createSourceLogicalFile(Session session, URL source, FlagsBytes flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        int correctedFlags = flags.remove(Flags.OVERWRITE);
        try {
            return LogicalFileFactory.createLogicalFile(session, source, correctedFlags);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception: AlreadyExists", e);
        }
    }
}
